package com.rxsoft.mobile.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppIconButton
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppSearchBar
import com.rxsoft.mobile.ui.designsystem.components.AppTextField
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import java.math.BigDecimal

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentScreen(
    onBack: () -> Unit,
    viewModel: StockAdjustmentViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val deltaQuantity by viewModel.deltaQuantity.collectAsState()
    val reason by viewModel.reason.collectAsState()
    val submitState by viewModel.submitState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            kotlinx.coroutines.delay(1500)
            viewModel.reset()
        }
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            AppTopAppBar(title = "Stock Adjustment", onBack = onBack)
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
            )

            selectedItem?.let { item ->
                AppCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "${item.name} (${item.code ?: item.id})",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        AppIconButton(
                            icon = Icons.Default.Clear,
                            onClick = { viewModel.selectItem(item) },
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
                val data = (searchResults as UiState.Success<*>).data
                if (data is List<*> && data.isNotEmpty()) {
                    @Suppress("UNCHECKED_CAST")
                    val items = data as List<ItemDto>
                    androidx.compose.material3.Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        tonalElevation = 2.dp,
                    ) {
                        LazyColumn {
                            items(items, key = { it.id }) { item ->
                                androidx.compose.material3.ListItem(
                                    headlineContent = { Text(item.name) },
                                    supportingContent = item.code?.let { { Text(it) } },
                                    modifier = Modifier.clickable { viewModel.selectItem(item) },
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppTextField(
                    value = deltaQuantity.toPlainString(),
                    onValueChange = { viewModel.updateDeltaQuantity(it) },
                    modifier = Modifier.weight(1f),
                    label = "Quantity",
                    placeholder = "e.g. 10 or -5",
                    keyboardType = KeyboardType.Decimal,
                    enabled = selectedItem != null,
                )
                AppIconButton(
                    icon = Icons.Default.Add,
                    onClick = { viewModel.updateDeltaQuantity("1") },
                )
                AppIconButton(
                    icon = Icons.Default.Remove,
                    onClick = { viewModel.updateDeltaQuantity("-1") },
                )
            }

            AppTextField(
                value = reason,
                onValueChange = { viewModel.updateReason(it) },
                label = "Reason",
                placeholder = "e.g. Damaged goods, Stock count correction",
                singleLine = false,
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
                        "Adjustment Applied",
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
                enabled = selectedItem != null && reason.isNotBlank() && deltaQuantity.compareTo(BigDecimal.ZERO) != 0 && submitState !is UiState.Loading,
            )
        }
    }
}
