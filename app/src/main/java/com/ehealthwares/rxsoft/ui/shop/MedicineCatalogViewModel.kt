package com.rxsoft.mobile.ui.shop

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.local.CatalogItemWithPrice
import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.local.PriceListDao
import com.rxsoft.mobile.data.remote.dto.OrgItemDto
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.ui.shop.model.Product
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineCatalogViewModel @Inject constructor(
    private val posRepository: PosRepository,
    private val itemDao: OfflineItemDao,
    private val priceListDao: PriceListDao,
    private val shopCart: ShopCart
) : ViewModel() {

    private val _items = MutableStateFlow<UiState<List<Product>>>(UiState.Idle)
    val items: StateFlow<UiState<List<Product>>> = _items.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val cartItemCount: StateFlow<Int> = shopCart.items
        .map { cart -> cart.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private var observeJob: Job? = null

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            _items.value = UiState.Loading
            // Same source as POS: the synced SQLite catalogue + price list.
            val defaultPriceListId = resolveDefaultPriceListId()
            val cached = try {
                itemDao.catalog(defaultPriceListId)
            } catch (e: Exception) {
                Log.e("MedicineCatalogVM", "Room catalog read failed: ${e.message}", e)
                emptyList()
            }

            if (cached.isNotEmpty()) {
                _items.value = UiState.Success(cached.map { it.toProduct() })
            } else {
                // Network fallback (guest / first run before an initial sync).
                posRepository.listOrgItems()
                    .onSuccess { dtos ->
                        _items.value = UiState.Success(dtos.map { it.toProduct(0.0) })
                    }
                    .onFailure { e ->
                        Log.e("MedicineCatalogVM", "Failed to load items: ${e.message}", e)
                        _items.value = UiState.Error(e.message ?: "Failed to load items")
                    }
            }

            // Keep the catalogue in sync with the local DB as it changes.
            observeCatalog(defaultPriceListId)
        }
    }

    private suspend fun resolveDefaultPriceListId(): String? = try {
        val lists = priceListDao.all()
        lists.firstOrNull { it.isDefault }?.id ?: lists.firstOrNull()?.id
    } catch (e: Exception) {
        null
    }

    private fun observeCatalog(priceListId: String?) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            itemDao.observeCatalog(priceListId).collect { rows ->
                if (rows.isNotEmpty()) {
                    _items.value = UiState.Success(rows.map { it.toProduct() })
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: Product) {
        shopCart.add(product)
    }

    private fun CatalogItemWithPrice.toProduct() = Product(
        id = item.itemId,
        name = item.displayName ?: item.name,
        manufacturer = item.categoryName ?: "",
        price = unitPrice?.toDoubleOrNull() ?: 0.0,
        imageUrl = item.imageUrl,
        unit = item.baseUomName ?: "",
        description = "",
        category = item.categoryName ?: "",
        code = item.code,
        barcode = item.barcode,
        uomId = item.baseUomId
    )

    private fun OrgItemDto.toProduct(price: Double) = Product(
        id = itemId,
        name = displayName ?: name,
        manufacturer = category?.name ?: "",
        price = price,
        imageUrl = imageUrl,
        unit = baseUom?.name ?: "",
        description = "",
        category = category?.name ?: "",
        code = code,
        barcode = barcode,
        uomId = baseUom?.id
    )
}
