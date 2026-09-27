package com.rxsoft.mobile.ui.reports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.DailySalesRow
import com.rxsoft.mobile.data.remote.dto.TopSellingItem
import com.rxsoft.mobile.data.repository.ReportsRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DailySalesViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository
) : ViewModel() {

    private val _dailyRows = MutableStateFlow<UiState<List<DailySalesRow>>>(UiState.Loading)
    val dailyRows: StateFlow<UiState<List<DailySalesRow>>> = _dailyRows.asStateFlow()

    private val _topItems = MutableStateFlow<UiState<List<TopSellingItem>>>(UiState.Loading)
    val topItems: StateFlow<UiState<List<TopSellingItem>>> = _topItems.asStateFlow()

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val _fromDate = MutableStateFlow<String?>(null)
    val fromDate: StateFlow<String?> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow<String?>(null)
    val toDate: StateFlow<String?> = _toDate.asStateFlow()

    val fromDisplay: String
        get() = _fromDate.value?.let { LocalDate.parse(it, fmt) }
            ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "All"

    val toDisplay: String
        get() = _toDate.value?.let { LocalDate.parse(it, fmt) }
            ?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "All"

    init {
        // Default to this week (Monday → today).
        val today = LocalDate.now()
        setDateRange(today.with(DayOfWeek.MONDAY), today)
    }

    fun setDateRange(from: LocalDate?, to: LocalDate?) {
        _fromDate.value = from?.format(fmt)
        _toDate.value = to?.format(fmt)
        loadReport()
    }

    fun clearDateRange() {
        _fromDate.value = null
        _toDate.value = null
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            _dailyRows.value = UiState.Loading
            reportsRepository.getDailySales(_fromDate.value, _toDate.value)
                .onSuccess { _dailyRows.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("DailySalesVM", "Failed to load report: ${e.message}", e)
                    _dailyRows.value = UiState.Error(e.message ?: "Failed to load report")
                }
        }
        viewModelScope.launch {
            _topItems.value = UiState.Loading
            reportsRepository.getTopSellingItems()
                .onSuccess { _topItems.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("DailySalesVM", "Failed to load top items: ${e.message}", e)
                    _topItems.value = UiState.Error(e.message ?: "Failed to load top items")
                }
        }
    }
}
