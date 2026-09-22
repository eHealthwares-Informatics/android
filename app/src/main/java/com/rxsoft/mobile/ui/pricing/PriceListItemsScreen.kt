package com.rxsoft.mobile.ui.pricing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.data.remote.dto.PriceListItemDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppEmptyState
import com.rxsoft.mobile.ui.designsystem.components.AppErrorState
import com.rxsoft.mobile.ui.designsystem.components.AppIconButton
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppSearchBar
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListItemsScreen(
    priceListId: String,
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    viewModel: PriceListItemsViewModel = hiltViewModel(),
) {
    val state by viewModel.items.collectAsState()
    val search by viewModel.search.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var editing by remember { mutableStateOf<PriceListItemDto?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    LaunchedEffect(priceListId) { viewModel.load(priceListId) }
    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { AppTopAppBar(title = "Prices", onBack = onBack, onMenuClick = onMenuClick) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            AppSearchBar(
                query = search,
                onQueryChange = { viewModel.updateSearch(it) },
                modifier = Modifier.padding(
                    horizontal = SpacingTokens.screenHorizontal,
                    vertical = SpacingTokens.sm,
                ),
                placeholder = "Search items",
                searchDescription = "Search items",
            )

            PullToRefreshBox(
                modifier = Modifier.weight(1f),
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.load(priceListId)
                },
            ) {
                when (val s = state) {
                    is UiState.Loading, is UiState.Idle -> AppLoadingState()
                    is UiState.Error -> AppErrorState(message = s.message, onRetry = { viewModel.load(priceListId) })
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                        AppEmptyState(title = "No prices", subtitle = "No price entries for this list")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                            ) {
                                items(s.data, key = { it.id ?: it.hashCode().toString() }) { item ->
                                    PriceItemRow(
                                        item = item,
                                        formattedPrice = item.unitPrice?.let { format.format(it) } ?: "-",
                                        onEdit = { editing = item },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { item ->
        EditPriceDialog(
            item = item,
            onDismiss = { editing = null },
            onSave = { price ->
                viewModel.savePrice(item, price)
                editing = null
            },
        )
    }
}

@Composable
private fun PriceItemRow(
    item: PriceListItemDto,
    formattedPrice: String,
    onEdit: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.item?.name ?: "Item", fontWeight = FontWeight.Medium)
                Text(
                    listOfNotNull(item.item?.code, item.currencyCode).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(formattedPrice, fontWeight = FontWeight.Bold)
            AppIconButton(icon = Icons.Default.Edit, onClick = onEdit, description = "Edit price")
        }
    }
}

@Composable
private fun EditPriceDialog(
    item: PriceListItemDto,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit,
) {
    var priceText by remember { mutableStateOf(item.unitPrice?.toPlainString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ShapeTokens.dialog,
        title = { Text(item.item?.name ?: "Edit price") },
        text = {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Unit price") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            AppPrimaryButton(
                text = "Save",
                onClick = { priceText.toDoubleOrNull()?.let(onSave) },
                enabled = priceText.toDoubleOrNull() != null,
            )
        },
        dismissButton = {
            AppTextButton(text = "Cancel", onClick = onDismiss)
        },
    )
}
