package com.ehealthwares.rxsoft.data.repository

import com.ehealthwares.rxsoft.data.local.CachedItemEntity
import com.ehealthwares.rxsoft.data.local.CachedPaymentMethodEntity
import com.ehealthwares.rxsoft.data.local.OfflineItemDao
import com.ehealthwares.rxsoft.data.local.PaymentMethodDao
import com.ehealthwares.rxsoft.data.local.PendingSaleDao
import com.ehealthwares.rxsoft.data.local.PendingSaleEntity
import com.ehealthwares.rxsoft.data.local.PriceDao
import com.ehealthwares.rxsoft.data.remote.api.*
import com.ehealthwares.rxsoft.data.remote.dto.*
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class PosRepository @Inject constructor(
    private val salesApi: SalesApi,
    private val itemsApi: ItemsApi,
    private val paymentMethodsApi: PaymentMethodsApi,
    private val configApi: ConfigApi,
    private val customersApi: CustomersApi,
    private val pricingApi: PricingApi,
    private val uploadApi: UploadApi,
    private val offlineItemDao: OfflineItemDao,
    private val priceDao: PriceDao,
    private val paymentMethodDao: PaymentMethodDao,
    private val pendingSaleDao: PendingSaleDao,
    private val moshi: Moshi,
) {
    private val createSaleAdapter by lazy {
        moshi.adapter(CreateSaleRequest::class.java)
    }

    suspend fun createSale(request: CreateSaleRequest): Result<SaleDto> {
        return try {
            Result.success(salesApi.createSale(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSaleOrQueue(request: CreateSaleRequest): SaleSubmitResult {
        return try {
            SaleSubmitResult.Pushed(salesApi.createSale(request))
        } catch (e: Exception) {
            val clientRef = UUID.randomUUID().toString()
            pendingSaleDao.insert(
                PendingSaleEntity(
                    clientRef = clientRef,
                    saleJson = createSaleAdapter.toJson(request),
                    createdAt = System.currentTimeMillis(),
                ),
            )
            SaleSubmitResult.Queued(clientRef)
        }
    }

    suspend fun syncPendingSales(): Result<Int> {
        var pushedCount = 0
        for (sale in pendingSaleDao.getAll()) {
            try {
                val request = createSaleAdapter.fromJson(sale.saleJson) ?: continue
                salesApi.createSale(request)
                pendingSaleDao.deleteByClientRef(sale.clientRef)
                pushedCount++
            } catch (e: Exception) {
                pendingSaleDao.markPushFailed(sale.clientRef, e.message ?: "unknown error")
            }
        }
        return Result.success(pushedCount)
    }

    suspend fun listSales(page: Int = 1, limit: Int = 20): Result<List<SaleDto>> {
        return try {
            val params = mapOf("page" to page.toString(), "limit" to limit.toString())
            Result.success(salesApi.listSales(params).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSale(id: String): Result<SaleDto> {
        return try {
            Result.success(salesApi.getSale(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCustomers(query: String): Result<List<PartyDto>> {
        return try {
            val params = mapOf("search" to query, "limit" to "20")
            Result.success(customersApi.listCustomers(params).data.map { c ->
                PartyDto(id = c.id, name = c.name, phone = c.phone, email = c.email)
            })
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

    suspend fun listOrgItems(): Result<List<OrgItemDto>> {
        return try {
            Result.success(itemsApi.listOrgItems())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentMethods(): Result<List<PaymentMethodDto>> {
        return try {
            val online = paymentMethodsApi.paymentMethods().data
            paymentMethodDao.upsertAll(online.map { it.toCachedEntity() })
            Result.success(online)
        } catch (e: Exception) {
            val cached = paymentMethodDao.all()
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toPaymentMethodDto() })
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun syncPaymentMethods(): Result<Int> {
        return try {
            val online = paymentMethodsApi.paymentMethods().data
            paymentMethodDao.upsertAll(online.map { it.toCachedEntity() })
            Result.success(online.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPosConfig(): Result<UserPosConfig> {
        return try {
            Result.success(configApi.userPosConfig())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemPrice(priceListId: String, itemId: String): Result<BigDecimal?> {
        return try {
            val cached = priceDao.unitPrice(priceListId, itemId)
            if (cached != null) return Result.success(cached.toBigDecimalOrNull())
            val params = mapOf("itemId" to itemId, "limit" to "1")
            val items = pricingApi.getPriceListItems(priceListId, params).data
            Result.success(items.firstOrNull()?.unitPrice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listItems(page: Int = 1, limit: Int = 20, search: String? = null): Result<List<ItemDto>> {
        return try {
            val params = mutableMapOf("page" to page.toString(), "limit" to limit.toString())
            search?.let { params["search"] = it }
            Result.success(itemsApi.listItems(params).data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItem(id: String): Result<ItemDto> {
        return try {
            Result.success(itemsApi.getItem(id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createItem(request: CreateItemRequest): Result<ItemDto> {
        return try {
            Result.success(itemsApi.createItem(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItem(id: String, request: PatchItemRequest): Result<ItemDto> {
        return try {
            Result.success(itemsApi.updateItem(id, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<CategoryDto>> = getCategories(null)

    /** Fetch ALL categories, walking the server's pagination (limit capped at 100). */
    suspend fun getCategories(search: String?): Result<List<CategoryDto>> {
        return try {
            val all = mutableListOf<CategoryDto>()
            var page = 1
            while (true) {
                val response = itemsApi.getCategories(limit = 100, page = page, search = search)
                all += response.data
                val total = response.meta?.total ?: all.size
                if (all.size >= total || response.data.isEmpty()) break
                page++
            }
            Result.success(all)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUoms(): Result<List<UomDto>> = getUoms(null)

    /** Fetch ALL uoms, walking the server's pagination (limit capped at 100). */
    suspend fun getUoms(search: String?): Result<List<UomDto>> {
        return try {
            val all = mutableListOf<UomDto>()
            var page = 1
            while (true) {
                val response = itemsApi.getUoms(limit = 100, page = page, search = search)
                all += response.data
                val total = response.meta?.total ?: all.size
                if (all.size >= total || response.data.isEmpty()) break
                page++
            }
            Result.success(all)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(file: File): Result<UploadImageResponse> {
        return try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            Result.success(uploadApi.uploadImage(part))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class SaleSubmitResult {
    data class Pushed(val sale: SaleDto) : SaleSubmitResult()
    data class Queued(val clientRef: String) : SaleSubmitResult()
}

/** Map a locally-cached catalog item back to the API DTO used by POS screens. */
fun CachedItemEntity.toItemDto(): ItemDto = ItemDto(
    id = itemId,
    code = code,
    name = displayName ?: name,
    barcode = barcode,
    imageUrl = imageUrl,
    smallImageUrl = smallImageUrl,
    category = categoryId?.let { CategoryDto(id = it, name = categoryName) },
    baseUomId = baseUomId,
    saleUomId = saleUomId,
    saleUom = saleUomId?.let { ReferenceDto(id = it, code = saleUomCode, name = saleUomName) },
    baseUom = baseUomId?.let { ReferenceDto(id = it, code = baseUomCode, name = baseUomName) },
)

fun PaymentMethodDto.toCachedEntity(): CachedPaymentMethodEntity = CachedPaymentMethodEntity(
    id = id,
    code = code,
    name = name,
    methodType = methodType,
    isActive = isActive,
)

fun CachedPaymentMethodEntity.toPaymentMethodDto(): PaymentMethodDto = PaymentMethodDto(
    id = id,
    code = code,
    name = name,
    methodType = methodType,
    isActive = isActive,
)
