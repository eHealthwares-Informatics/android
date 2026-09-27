package com.rxsoft.mobile.ui.items

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.data.repository.PosRepository
import com.rxsoft.mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemListViewModel @Inject constructor(
    private val posRepository: PosRepository
) : ViewModel() {

    private val _items = MutableStateFlow<UiState<List<ItemDto>>>(UiState.Loading)
    val items: StateFlow<UiState<List<ItemDto>>> = _items.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPage = 1
    private var hasMore = true
    private var currentSearch: String? = null

    init {
        loadItems()
    }

    fun loadItems(search: String? = null) {
        currentSearch = search
        currentPage = 1
        hasMore = true
        viewModelScope.launch {
            _items.value = UiState.Loading
            posRepository.listItems(search = search, page = 1)
                .onSuccess {
                    _items.value = UiState.Success(it)
                    hasMore = it.size >= 20
                }
                .onFailure { e ->
                    Log.e("ItemListVM", "Failed to load items: ${e.message}", e)
                    _items.value = UiState.Error(e.message ?: "Failed to load items")
                }
        }
    }

    fun loadMoreItems() {
        if (_isLoadingMore.value || !hasMore) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++
            posRepository.listItems(search = currentSearch, page = currentPage)
                .onSuccess { items ->
                    val current = (_items.value as? UiState.Success)?.data ?: emptyList()
                    _items.value = UiState.Success(current + items)
                    hasMore = items.size >= 20
                }
                .onFailure { e ->
                    Log.e("ItemListVM", "Failed to load more items: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
        }
    }
}
