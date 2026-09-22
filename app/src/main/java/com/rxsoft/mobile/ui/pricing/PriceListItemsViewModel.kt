package com.rxsoft.mobile.ui.pricing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.PriceListItemDto
import com.rxsoft.mobile.data.repository.PricingRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceListItemsViewModel @Inject constructor(
    private val pricingRepository: PricingRepository,
) : ViewModel() {

    private val _items = MutableStateFlow<UiState<List<PriceListItemDto>>>(UiState.Loading)
    val items: StateFlow<UiState<List<PriceListItemDto>>> = _items.asStateFlow()

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var priceListId: String = ""

    fun load(id: String) {
        priceListId = id
        reload()
    }

    fun updateSearch(query: String) {
        _search.value = query
        reload()
    }

    private fun reload() {
        viewModelScope.launch {
            _items.value = UiState.Loading
            pricingRepository.listItems(priceListId, _search.value, page = 1, limit = 200)
                .onSuccess { _items.value = UiState.Success(it) }
                .onFailure { _items.value = UiState.Error(it.message ?: "Failed to load prices") }
        }
    }

    fun savePrice(item: PriceListItemDto, newPrice: Double) {
        val rowId = item.id ?: return
        val itemId = item.item?.id ?: return
        viewModelScope.launch {
            pricingRepository.updateItemPrice(priceListId, rowId, itemId, newPrice, item.currencyCode)
                .onSuccess {
                    _message.value = "Price updated"
                    reload()
                }
                .onFailure { _message.value = it.message ?: "Update failed" }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
