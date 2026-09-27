package com.rxsoft.mobile.data.repository

import android.util.Log
import com.rxsoft.mobile.data.local.CachedCategoryEntity
import com.rxsoft.mobile.data.local.CachedCustomerEntity
import com.rxsoft.mobile.data.local.CachedItemEntity
import com.rxsoft.mobile.data.local.CachedPriceEntity
import com.rxsoft.mobile.data.local.CachedPriceListEntity
import com.rxsoft.mobile.data.local.CachedStockBalanceEntity
import com.rxsoft.mobile.data.local.CachedStockLocationEntity
import com.rxsoft.mobile.data.local.CachedUomEntity
import com.rxsoft.mobile.data.local.CategoryDao
import com.rxsoft.mobile.data.local.CustomerDao
import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.local.PriceDao
import com.rxsoft.mobile.data.local.PriceListDao
import com.rxsoft.mobile.data.local.StockBalanceDao
import com.rxsoft.mobile.data.local.StockLocationDao
import com.rxsoft.mobile.data.local.SyncStateDao
import com.rxsoft.mobile.data.local.SyncStateEntity
import com.rxsoft.mobile.data.local.UomDao
import com.rxsoft.mobile.data.remote.api.SyncApi
import com.rxsoft.mobile.data.remote.dto.SyncDeletedRef
import com.rxsoft.mobile.data.remote.dto.SyncMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class SyncProgress(
    val running: Boolean = false,
    val entity: String? = null,
    val completed: Int = 0,
    val total: Int = 0,
    val error: String? = null,
) {
    val fraction: Float get() = if (total <= 0) 0f else completed.toFloat() / total.toFloat()
}

private data class PageResult<D>(
    val data: List<D>,
    val deleted: List<SyncDeletedRef>,
    val meta: SyncMeta,
)

private object Entities {
    const val ITEMS = "items"
    const val PRICE_LISTS = "priceLists"
    const val PRICE_LIST_ITEMS = "priceListItems"
    const val STOCK_LOCATIONS = "stockLocations"
    const val STOCK_BALANCES = "stockBalances"
    const val CUSTOMERS = "customers"
    const val CATEGORIES = "categories"
    const val UOMS = "uoms"

    val ALL = listOf(
        // Pricing first so catalog/POS pricing is available even if the (large)
        // item catalogue sync is still running or times out.
        PRICE_LISTS, PRICE_LIST_ITEMS, CATEGORIES, UOMS,
        STOCK_LOCATIONS, CUSTOMERS, ITEMS, STOCK_BALANCES,
    )
}

