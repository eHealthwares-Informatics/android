package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.remote.api.PurchasesApi
import com.rxsoft.mobile.data.remote.api.InventoryApi
import com.rxsoft.mobile.data.remote.api.ItemsApi
import com.rxsoft.mobile.data.remote.api.CustomersApi
import com.rxsoft.mobile.data.remote.dto.CreatePurchaseLine
import com.rxsoft.mobile.data.remote.dto.CreatePurchaseRequest
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.data.remote.dto.PartyDto
import com.rxsoft.mobile.data.remote.dto.PurchaseDto
import com.rxsoft.mobile.data.remote.dto.ReceiveGoodsLine
import com.rxsoft.mobile.data.remote.dto.ReceiveGoodsRequest
import com.rxsoft.mobile.data.remote.dto.ReferenceDto
import com.rxsoft.mobile.data.remote.dto.StockLocationDto
import com.rxsoft.mobile.data.remote.dto.UomDto
import com.rxsoft.mobile.data.remote.dto.toItemDto
import java.math.BigDecimal
import javax.inject.Inject

class PurchasesRepository @Inject constructor(
    private val purchasesApi: PurchasesApi,
    private val customersApi: CustomersApi,
    private val inventoryApi: InventoryApi,
    private val itemsApi: ItemsApi,
    private val offlineItemDao: OfflineItemDao,
) {
    suspend fun listPurchases(page: Int = 1, limit: Int = 20, search: String? = null): Result<List<PurchaseDto>> {
        return try {
            val params = mutableMapOf("page" to page.toString(), "limit" to limit.toString())
            search?.let { params["search"] = it }
            Result.success(purchasesApi.listPurchases(params).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPurchase(id: String): Result<PurchaseDto> {
        return try {
            Result.success(purchasesApi.getPurchase(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPurchase(
        supplierId: String,
        warehouseId: String,
        lines: List<CreatePurchaseLine>,
        note: String? = null,
        invoiceNumber: String? = null,
        currencyCode: String = "NGN",
    ): Result<PurchaseDto> {
        return try {
            val request = CreatePurchaseRequest(
                supplierId = supplierId,
                warehouseId = warehouseId,
                currencyCode = currencyCode,
                note = note,
                invoiceNumber = invoiceNumber,
                lines = lines,
            )
            Result.success(purchasesApi.createPurchase(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Approve a draft purchase order (status update). */
    suspend fun approvePurchase(id: String): Result<PurchaseDto> {
        return try {
            Result.success(purchasesApi.updatePurchase(id, com.rxsoft.mobile.data.remote.api.UpdatePurchaseStatusRequest(status = "approved")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Receive goods against a purchase order (full receipt of outstanding quantities). */
    suspend fun receiveGoods(
        purchaseId: String,
        lines: List<ReceiveGoodsLine>,
        note: String? = null,
    ): Result<PurchaseDto> {
        return try {
            val request = ReceiveGoodsRequest(
                purchaseOrderId = purchaseId,
                receivedDate = java.time.LocalDate.now().toString(),
                note = note,
                lines = lines,
            )
            Result.success(purchasesApi.receiveGoods(purchaseId, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Lookups for the create flow ─────────────────────────────────────────

    /** Suppliers come from the dedicated /suppliers endpoint (party_type supplier/both). */
    suspend fun listSuppliers(): Result<List<PartyDto>> {
        return try {
            val params = mapOf("page" to "1", "limit" to "200")
            Result.success(customersApi.listSuppliers(params).data.map { c ->
                PartyDto(id = c.id, name = c.name, phone = c.phone, email = c.email)
            })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listWarehouses(): Result<List<StockLocationDto>> {
        return try {
            Result.success(inventoryApi.stockLocations(mapOf("limit" to "200")).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listUoms(): Result<List<UomDto>> {
        return try {
            Result.success(itemsApi.getUoms().data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchOrgItems(query: String): Result<List<ItemDto>> {
        return try {
            val needle = query.trim().lowercase()
            if (needle.isEmpty()) return Result.success(emptyList())
            Result.success(offlineItemDao.search(needle).map { it.toItemDto() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun line(
            itemId: String,
            orderedQty: BigDecimal,
            uomId: String,
            unitCost: BigDecimal,
        ): CreatePurchaseLine = CreatePurchaseLine(itemId, orderedQty, uomId, unitCost)

        fun receiveLine(
            itemId: String,
            receivedQty: BigDecimal,
            unitCost: BigDecimal,
            uomId: String,
        ): ReceiveGoodsLine = ReceiveGoodsLine(itemId, receivedQty, unitCost, uomId)
    }
}
