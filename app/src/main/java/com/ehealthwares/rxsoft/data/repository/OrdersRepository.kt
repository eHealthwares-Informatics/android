package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.local.CachedItemEntity
import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.local.PendingOrderDao
import com.rxsoft.mobile.data.local.PendingOrderEntity
import com.rxsoft.mobile.data.remote.api.ItemsApi
import com.rxsoft.mobile.data.remote.api.WebsiteApi
import com.rxsoft.mobile.data.remote.api.GenericProductsApi
import com.rxsoft.mobile.data.remote.dto.CreateOrderItem
import com.rxsoft.mobile.data.remote.dto.GenericProductDto
import com.rxsoft.mobile.data.remote.dto.OrderDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.util.UUID
import javax.inject.Inject

/** Result of an order submission: whether it was pushed online or queued offline. */
sealed class OrderSubmitResult {
    data class Pushed(val order: OrderDto) : OrderSubmitResult()
    data class Queued(val clientRef: String) : OrderSubmitResult()
}

/**
 * Local-first orders data source:
 *  - Org items are cached in SQLite (Room) so order lines can be searched offline.
 *  - Orders created while offline go into a `pending_orders` outbox table and are
 *    pushed (oldest first) whenever connectivity returns — or on demand.
 */
class OrdersRepository @Inject constructor(
    private val websiteApi: WebsiteApi,
    private val genericProductsApi: GenericProductsApi,
    private val itemsApi: ItemsApi,
    private val offlineItemDao: OfflineItemDao,
    private val pendingOrderDao: PendingOrderDao,
    private val moshi: Moshi,
) {
    private val itemsAdapter by lazy {
        val type = Types.newParameterizedType(List::class.java, CreateOrderItem::class.java)
        moshi.adapter<List<CreateOrderItem>>(type)
    }

    // ── Online lookups ────────────────────────────────────────────────

    suspend fun listOrders(): Result<List<OrderDto>> {
        return try {
            // /website/orders returns a bare JSON array.
            Result.success(websiteApi.listOrders())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchGenericProducts(query: String, page: Int = 1, limit: Int = 20): Result<List<GenericProductDto>> {
        return try {
            Result.success(genericProductsApi.search(query, page, limit).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Offline item cache ──────────────────────────────────────────────────

    /** Pull the org item list and cache it locally for offline search. */
    suspend fun refreshItemCache(): Result<Int> {
        return try {
            val orgItems = itemsApi.listOrgItems()
            val entities = orgItems.map { org ->
                CachedItemEntity(
                    itemId = org.itemId,
                    orgItemId = org.id,
                    name = org.name,
                    displayName = org.displayName,
                    code = org.code,
                    barcode = org.barcode,
                    categoryId = org.category?.id,
                    categoryCode = null,
                    categoryName = org.category?.name,
                    baseUomId = org.baseUom?.id,
                    baseUomCode = org.baseUom?.code,
                    baseUomName = org.baseUom?.name,
                    genericProductCode = org.genericProductCode,
                    genericProductName = org.genericProductName,
                    imageUrl = org.imageUrl,
                    smallImageUrl = org.smallImageUrl,
                    searchText = listOfNotNull(
                        org.name, org.displayName, org.code, org.barcode,
                        org.genericProductCode, org.category?.name,
                    ).joinToString(" ").lowercase(),
                    cachedAt = System.currentTimeMillis(),
                )
            }
            offlineItemDao.insertAll(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search items for order lines: local SQLite first (works offline).
     * Cache is refreshed on app start / pull-to-refresh while online.
     */
    suspend fun searchItems(query: String): Result<List<CachedItemEntity>> {
        return try {
            val needle = query.trim().lowercase()
            if (needle.isEmpty()) return Result.success(emptyList())
            Result.success(offlineItemDao.search(needle))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cachedItemCount(): Int = offlineItemDao.count()

    // ── Order creation (online-first, offline fallback) ─────────────────────

    suspend fun createOrder(
        paymentMethod: String,
        items: List<CreateOrderItem>,
    ): OrderSubmitResult {
        // Try to push immediately.
        val pushed = try {
            websiteApi.createOrder(
                com.rxsoft.mobile.data.remote.dto.CreateOrderRequest(
                    paymentMethod = paymentMethod,
                    origin = "mobile",
                    items = items,
                ),
            )
        } catch (e: Exception) {
            null
        }

        if (pushed != null) return OrderSubmitResult.Pushed(pushed)

        // Offline (or server error): queue in the outbox for later sync.
        val clientRef = UUID.randomUUID().toString()
        pendingOrderDao.insert(
            PendingOrderEntity(
                clientRef = clientRef,
                paymentMethod = paymentMethod,
                itemsJson = itemsAdapter.toJson(items),
                createdAt = System.currentTimeMillis(),
            ),
        )
        return OrderSubmitResult.Queued(clientRef)
    }

    // ── Outbox / sync ───────────────────────────────────────────────────────

    fun observePendingOrders() = pendingOrderDao.observeAll()

    fun observePendingCount() = pendingOrderDao.observeCount()

    /**
     * Push all queued orders (oldest first). Per-order failures are recorded
     * (attempts + last error) and do not stop the remaining pushes.
     *
     * @return number of orders successfully pushed.
     */
    suspend fun syncPendingOrders(): Result<Int> {
        var pushedCount = 0
        for (order in pendingOrderDao.getAll()) {
            try {
                val items = itemsAdapter.fromJson(order.itemsJson) ?: emptyList()
                websiteApi.createOrder(
                    com.rxsoft.mobile.data.remote.dto.CreateOrderRequest(
                        paymentMethod = order.paymentMethod,
                        origin = "mobile",
                        items = items,
                    ),
                )
                pendingOrderDao.deleteByClientRef(order.clientRef)
                pushedCount++
            } catch (e: Exception) {
                pendingOrderDao.markPushFailed(order.clientRef, e.message ?: "unknown error")
            }
        }
        return Result.success(pushedCount)
    }
}
