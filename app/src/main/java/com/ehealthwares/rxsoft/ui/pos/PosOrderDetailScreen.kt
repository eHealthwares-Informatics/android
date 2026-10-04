package com.ehealthwares.rxsoft.ui.pos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.ui.designsystem.components.AppCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AppErrorState
import com.ehealthwares.rxsoft.ui.designsystem.components.AppLoadingState
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTopAppBar
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PosOrderDetailScreen(
    saleId: String,
    onBack: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    viewModel: PosOrderDetailViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.sale.collectAsState()
    val reprint by viewModel.reprint.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saleId) {
        viewModel.loadSale(saleId)
    }

    // Surface reprint results as a snackbar.
    LaunchedEffect(reprint) {
        when (reprint) {
            is ReprintState.Success -> {
                snackbarHostState.showSnackbar((reprint as ReprintState.Success).message)
                viewModel.resetReprint()
            }
            is ReprintState.Failure -> {
                snackbarHostState.showSnackbar((reprint as ReprintState.Failure).message)
                viewModel.resetReprint()
            }
            else -> Unit
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopAppBar(
                title = "Order Detail",
                onBack = onBack,
                onMenuClick = onMenuClick,
                actions = {
                    if (reprint is ReprintState.Printing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = SpacingTokens.sm)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.printSale() },
                            modifier = Modifier.semantics { contentDescription = "Print order" },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Print,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPad ->
        when (state) {
            is UiState.Loading, is UiState.Idle -> {
                AppLoadingState(modifier = Modifier.fillMaxSize().padding(innerPad))
            }
            is UiState.Error -> {
                AppErrorState(
                    message = (state as UiState.Error).message,
                    onRetry = { viewModel.loadSale(saleId) },
                    modifier = Modifier.fillMaxSize().padding(innerPad),
                )
            }
            is UiState.Success -> {
                val sale = (state as UiState.Success<*>).data as com.ehealthwares.rxsoft.data.remote.dto.SaleDto
                val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPad),
                    contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    item {
                        AppCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(sale.saleNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(sale.status.uppercase(), style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(SpacingTokens.sm))
                            Text("Customer: ${sale.customer?.name ?: "Walk-in"}", style = MaterialTheme.typography.bodyMedium)
                            Text("Date: ${sale.saleDate}", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    item {
                        Text("Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    if (sale.lines != null) {
                        items(sale.lines, key = { it.id ?: it.lineNumber.toString() }) { line ->
                            AppCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(line.item?.name ?: "Item", fontWeight = FontWeight.Medium)
                                        Text("${line.quantity} x ${format.format(line.unitPrice)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(format.format(line.lineTotal), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(SpacingTokens.sm))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(SpacingTokens.sm))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(format.format(sale.totalAmount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
