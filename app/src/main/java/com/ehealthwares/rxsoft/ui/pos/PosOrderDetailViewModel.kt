package com.rxsoft.mobile.ui.pos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.SaleDto
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PosOrderDetailViewModel @Inject constructor(
    private val posRepository: PosRepository
) : ViewModel() {

    private val _sale = MutableStateFlow<UiState<SaleDto>>(UiState.Idle)
    val sale: StateFlow<UiState<SaleDto>> = _sale.asStateFlow()

    fun loadSale(id: String) {
        viewModelScope.launch {
            _sale.value = UiState.Loading
            posRepository.getSale(id)
                .onSuccess { _sale.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("PosOrderDetailVM", "Failed to load sale: ${e.message}", e)
                    _sale.value = UiState.Error(e.message ?: "Failed to load sale")
                }
        }
    }
}
