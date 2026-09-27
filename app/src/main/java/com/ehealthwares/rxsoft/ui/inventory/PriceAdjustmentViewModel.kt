package com.ehealthwares.rxsoft.ui.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.local.CachedItemEntity
import com.rxsoft.mobile.data.local.OfflineItemDao
import com.rxsoft.mobile.data.local.PriceDao
import com.rxsoft.mobile.data.remote.dto.PriceListDto
import com.rxsoft.mobile.data.repository.PricingRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/**
 * Price Adjustment: change an item's unit price within a selected price list
 * (percentage quick-adjust or a manual price). Writes go through the pricing
 * API and are mirrored into the local price cache, so POS/catalog pricing
 * picks the new price up immediately.
 */
@HiltViewModel
class PriceAdjustmentViewModel @Inject constructor(
    private val pricingRepository: PricingRepository,
    private val offlineItemDao: OfflineItemDao,
    private val priceDao: PriceDao,
) : ViewModel() {

    private val _priceLists = MutableStateFlow<List<PriceListDto>>(emptyList())
    val priceLists: StateFlow<List<PriceListDto>> = _priceLists.asStateFlow()

    private val _selectedPriceListId = MutableStateFlow<String?>(null)
    val selectedPriceListId: StateFlow<String?> = _selectedPriceListId.asStateFlow()

    private val _searchResults = MutableStateFlow<UiState<List<CachedItemEntity>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<CachedItemEntity>>> = _searchResults.asStateFlow()

    private val _selectedItem = MutableStateFlow<CachedItemEntity?>(null)
    val selectedItem: StateFlow<CachedItemEntity?> = _selectedItem.asStateFlow()

    private val _currentPrice = MutableStateFlow<BigDecimal?>(null)
    val currentPrice: StateFlow<BigDecimal?> = _currentPrice.asStateFlow()

    private val _newPriceText = MutableStateFlow("")
    val newPriceText: StateFlow<String> = _newPriceText.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPriceLists()
    }

    private fun loadPriceLists() {
        viewModelScope.launch {
            pricingRepository.listPriceLists()
                .onSuccess { lists ->
                    _priceLists.value = lists
                    if (_selectedPriceListId.value == null) {
                        _selectedPriceListId.value = lists.firstOrNull()?.id
                        refreshCurrentPrice()
                    }
                }
                .onFailure { e ->
                    Log.w("PriceAdjustVM", "Price list load failed: ${e.message}")
                }
        }
    }

    fun selectPriceList(id: String) {
        if (_selectedPriceListId.value == id) return
        _selectedPriceListId.value = id
        refreshCurrentPrice()
    }

    fun searchItems(query: String) {
        searchJob?.cancel()
        if (query.trim().length < 2) {
            _searchResults.value = UiState.Idle
            return
        }
        searchJob = viewModelScope.launch {
            delay(250)
            _searchResults.value = UiState.Loading
            try {
                val hits = offlineItemDao.search(query.trim().lowercase())
                _searchResults.value = UiState.Success(hits)
            } catch (e: Exception) {
                _searchResults.value = UiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun selectItem(item: CachedItemEntity) {
        _selectedItem.value = item
        _searchResults.value = UiState.Idle
        _submitState.value = UiState.Idle
        refreshCurrentPrice()
    }

    fun clearSelectedItem() {
        _selectedItem.value = null
        _currentPrice.value = null
        _newPriceText.value = ""
        _submitState.value = UiState.Idle
    }

    /** Read the item's current price in the selected list from the local cache. */
    private fun refreshCurrentPrice() {
        val listId = _selectedPriceListId.value ?: return
        val item = _selectedItem.value ?: return
        viewModelScope.launch {
            _currentPrice.value = try {
                priceDao.unitPrice(listId, item.itemId)?.toBigDecimalOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }

    fun updateNewPrice(text: String) {
        _newPriceText.value = text
    }

    /** Quick-adjust the new price by a percentage of the current price. */
    fun applyPercent(percent: Int) {
        val current = _currentPrice.value ?: return
        val factor = BigDecimal.ONE.add(BigDecimal(percent).movePointLeft(2))
        val adjusted = current.multiply(factor).setScale(2, RoundingMode.HALF_UP)
        _newPriceText.value = adjusted.toPlainString()
    }

    fun submit() {
        val listId = _selectedPriceListId.value ?: run {
            _submitState.value = UiState.Error("Select a price list")
            return
        }
        val item = _selectedItem.value ?: run {
            _submitState.value = UiState.Error("Select an item")
            return
        }
        val newPrice = _newPriceText.value.toDoubleOrNull() ?: run {
            _submitState.value = UiState.Error("Enter a valid new price")
            return
        }
        val current = _currentPrice.value
        if (current != null && BigDecimal.valueOf(newPrice).compareTo(current) == 0) {
            _submitState.value = UiState.Error("New price equals the current price")
            return
        }

        viewModelScope.launch {
            _submitState.value = UiState.Loading
            // Find the price-list row for this item to obtain its row id.
            val row = findPriceRow(listId, item)
            if (row == null) {
                _submitState.value = UiState.Error(
                    "This item has no price entry in the selected list. Add it from Price Lists first.",
                )
                return@launch
            }
            pricingRepository.updateItemPrice(listId, row.first, item.itemId, newPrice, row.second)
                .onSuccess {
                    Log.d("PriceAdjustVM", "Price adjusted for ${item.itemId}: $newPrice")
                    _currentPrice.value = BigDecimal.valueOf(newPrice)
                    _submitState.value = UiState.Success(Unit)
                }
                .onFailure { e ->
                    Log.e("PriceAdjustVM", "Price adjustment failed: ${e.message}", e)
                    _submitState.value = UiState.Error(e.message ?: "Adjustment failed")
                }
        }
    }

    private suspend fun findPriceRow(
        listId: String,
        item: CachedItemEntity,
    ): Pair<String, String?>? {
        val needle = item.code ?: item.displayName ?: item.name
        val rows = pricingRepository.listItems(listId, search = needle, page = 1, limit = 50)
            .getOrElse { return null }
        val exact = rows.firstOrNull { it.item?.id == item.itemId }
        return exact?.id?.let { it to exact.currencyCode }
    }

    fun resetSubmitState() {
        _submitState.value = UiState.Idle
    }
}
