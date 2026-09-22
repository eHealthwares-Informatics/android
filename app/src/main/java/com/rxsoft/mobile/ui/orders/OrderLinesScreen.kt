package com.rxsoft.mobile.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.templates.ListScreenTemplate
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import java.text.NumberFormat
import java.util.Locale

/** Order Lines: every order line item, each card prefixed with its parent order. */
@Composable
fun OrderLinesScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    viewModel: OrderLinesViewModel = hiltViewModel(),
) {
    val linesState by viewModel.lines.collectAsState()

    ListScreenTemplate(
        title = "Order Lines",
        state = linesState,
        onBack = onBack,
        onMenuClick = onMenuClick,
        onRefresh = { viewModel.loadLines() },
        emptyTitle = "No order lines",
        emptySubtitle = "Line items will appear here once orders contain items",
        listContent = { lines ->
            items(lines, key = { lineRowKey(it) }) { line ->
                OrderLineCard(line)
            }
        },
    )
}

private fun lineRowKey(line: OrderLineRow): String =
    "${line.orderId}:${line.lineId}"

@Composable
private fun OrderLineCard(line: OrderLineRow) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    val date = remember(line.createdAt) { formatOrderLineDate(line.createdAt) }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(line.itemName, fontWeight = FontWeight.Medium)
                Text(
                    listOfNotNull(
                        line.orderNumber,
                        date,
                        line.orderStatus.uppercase(),
                        line.kind,
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(format.format(line.lineTotal), fontWeight = FontWeight.Bold)
        }
        Text(
            "Qty ${line.quantity} × ${format.format(line.unitPrice)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val orderLineDateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")

private fun formatOrderLineDate(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        java.time.Instant.parse(raw)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(orderLineDateFormatter)
    } catch (e: Exception) {
        raw.take(10).takeIf { it.length == 10 }
    }
}