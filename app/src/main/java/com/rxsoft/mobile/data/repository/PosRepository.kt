package com.rxsoft.mobile.data.repository

import com.rxsoft.mobile.data.local.CachedItemEntity
import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.local.PriceDao
import com.rxsoft.mobile.data.remote.api.*
import com.rxsoft.mobile.data.remote.dto.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.math.BigDecimal
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
) {
    suspend fun createSale(request: CreateSaleRequest): Result<SaleDto> {
        return try {
            Result.success(salesApi.createSale(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            Result.success(paymentMethodsApi.paymentMethods().data)
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

    suspend fun getCategories(): Result<List<CategoryDto>> {
        return try {
            Result.success(itemsApi.getCategories().data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUoms(): Result<List<UomDto>> {
        return try {
            Result.success(itemsApi.getUoms().data)
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
