package com.ehealthwares.rxsoft.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.rxsoft.mobile.data.local.CachedItemEntity
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppIconButton
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextField
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

/** Price Adjustment: change an item's unit price within a price list. */
@Composable
fun PriceAdjustmentScreen(
    onBack: () -> Unit,
    viewModel: PriceAdjustmentViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val priceLists by viewModel.priceLists.collectAsState()
    val selectedPriceListId by viewModel.selectedPriceListId.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val currentPrice by viewModel.currentPrice.collectAsState()
    val newPriceText by viewModel.newPriceText.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            kotlinx.coroutines.delay(1500)
            viewModel.resetSubmitState()
        }
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            AppTopAppBar(title = "Price Adjustment", onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(SpacingTokens.screenHorizontal)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            if (priceLists.isNotEmpty()) {
                Column {
                    Text("Price list", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.padding(vertical = SpacingTokens.xxs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                    ) {
                        priceLists.forEach { list ->
                            val id = list.id ?: return@forEach
                            FilterChip(
                                selected = selectedPriceListId == id,
                                onClick = { viewModel.selectPriceList(id) },
                                label = { Text(list.name ?: list.code ?: id) },
                            )
                        }
                    }
                }
            }

            AppTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchItems(it)
                },
                label = "Search item to adjust",
                placeholder = "Search item to adjust",
                trailingIcon = if (searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                onTrailingIconClick = if (searchQuery.isNotEmpty()) {{ searchQuery = ""; viewModel.searchItems("") }} else null,
                trailingIconDescription = "Clear search",
                enabled = selectedPriceListId != null,
            )

            selectedItem?.let { item ->
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.displayName ?: item.name,
                                fontWeight = FontWeight.Medium,
                            )
                            item.code?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            currentPrice?.let { price ->
                                Text(
                                    "Current: ${format.format(price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        AppIconButton(
                            icon = Icons.Default.Clear,
                            onClick = {
                                viewModel.clearSelectedItem()
                                searchQuery = ""
                            },
                            description = "Remove selected item",
                        )
                    }
                }
            }

            if (searchResults !is UiState.Idle && selectedItem == null) {
                when (val results = searchResults) {
                    is UiState.Loading -> {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    is UiState.Error -> {
                        Text(results.message, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {}
                }
            }
            if (searchResults is UiState.Success && selectedItem == null) {
                val items = (searchResults as UiState.Success<List<CachedItemEntity>>).data
                if (items.isNotEmpty()) {
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        tonalElevation = 2.dp,
                    ) {
                        LazyColumn {
                            items(items, key = { it.itemId }) { item ->
                                androidx.compose.material3.ListItem(
                                    headlineContent = { Text(item.displayName ?: item.name) },
                                    supportingContent = item.code?.let { { Text(it) } },
                                    modifier = Modifier.clickable { viewModel.selectItem(item) },
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "No items found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                listOf(5, 10, -5, -10).forEach { percent ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.applyPercent(percent) },
                        label = { Text(if (percent > 0) "+$percent%" else "$percent%") },
                        enabled = selectedItem != null && currentPrice != null,
                    )
                }
            }

            OutlinedTextField(
                value = newPriceText,
                onValueChange = { viewModel.updateNewPrice(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New price") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = selectedItem != null,
            )

            if (submitState is UiState.Error) {
                Text(
                    (submitState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (submitState is UiState.Success) {
                AppCard {
                    Text(
                        "Price Adjusted",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AppPrimaryButton(
                text = if (submitState is UiState.Loading) "Applying..." else "Apply Adjustment",
                onClick = { viewModel.submit() },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedItem != null &&
                    newPriceText.toDoubleOrNull() != null &&
                    submitState !is UiState.Loading,
            )
        }
    }
}
