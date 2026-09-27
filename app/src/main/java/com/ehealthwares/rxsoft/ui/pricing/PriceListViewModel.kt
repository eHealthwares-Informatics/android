package com.rxsoft.mobile.ui.pricing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.PriceListDto
import com.rxsoft.mobile.data.repository.PricingRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceListViewModel @Inject constructor(
    private val pricingRepository: PricingRepository,
) : ViewModel() {

    private val _lists = MutableStateFlow<UiState<List<PriceListDto>>>(UiState.Loading)
    val lists: StateFlow<UiState<List<PriceListDto>>> = _lists.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _lists.value = UiState.Loading
            pricingRepository.listPriceLists()
                .onSuccess { _lists.value = UiState.Success(it) }
                .onFailure { _lists.value = UiState.Error(it.message ?: "Failed to load price lists") }
        }
    }

    fun create(code: String, name: String, isDefault: Boolean) {
        viewModelScope.launch {
            pricingRepository.createPriceList(code, name, isDefault)
                .onSuccess {
                    _message.value = "Price list created"
                    load()
                }
                .onFailure { _message.value = it.message ?: "Create failed" }
        }
    }

    fun update(id: String, code: String?, name: String?, isDefault: Boolean?, isActive: Boolean?) {
        viewModelScope.launch {
            pricingRepository.updatePriceList(id, code, name, isDefault, isActive)
                .onSuccess {
                    _message.value = "Price list updated"
                    load()
                }
                .onFailure { _message.value = it.message ?: "Update failed" }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
