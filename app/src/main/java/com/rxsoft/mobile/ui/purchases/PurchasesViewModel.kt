package com.rxsoft.mobile.ui.purchases

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.CreatePurchaseLine
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.data.remote.dto.PartyDto
import com.rxsoft.mobile.data.remote.dto.PurchaseDto
import com.rxsoft.mobile.data.remote.dto.ReceiveGoodsLine
import com.rxsoft.mobile.data.remote.dto.StockLocationDto
import com.rxsoft.mobile.data.remote.dto.UomDto
import com.rxsoft.mobile.data.repository.PurchasesRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class PurchasesViewModel @Inject constructor(
    private val purchasesRepository: PurchasesRepository,
) : ViewModel() {

    private var searchJob: Job? = null

    private val _purchases = MutableStateFlow<UiState<List<PurchaseDto>>>(UiState.Loading)
    val purchases: StateFlow<UiState<List<PurchaseDto>>> = _purchases.asStateFlow()

    private val _detail = MutableStateFlow<UiState<PurchaseDto>>(UiState.Idle)
    val detail: StateFlow<UiState<PurchaseDto>> = _detail.asStateFlow()

    private val _createState = MutableStateFlow<UiState<PurchaseDto>>(UiState.Idle)
    val createState: StateFlow<UiState<PurchaseDto>> = _createState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<PurchaseDto>>(UiState.Idle)
    val actionState: StateFlow<UiState<PurchaseDto>> = _actionState.asStateFlow()

    // Lookups for the create flow
    private val _suppliers = MutableStateFlow<List<PartyDto>>(emptyList())
    val suppliers: StateFlow<List<PartyDto>> = _suppliers.asStateFlow()

    private val _warehouses = MutableStateFlow<List<StockLocationDto>>(emptyList())
    val warehouses: StateFlow<List<StockLocationDto>> = _warehouses.asStateFlow()

    private val _uoms = MutableStateFlow<List<UomDto>>(emptyList())
    val uoms: StateFlow<List<UomDto>> = _uoms.asStateFlow()

    private val _items = MutableStateFlow<List<ItemDto>>(emptyList())
    val items: StateFlow<List<ItemDto>> = _items.asStateFlow()

    private val _searchingItems = MutableStateFlow(false)
    val searchingItems: StateFlow<Boolean> = _searchingItems.asStateFlow()

    init {
        loadPurchases()
        loadLookups()
    }

    fun loadPurchases() {
        viewModelScope.launch {
            _purchases.value = UiState.Loading
            purchasesRepository.listPurchases()
                .onSuccess { _purchases.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to load purchases: ${e.message}", e)
                    _purchases.value = UiState.Error(e.message ?: "Failed to load purchases")
                }
        }
    }

    private fun loadLookups() {
        viewModelScope.launch {
            purchasesRepository.listSuppliers()
                .onSuccess { _suppliers.value = it }
                .onFailure { e -> Log.e("PurchasesVM", "Failed to load suppliers: ${e.message}", e) }
            purchasesRepository.listWarehouses()
                .onSuccess { _warehouses.value = it }
                .onFailure { e -> Log.e("PurchasesVM", "Failed to load warehouses: ${e.message}", e) }
            purchasesRepository.listUoms()
                .onSuccess { _uoms.value = it }
                .onFailure { e -> Log.e("PurchasesVM", "Failed to load uoms: ${e.message}", e) }
        }
    }

    fun searchItems(query: String) {
        val needle = query.trim()
        searchJob?.cancel()
        if (needle.length < 2) {
            _items.value = emptyList()
            _searchingItems.value = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(250)
            _searchingItems.value = true
            purchasesRepository.searchOrgItems(needle)
                .onSuccess { _items.value = it }
                .onFailure { _items.value = emptyList() }
            _searchingItems.value = false
        }
    }

    fun clearItemSearch() {
        _items.value = emptyList()
        _searchingItems.value = false
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _detail.value = UiState.Loading
            purchasesRepository.getPurchase(id)
                .onSuccess { _detail.value = UiState.Success(it) }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to load purchase: ${e.message}", e)
                    _detail.value = UiState.Error(e.message ?: "Failed to load purchase")
                }
        }
    }

    fun clearDetail() {
        _detail.value = UiState.Idle
    }

    fun createPurchase(
        supplierId: String,
        warehouseId: String,
        lines: List<CreatePurchaseLine>,
        note: String? = null,
        invoiceNumber: String? = null,
    ) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            purchasesRepository.createPurchase(supplierId, warehouseId, lines, note, invoiceNumber)
                .onSuccess {
                    _createState.value = UiState.Success(it)
                    loadPurchases()
                }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to create purchase: ${e.message}", e)
                    _createState.value = UiState.Error(e.message ?: "Failed to create purchase")
                }
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }

    fun approvePurchase(id: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            purchasesRepository.approvePurchase(id)
                .onSuccess {
                    _actionState.value = UiState.Success(it)
                    loadPurchases()
                }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to approve purchase: ${e.message}", e)
                    _actionState.value = UiState.Error(e.message ?: "Failed to approve purchase")
                }
        }
    }

    fun receiveGoods(purchaseId: String, lines: List<ReceiveGoodsLine>, note: String? = null) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            purchasesRepository.receiveGoods(purchaseId, lines, note)
                .onSuccess {
                    _actionState.value = UiState.Success(it)
                    loadPurchases()
                    if (_detail.value is UiState.Success && _detail.value.let { s -> (s as UiState.Success<PurchaseDto>).data.id } == purchaseId) {
                        loadDetail(purchaseId)
                    }
                }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to receive goods: ${e.message}", e)
                    _actionState.value = UiState.Error(e.message ?: "Failed to receive goods")
                }
        }
    }

    fun resetActionState() {
        _actionState.value = UiState.Idle
    }

    companion object {
        fun buildLine(
            itemId: String,
            orderedQty: BigDecimal,
            uomId: String,
            unitCost: BigDecimal,
        ): CreatePurchaseLine = CreatePurchaseLine(itemId, orderedQty, uomId, unitCost)

        fun buildReceiveLine(
            itemId: String,
            receivedQty: BigDecimal,
            unitCost: BigDecimal,
            uomId: String,
        ): ReceiveGoodsLine = ReceiveGoodsLine(itemId, receivedQty, unitCost, uomId)
    }
}
