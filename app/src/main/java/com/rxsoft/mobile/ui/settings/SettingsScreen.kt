package com.rxsoft.mobile.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.data.local.SyncStateEntity
import com.rxsoft.mobile.ui.designsystem.components.AppOutlinedButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    externalVm: SettingsViewModel? = null
) {
    val resolvedVm = externalVm ?: hiltViewModel<SettingsViewModel>()
    val serverUrl by resolvedVm.serverUrl.collectAsState()
    val activeModules by resolvedVm.activeModules.collectAsState()
    val posConfig by resolvedVm.posConfigManager.config.collectAsState()
    val syncTimeoutSeconds by resolvedVm.syncTimeoutSeconds.collectAsState()
    val syncStates by resolvedVm.syncStates.collectAsState()
    val syncProgress by resolvedVm.syncProgress.collectAsState()
    var editingUrl by remember { mutableStateOf(false) }
    var urlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            shape = ShapeTokens.dialog,
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                AppTextButton(
                    text = "Sign Out",
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                )
            },
            dismissButton = {
                AppTextButton(
                    text = "Cancel",
                    onClick = { showSignOutDialog = false },
                )
            },
        )
    }

    Scaffold(
        topBar = {
            AppTopAppBar(title = "Settings", onMenuClick = onMenuClick)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SpacingTokens.xl)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(SpacingTokens.lg))

            Text("Modules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SpacingTokens.md))

            ModuleToggleCard(
                title = AppModule.POS.title,
                description = AppModule.POS.description,
                icon = Icons.Outlined.PointOfSale,
                isActive = activeModules.contains(AppModule.POS),
                onToggle = { resolvedVm.toggleModule(AppModule.POS) }
            )
            Spacer(Modifier.height(SpacingTokens.sm))
            ModuleToggleCard(
                title = AppModule.SHOP.title,
                description = AppModule.SHOP.description,
                icon = Icons.Outlined.Medication,
                isActive = activeModules.contains(AppModule.SHOP),
                onToggle = { resolvedVm.toggleModule(AppModule.SHOP) }
            )
            Spacer(Modifier.height(SpacingTokens.sm))
            ModuleToggleCard(
                title = AppModule.INVENTORY.title,
                description = AppModule.INVENTORY.description,
                icon = Icons.Outlined.Inventory2,
                isActive = activeModules.contains(AppModule.INVENTORY),
                onToggle = { resolvedVm.toggleModule(AppModule.INVENTORY) }
            )
            Spacer(Modifier.height(SpacingTokens.sm))
            ModuleToggleCard(
                title = AppModule.SALES.title,
                description = AppModule.SALES.description,
                icon = Icons.Outlined.BarChart,
                isActive = activeModules.contains(AppModule.SALES),
                onToggle = { resolvedVm.toggleModule(AppModule.SALES) }
            )

            Spacer(Modifier.height(SpacingTokens.xxxl))
            Text("POS Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SpacingTokens.md))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeTokens.xl,
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.xl)) {
                    Text("Stock Location", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    Text(
                        text = posConfig?.stockLocation?.name ?: "Not configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (posConfig?.stockLocation != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.error,
                    )
                    if (posConfig?.storeId != null) {
                        Spacer(Modifier.height(SpacingTokens.sm))
                        Text("Store ID", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(SpacingTokens.xxs))
                        Text(
                            text = posConfig!!.storeId!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(SpacingTokens.xxxl))
            Text("Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SpacingTokens.md))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeTokens.xl,
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.xl)) {
                    Text("Startup sync timeout", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    Text(
                        "How long the launch screen waits for the catalog sync before continuing offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(SpacingTokens.sm))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                    ) {
                        AppOutlinedButton(
                            text = "-5s",
                            onClick = { resolvedVm.setSyncTimeoutSeconds(syncTimeoutSeconds - 5) },
                        )
                        Text(
                            "$syncTimeoutSeconds s",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        AppOutlinedButton(
                            text = "+5s",
                            onClick = { resolvedVm.setSyncTimeoutSeconds(syncTimeoutSeconds + 5) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(SpacingTokens.xxxl))
            Text("Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SpacingTokens.md))
            SyncSection(
                states = syncStates,
                isSyncing = syncProgress.running,
                onSyncNow = { resolvedVm.syncNow() },
            )

            Spacer(Modifier.height(SpacingTokens.xxxl))
            Text("Server", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(SpacingTokens.md))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeTokens.xl,
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.xl)) {
                    Text("API Base URL", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(SpacingTokens.sm))
                    if (editingUrl) {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = ShapeTokens.md,
                        )
                        Spacer(Modifier.height(SpacingTokens.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                            AppOutlinedButton(
                                text = "Cancel",
                                onClick = { editingUrl = false; urlInput = serverUrl },
                            )
                            Button(onClick = {
                                resolvedVm.saveServerUrl(urlInput)
                                editingUrl = false
                            }) { Text("Save") }
                        }
                    } else {
                        Text(
                            text = serverUrl,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(SpacingTokens.sm))
                        AppTextButton(text = "Edit", onClick = { editingUrl = true })
                    }
                }
            }

            Spacer(Modifier.height(SpacingTokens.xxxl))
            Button(
                onClick = { showSignOutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out")
            }

            Spacer(Modifier.height(SpacingTokens.xxxl))
        }
    }
}

@Composable
private fun ModuleToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .semantics { contentDescription = "$title, toggle" },
        shape = ShapeTokens.xl,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(SpacingTokens.lg))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = isActive, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun SyncSection(
    states: List<SyncStateEntity>,
    isSyncing: Boolean,
    onSyncNow: () -> Unit,
) {
    val byKey = states.associateBy { it.entity }
    val keys = listOf(
        "items", "priceLists", "priceListItems", "stockLocations",
        "stockBalances", "customers", "categories", "uoms",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeTokens.xl,
    ) {
        Column(modifier = Modifier.padding(SpacingTokens.xl)) {
            Text("Offline catalog", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(SpacingTokens.sm))
            keys.forEach { key ->
                val lastSync = byKey[key]?.lastSyncAt
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.xxs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(syncEntityLabel(key), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (lastSync != null && lastSync > 0) formatSyncTime(lastSync) else "Never",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(SpacingTokens.md))
            Button(
                onClick = onSyncNow,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSyncing) "Syncing…" else "Sync now")
            }
        }
    }
}

private fun syncEntityLabel(key: String): String = when (key) {
    "items" -> "Items"
    "priceLists" -> "Price lists"
    "priceListItems" -> "Prices"
    "stockLocations" -> "Stock locations"
    "stockBalances" -> "Stock balances"
    "customers" -> "Customers"
    "categories" -> "Categories"
    "uoms" -> "Units of measure"
    else -> key
}

private fun formatSyncTime(millis: Long): String {
    if (millis <= 0) return "Never"
    val fmt = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return fmt.format(java.util.Date(millis))
}
