package com.rxsoft.mobile.ui.pos

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch
import com.rxsoft.mobile.data.remote.dto.SaleDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.templates.ListScreenTemplate
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.ui.designsystem.theme.AppearanceMode
import com.rxsoft.mobile.ui.designsystem.theme.ThemeViewModel
import com.rxsoft.mobile.util.PosConfigManager
import com.rxsoft.mobile.util.UiState
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PosOrderListScreen(
    posConfigManager: PosConfigManager,
    onNewSale: () -> Unit = {},
    onSaleClick: (String) -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    onListScrollDirection: ((scrollingDown: Boolean) -> Unit)? = null,
    viewModel: PosOrderListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    themeViewModel: ThemeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val salesState by viewModel.sales.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val config by posConfigManager.config.collectAsState()
    val configState by posConfigManager.configState.collectAsState()
    val themeSettings by themeViewModel.themeSettings.collectAsState()
    val scope = rememberCoroutineScope()

    val isDark = when (themeSettings.appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> {
            val config2 = androidx.compose.ui.platform.LocalContext.current.resources.configuration
            val uiMode = config2.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
            uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    }

    ListScreenTemplate(
        title = "Sales",
        state = salesState,
        isLoadingMore = isLoadingMore,
        onMenuClick = onMenuClick,
        onRefresh = { viewModel.loadSales() },
        onLoadMore = { viewModel.loadMoreSales() },
        onListScrollDirection = onListScrollDirection,
        emptyTitle = "No orders yet",
        emptySubtitle = "Create a new sale to get started",
        topBarActions = {
            IconButton(onClick = {
                val newMode = if (isDark) AppearanceMode.LIGHT else AppearanceMode.DARK
                themeViewModel.updateAppearanceMode(newMode)
            }) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        fab = {
            FloatingActionButton(
                onClick = {
                    // Refetch + save the POS config first when the stock location
                    // hasn't been resolved yet, then start the sale.
                    scope.launch {
                        var cfg = posConfigManager.config.value
                        if (cfg?.stockLocation == null) {
                            cfg = posConfigManager.fetchConfig()
                        }
                        if (cfg?.stockLocation != null) {
                            onNewSale()
                        } else {
                            Log.e("PosOrderList", "Cannot start sale: stock location not configured")
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Sale")
            }
        },
        configMissing = configState is UiState.Error || (configState is UiState.Success && config?.stockLocation == null),
        configMessage = when {
            configState is UiState.Error -> (configState as UiState.Error).message
            configState is UiState.Success && config?.stockLocation == null -> "Stock location not configured for this user. Go to Settings to check your POS configuration."
            else -> null
        },
    ) { data ->
        items(data, key = { it.id }) { sale ->
            SaleCard(sale = sale, onClick = { onSaleClick(sale.id) })
        }
    }
}

@Composable
private fun SaleCard(sale: SaleDto, onClick: () -> Unit) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    AppCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(sale.saleNumber, fontWeight = FontWeight.Bold)
            Text(sale.status.uppercase(), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(SpacingTokens.xs))
        Text(sale.customer?.name ?: "Walk-in", style = MaterialTheme.typography.bodyMedium)
        Text(
            format.format(sale.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
