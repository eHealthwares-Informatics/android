package com.rxsoft.mobile.ui.designsystem.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rxsoft.mobile.ui.designsystem.components.AppEmptyState
import com.rxsoft.mobile.ui.designsystem.components.AppErrorState
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppPageLoading
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListScreenTemplate(
    title: String,
    state: UiState<List<T>>,
    modifier: Modifier = Modifier,
    isLoadingMore: Boolean = false,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    searchQuery: String = "",
    onSearchQueryChange: ((String) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onLoadMore: (() -> Unit)? = null,
    emptyTitle: String = "Nothing here",
    emptySubtitle: String? = null,
    emptyAction: @Composable (() -> Unit)? = null,
    topBarActions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit) = {},
    fab: @Composable (() -> Unit)? = null,
    configMissing: Boolean = false,
    configMessage: String? = null,
    onListScrollDirection: ((scrollingDown: Boolean) -> Unit)? = null,
    listContent: LazyListScope.(listData: List<T>) -> Unit,
) {
    val listState = rememberLazyListState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    if (onLoadMore != null) {
        LaunchedEffect(listState) {
            snapshotFlow {
                val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisible to listState.layoutInfo.totalItemsCount
            }.collect { (lastVisible, totalItems) ->
                if (totalItems > 0 && lastVisible >= totalItems - 2) {
                    onLoadMore()
                }
            }
        }
    }

    // Report scroll direction so hosts can react (e.g. hide/show a bottom bar):
    // scrolling down hides, scrolling up (or reaching the top) shows.
    if (onListScrollDirection != null) {
        LaunchedEffect(listState) {
            var lastIndex = listState.firstVisibleItemIndex
            var lastOffset = listState.firstVisibleItemScrollOffset
            var accumulated = 0
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                val delta = if (index == lastIndex) offset - lastOffset else (index - lastIndex) * 600
                accumulated += delta
                if (index == 0 && offset == 0) {
                    onListScrollDirection(false)
                    accumulated = 0
                } else if (accumulated > 120) {
                    onListScrollDirection(true)
                    accumulated = 0
                } else if (accumulated < -120) {
                    onListScrollDirection(false)
                    accumulated = 0
                }
                lastIndex = index
                lastOffset = offset
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopAppBar(
                title = title,
                onBack = onBack,
                onMenuClick = onMenuClick,
                actions = topBarActions,
            )
        },
        floatingActionButton = {
            if (fab != null && state is UiState.Success && state.data.isNotEmpty()) {
                fab()
            }
        },
    ) { innerPadding ->
        val body: @Composable () -> Unit = {
            if (configMissing) {
                ErrorList(message = configMessage ?: "POS configuration not available")
            } else when (state) {
                is UiState.Idle, is UiState.Loading -> {
                    if (searchQuery.isEmpty() && onSearchQueryChange != null) {
                        AppPageLoading()
                    } else {
                        AppLoadingState()
                    }
                }
                is UiState.Error -> {
                    ErrorList(
                        message = state.message,
                        onRetry = if (onRefresh != null) {{ onRefresh() }} else null,
                    )
                }
                is UiState.Success -> {
                    val items = state.data
                    if (items.isEmpty() && !isLoadingMore) {
                        EmptyList(
                            title = emptyTitle,
                            subtitle = emptySubtitle,
                            action = emptyAction,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(
                                horizontal = SpacingTokens.screenHorizontal,
                                vertical = SpacingTokens.section,
                            ),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                        ) {
                            listContent(items)
                            if (isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { contentDescription = "Loading more" },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.padding(SpacingTokens.sm),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (onRefresh != null) {
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    onRefresh()
                },
            ) {
                body()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                body()
            }
        }
    }
}

/** Scrollable error state so pull-to-refresh works while an error is shown. */
@Composable
private fun ErrorList(message: String, onRetry: (() -> Unit)? = null) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillParentMaxSize()
                    .padding(horizontal = SpacingTokens.screenHorizontal),
                contentAlignment = Alignment.Center,
            ) {
                AppErrorState(message = message, onRetry = onRetry)
            }
        }
    }
}

/** Scrollable empty state so pull-to-refresh works while the list is empty. */
@Composable
private fun EmptyList(
    title: String,
    subtitle: String?,
    action: @Composable (() -> Unit)? = null,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(
                modifier = Modifier
                    .fillParentMaxSize()
                    .padding(horizontal = SpacingTokens.screenHorizontal),
                contentAlignment = Alignment.Center,
            ) {
                AppEmptyState(
                    title = title,
                    subtitle = subtitle,
                    icon = Icons.Outlined.Inbox,
                    action = action,
                )
            }
        }
    }
}