/**
 * Pulls the organisation's catalog (items, all price lists, stock, customers,
 * categories, uoms) into SQLite. The first run does a full fetch; subsequent
 * runs use a cheap manifest call and only query entities that changed.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncApi: SyncApi,
    private val itemDao: OfflineItemDao,
    private val priceListDao: PriceListDao,
    private val priceDao: PriceDao,
    private val stockLocationDao: StockLocationDao,
    private val stockBalanceDao: StockBalanceDao,
    private val customerDao: CustomerDao,
    private val categoryDao: CategoryDao,
    private val uomDao: UomDao,
    private val syncStateDao: SyncStateDao,
) {
    companion object {
        private const val TAG = "SyncRepository"
        private const val PAGE_SIZE = 500
        private const val MAX_PAGES = 200
    }

    private val _progress = MutableStateFlow(SyncProgress())
    val progress: StateFlow<SyncProgress> = _progress.asStateFlow()

    private val _isSynced = MutableStateFlow(false)
    val isSynced: StateFlow<Boolean> = _isSynced.asStateFlow()

    private val mutex = Mutex()

    /** Ordered entity keys, used by the Settings sync panel. */
    val entityKeys: List<String> = Entities.ALL

    /** Observe per-entity sync cursors + last-synced timestamps. */
    fun observeSyncStates(): kotlinx.coroutines.flow.Flow<List<SyncStateEntity>> =
        syncStateDao.observeAll()

    /** True when there is usable cached catalog data on device. */
    suspend fun hasCache(): Boolean = itemDao.count() > 0

    /**
     * Ensures the local cache is up to date.
     *
     * @param force when true, ignores cursors and pulls a full snapshot.
     */
    suspend fun ensureSynced(force: Boolean = false) {
        mutex.withLock {
            _progress.value = SyncProgress(running = true, completed = 0, total = Entities.ALL.size)
            try {
                val changed = if (force) {
                    Entities.ALL.associateWith { true }
                } else {
                    changedEntities()
                }

                var done = 0
                for (entity in Entities.ALL) {
                    val shouldSync = changed[entity] ?: true
                    val hasCursor = syncStateDao.get(entity)?.cursor != null
                    if (!shouldSync && hasCursor) {
                        done++
                        _progress.value = _progress.value.copy(entity = entity, completed = done)
                        continue
                    }
                    _progress.value = _progress.value.copy(entity = entity)
                    try {
                        syncEntity(entity)
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "Sync of '$entity' failed: ${e.message}")
                    }
                    done++
                    _progress.value = _progress.value.copy(entity = entity, completed = done)
                }
                _isSynced.value = true
                _progress.value = _progress.value.copy(
                    running = false,
                    entity = null,
                    completed = Entities.ALL.size,
                )
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed: ${e.message}", e)
                _progress.value = _progress.value.copy(running = false, error = e.message ?: "Sync failed")
                throw e
            }
        }
    }

    /**
     * Asks the server which entities changed since the oldest local cursor.
     * Entities with no local cursor are always considered changed.
     */
    private suspend fun changedEntities(): Map<String, Boolean> {
        val cursors = Entities.ALL.associateWith { syncStateDao.get(it)?.cursor }
        if (cursors.values.any { it.isNullOrBlank() }) {
            // Never synced (or a newly added entity) → sync everything.
            return Entities.ALL.associateWith { true }
        }
        val minCursor = cursors.values.filterNotNull().minOrNull()
        val manifest = syncApi.manifest(if (minCursor != null) mapOf("updatedFrom" to minCursor) else emptyMap())
        val e = manifest.entities
        return mapOf(
            Entities.ITEMS to e.items.changed,
            Entities.PRICE_LISTS to e.priceLists.changed,
            Entities.PRICE_LIST_ITEMS to e.priceListItems.changed,
            Entities.STOCK_LOCATIONS to e.stockLocations.changed,
            Entities.STOCK_BALANCES to e.stockBalances.changed,
            Entities.CUSTOMERS to e.customers.changed,
            Entities.CATEGORIES to e.categories.changed,
            Entities.UOMS to e.uoms.changed,
        )
    }

    private suspend fun syncEntity(entity: String) {
        when (entity) {
            Entities.ITEMS -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.items(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    if (data.isNotEmpty()) itemDao.insertAll(data.map { it.toEntity() })
                    if (deleted.isNotEmpty()) itemDao.deleteByIds(deleted.map { it.itemId ?: it.id })
                },
            )

            Entities.PRICE_LISTS -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.priceLists(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    if (data.isNotEmpty()) priceListDao.upsertAll(data.map {
                        CachedPriceListEntity(it.id, it.code, it.name, it.isDefault, it.isActive, it.updatedAt)
                    })
                    if (deleted.isNotEmpty()) priceListDao.deleteByIds(deleted.map { it.id })
                },
            )

            Entities.PRICE_LIST_ITEMS -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.priceListItems(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    val rows = data.filter { it.priceListId != null && it.itemId != null }.map {
                        CachedPriceEntity(
                            priceListId = it.priceListId!!,
                            itemId = it.itemId!!,
                            unitPrice = it.unitPrice.toString(),
                            currencyCode = it.currencyCode,
                            startsAt = it.startsAt,
                            endsAt = it.endsAt,
                            updatedAt = it.updatedAt,
                        )
                    }
                    if (rows.isNotEmpty()) priceDao.upsertAll(rows)
                    for (d in deleted) {
                        val pl = d.priceListId
                        val iid = d.itemId
                        if (pl != null && iid != null) priceDao.delete(pl, iid)
                    }
                },
            )

            Entities.STOCK_LOCATIONS -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.stockLocations(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    if (data.isNotEmpty()) stockLocationDao.upsertAll(data.map {
                        CachedStockLocationEntity(it.id, it.code, it.name, it.locationType, it.isActive, it.parentId, it.updatedAt)
                    })
                    if (deleted.isNotEmpty()) stockLocationDao.deleteByIds(deleted.map { it.id })
                },
            )

            Entities.STOCK_BALANCES -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.stockBalances(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    if (data.isNotEmpty()) stockBalanceDao.upsertAll(data.map {
                        CachedStockBalanceEntity(
                            id = it.id,
                            itemId = it.itemId,
                            itemCode = it.itemCode,
                            itemName = it.itemName,
                            locationId = it.locationId,
                            locationName = it.locationName,
                            lotId = it.lotId,
                            lotCode = it.lotCode,
                            quantityOnHand = it.quantityOnHand.toString(),
                            quantityReserved = it.quantityReserved.toString(),
                            averageCost = it.averageCost?.toString(),
                            reorderMinQty = it.reorderMinQty?.toString(),
                            reorderMaxQty = it.reorderMaxQty?.toString(),
                            updatedAt = it.updatedAt,
                        )
                    })
                    if (deleted.isNotEmpty()) stockBalanceDao.deleteByIds(deleted.map { it.id })
                },
            )

            Entities.CUSTOMERS -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.customers(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    if (data.isNotEmpty()) customerDao.upsertAll(data.map {
                        CachedCustomerEntity(
                            id = it.id,
                            partyType = it.partyType,
                            code = it.code,
                            name = it.name,
                            phone = it.phone,
                            email = it.email,
                            addressLine1 = it.addressLine1,
                            isActive = it.isActive,
                            updatedAt = it.updatedAt,
                            searchText = customerSearchText(it.name, it.code, it.phone, it.email),
                        )
                    })
                    if (deleted.isNotEmpty()) customerDao.deleteByIds(deleted.map { it.id })
                },
            )

            Entities.CATEGORIES -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.categories(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    if (data.isNotEmpty()) categoryDao.upsertAll(data.map {
                        CachedCategoryEntity(it.id, it.code, it.name, it.parentId, it.uomCategoryId, it.updatedAt)
                    })
                    if (deleted.isNotEmpty()) categoryDao.deleteByIds(deleted.map { it.id })
                },
            )

            Entities.UOMS -> syncPaged(
                entity,
                fetch = { from, page, limit ->
                    syncApi.uoms(params(from, page, limit)).let {
                        PageResult(it.data, it.deleted, it.meta)
                    }
                },
                apply = { data, deleted ->
                    if (data.isNotEmpty()) uomDao.upsertAll(data.map {
                        CachedUomEntity(it.id, it.code, it.name, it.categoryId, it.uomType, it.factor.toString(), it.rounding.toString(), it.isActive, it.updatedAt)
                    })
                    if (deleted.isNotEmpty()) uomDao.deleteByIds(deleted.map { it.id })
                },
            )
        }
    }

    private fun params(updatedFrom: String?, page: Int, limit: Int): Map<String, String> {
        val m = mutableMapOf("page" to page.toString(), "limit" to limit.toString())
        if (!updatedFrom.isNullOrBlank()) m["updatedFrom"] = updatedFrom
        return m
    }

    private suspend fun <D> syncPaged(
        entity: String,
        fetch: suspend (updatedFrom: String?, page: Int, limit: Int) -> PageResult<D>,
        apply: suspend (List<D>, List<SyncDeletedRef>) -> Unit,
    ) {
        val state = syncStateDao.get(entity)
        val updatedFrom = state?.cursor
        var page = 1
        var cursor = updatedFrom
        while (true) {
            val result = fetch(updatedFrom, page, PAGE_SIZE)
            apply(result.data, result.deleted)
            result.meta.cursor?.let { cursor = it }
            if (result.data.size < PAGE_SIZE) break
            page++
            if (page > MAX_PAGES) break
        }
        syncStateDao.put(SyncStateEntity(entity, cursor, System.currentTimeMillis()))
    }

    private fun itemSearchText(dto: com.rxsoft.mobile.data.remote.dto.SyncItemDto): String {
        return listOfNotNull(
            dto.name,
            dto.displayName,
            dto.code,
            dto.barcode,
            dto.genericProductCode,
            dto.categoryName,
        ).joinToString(" ").lowercase()
    }

    private fun customerSearchText(name: String, code: String?, phone: String?, email: String?): String {
        return listOfNotNull(name, code, phone, email).joinToString(" ").lowercase()
    }

    private fun com.rxsoft.mobile.data.remote.dto.SyncItemDto.toEntity(): CachedItemEntity = CachedItemEntity(
        itemId = itemId,
        orgItemId = orgItemId,
        name = name,
        displayName = displayName,
        code = code,
        barcode = barcode,
        categoryId = categoryId,
        categoryCode = categoryCode,
        categoryName = categoryName,
        baseUomId = baseUomId,
        baseUomCode = baseUomCode,
        baseUomName = baseUomName,
        saleUomId = saleUomId,
        saleUomCode = saleUomCode,
        saleUomName = saleUomName,
        genericProductCode = genericProductCode,
        genericProductName = null,
        imageUrl = imageUrl,
        smallImageUrl = smallImageUrl,
        updatedAt = updatedAt,
        searchText = itemSearchText(this),
        cachedAt = System.currentTimeMillis(),
    )
}
