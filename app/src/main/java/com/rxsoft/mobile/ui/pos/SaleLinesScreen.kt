package com.rxsoft.mobile.ui.pos

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
import java.text.NumberFormat
import java.util.Locale

/** Sales Lines: every POS sale line item, each card prefixed with its parent sale. */
@Composable
fun SaleLinesScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    viewModel: SaleLinesViewModel = hiltViewModel(),
) {
    val linesState by viewModel.lines.collectAsState()

    ListScreenTemplate(
        title = "Sales Lines",
        state = linesState,
        onBack = onBack,
        onMenuClick = onMenuClick,
        onRefresh = { viewModel.loadLines() },
        emptyTitle = "No sale lines",
        emptySubtitle = "Line items will appear here once sales are made",
        listContent = { lines ->
            items(lines, key = { saleLineRowKey(it) }) { line ->
                SaleLineCard(line)
            }
        },
    )
}

private fun saleLineRowKey(line: SaleLineRow): String =
    "${line.saleId}:${line.lineId ?: "line${line.lineNumber}"}"

@Composable
private fun SaleLineCard(line: SaleLineRow) {
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }
    val date = remember(line.saleDate) { formatSaleLineDate(line.saleDate) }

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
                        line.saleNumber,
                        date,
                        line.saleChannel.uppercase(),
                        line.status.uppercase(),
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

private val saleLineDateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")

private fun formatSaleLineDate(raw: String?): String? {
    if (raw.isNullOrBlank()) return null
    return try {
        java.time.Instant.parse(raw)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(saleLineDateFormatter)
    } catch (e: Exception) {
        raw.take(10).takeIf { it.length == 10 }
    }
}