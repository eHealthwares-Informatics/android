package com.rxsoft.mobile.ui.shop

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.local.CachedItemEntity
import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.local.PriceDao
import com.rxsoft.mobile.data.local.PriceListDao
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.ui.shop.model.Product
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val posRepository: PosRepository,
    private val itemDao: OfflineItemDao,
    private val priceListDao: PriceListDao,
    private val priceDao: PriceDao,
    private val shopCart: ShopCart
) : ViewModel() {

    private val _productState = MutableStateFlow<UiState<Product>>(UiState.Idle)
    val productState: StateFlow<UiState<Product>> = _productState.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _productState.value = UiState.Loading
            val priceListId = resolveDefaultPriceListId()
            val price = priceListId
                ?.let { priceDao.unitPrice(it, itemId) }
                ?.toBigDecimalOrNull()
                ?.toDouble()
                ?: 0.0

            // Same source as POS: read the item from the local catalogue first.
            val cached = try {
                itemDao.getById(itemId)
            } catch (e: Exception) {
                Log.e("ProductDetailVM", "Room item read failed: ${e.message}", e)
                null
            }
            if (cached != null) {
                _productState.value = UiState.Success(cached.toProduct(price))
                return@launch
            }

            // Network fallback.
            posRepository.getItem(itemId)
                .onSuccess { dto ->
                    try {
                        _productState.value = UiState.Success(
                            Product(
                                id = dto.id,
                                name = dto.name,
                                manufacturer = dto.category?.name ?: "",
                                price = price,
                                imageUrl = dto.imageUrl,
                                unit = dto.saleUom?.name ?: dto.baseUom?.name ?: "",
                                description = "",
                                category = dto.category?.name ?: "",
                                uomId = dto.saleUomId ?: dto.baseUomId
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("ProductDetailVM", "Product mapping failed: ${e.message}", e)
                        _productState.value = UiState.Error("Failed to process product: ${e.message}")
                    }
                }
                .onFailure { e ->
                    Log.e("ProductDetailVM", "Failed to load item: ${e.message}", e)
                    _productState.value = UiState.Error(e.message ?: "Failed to load item")
                }
        }
    }

    private suspend fun resolveDefaultPriceListId(): String? = try {
        val lists = priceListDao.all()
        lists.firstOrNull { it.isDefault }?.id ?: lists.firstOrNull()?.id
    } catch (e: Exception) {
        null
    }

    private fun CachedItemEntity.toProduct(price: Double) = Product(
        id = itemId,
        name = displayName ?: name,
        manufacturer = categoryName ?: "",
        price = price,
        imageUrl = imageUrl,
        unit = baseUomName ?: "",
        description = "",
        category = categoryName ?: "",
        code = code,
        barcode = barcode,
        uomId = baseUomId
    )

    fun increaseQuantity() { _quantity.value = _quantity.value + 1 }
    fun decreaseQuantity() { if (_quantity.value > 1) _quantity.value = _quantity.value - 1 }

    fun addToCart(product: Product, quantity: Int = _quantity.value) {
        shopCart.add(product, quantity)
    }
}
