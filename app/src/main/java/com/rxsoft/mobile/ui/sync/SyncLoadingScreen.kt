package com.rxsoft.mobile.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.data.repository.SyncProgress

private fun entityLabel(entity: String?): String = when (entity) {
    "items" -> "Catalog items"
    "priceLists" -> "Price lists"
    "priceListItems" -> "Prices"
    "stockLocations" -> "Stock locations"
    "stockBalances" -> "Stock balances"
    "customers" -> "Customers"
    "categories" -> "Categories"
    "uoms" -> "Units of measure"
    else -> "Data"
}

/**
 * Launch-time gate shown while the local SQLite catalog is being synced.
 * Blocks until the sync finishes or the configured timeout elapses.
 */
@Composable
fun SyncLoadingScreen(
    progress: SyncProgress,
    error: String? = null,
    canContinueOffline: Boolean = false,
    onRetry: () -> Unit = {},
    onContinueOffline: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "RxSoft",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (error == null) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progress.fraction },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Preparing ${entityLabel(progress.entity)}…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = "Couldn't finish syncing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Retry") }
            if (canContinueOffline) {
                TextButton(onClick = onContinueOffline) { Text("Continue offline") }
            }
        }
    }
}
