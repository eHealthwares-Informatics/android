package com.ehealthwares.rxsoft.ui.items

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehealthwares.rxsoft.data.remote.dto.CategoryDto
import com.ehealthwares.rxsoft.data.remote.dto.ItemDto
import com.ehealthwares.rxsoft.data.repository.PosRepository
import com.ehealthwares.rxsoft.util.UiState
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

    /** Categories for the filter chip row (failure-tolerant; empty when offline). */
    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    private var currentPage = 1
    private var hasMore = true
    private var currentSearch: String? = null

    init {
        loadItems()
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            posRepository.getCategories(null)
                .onSuccess { _categories.value = it }
                .onFailure { e -> Log.w("ItemListVM", "Categories unavailable: ${e.message}") }
        }
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
                    hasMore = it.size >= 100
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
                    hasMore = items.size >= 100
                }
                .onFailure { e ->
                    Log.e("ItemListVM", "Failed to load more items: ${e.message}", e)
                    currentPage--
                }
            _isLoadingMore.value = false
        }
    }
}
