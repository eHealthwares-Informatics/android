package com.ehealthwares.rxsoft.ui.purchases

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.remote.dto.CreatePurchaseLine
import com.ehealthwares.rxsoft.data.remote.dto.ItemDto
import com.ehealthwares.rxsoft.data.remote.dto.PartyDto
import com.ehealthwares.rxsoft.data.remote.dto.PurchaseDto
import com.ehealthwares.rxsoft.data.remote.dto.ReceiveGoodsLine
import com.ehealthwares.rxsoft.data.remote.dto.StockLocationDto
import com.ehealthwares.rxsoft.data.remote.dto.UomDto
import com.ehealthwares.rxsoft.data.repository.PurchasesRepository
import com.ehealthwares.rxsoft.util.UiState
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

    /** Lazily-loaded purchase details keyed by id — fetched when an accordion card expands. */
    private val _details = MutableStateFlow<Map<String, UiState<PurchaseDto>>>(emptyMap())
    val details: StateFlow<Map<String, UiState<PurchaseDto>>> = _details.asStateFlow()

    private val _createState = MutableStateFlow<UiState<PurchaseDto>>(UiState.Idle)
    val createState: StateFlow<UiState<PurchaseDto>> = _createState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<PurchaseDto>>(UiState.Idle)
    val actionState: StateFlow<UiState<PurchaseDto>> = _actionState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPage = 1
    private var hasMore = true

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
        currentPage = 1
        hasMore = true
        viewModelScope.launch {
            _purchases.value = UiState.Loading
            purchasesRepository.listPurchases(page = 1)
                .onSuccess {
                    _purchases.value = UiState.Success(it)
                    hasMore = it.size >= 100
                }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to load purchases: ${e.message}", e)
                    _purchases.value = UiState.Error(e.message ?: "Failed to load purchases")
                }
        }
    }

    /** Infinite scroll: append the next page of purchase orders. */
    fun loadMorePurchases() {
        if (_isLoadingMore.value || !hasMore) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            purchasesRepository.listPurchases(page = currentPage)
                .onSuccess { items ->
                    val current = (_purchases.value as? UiState.Success)?.data ?: emptyList()
                    _purchases.value = UiState.Success(current + items)
                    hasMore = items.size >= 100
                }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to load more purchases: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
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

    /** Fetch (and cache) the full purchase for an expanded accordion card. */
    fun loadDetailFor(id: String) {
        if (_details.value[id] is UiState.Loading || _details.value[id] is UiState.Success) return
        viewModelScope.launch {
            _details.value = _details.value + (id to UiState.Loading)
            purchasesRepository.getPurchase(id)
                .onSuccess { purchase ->
                    _details.value = _details.value + (id to UiState.Success(purchase))
                }
                .onFailure { e ->
                    Log.e("PurchasesVM", "Failed to load purchase detail: ${e.message}", e)
                    _details.value = _details.value + (id to UiState.Error(e.message ?: "Failed to load lines"))
                }
        }
    }

    /** Drop a cached detail so a retry or refresh refetches it. */
    fun invalidateDetail(id: String) {
        _details.value = _details.value - id
    }

    fun createPurchase(
        supplierId: String,
        warehouseId: String,
        lines: List<CreatePurchaseLine>,
        note: String? = null,
        invoiceNumber: String? = null,
        status: String? = null,
    ) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            purchasesRepository.createPurchase(supplierId, warehouseId, lines, note, invoiceNumber, status = status)
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
                    _details.value = _details.value - id
                    loadPurchases()
                    // Refresh the expanded card so lines/status visibly update.
                    loadDetailFor(id)
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
                    _details.value = _details.value - purchaseId
                    loadPurchases()
                    if (_detail.value is UiState.Success && _detail.value.let { s -> (s as UiState.Success<PurchaseDto>).data.id } == purchaseId) {
                        loadDetail(purchaseId)
                    }
                    // Refresh the expanded accordion card so received quantities
                    // and the new PO status show up immediately.
                    loadDetailFor(purchaseId)
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
