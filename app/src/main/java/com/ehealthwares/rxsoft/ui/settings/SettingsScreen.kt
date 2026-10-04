package com.ehealthwares.rxsoft.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ehealthwares.rxsoft.data.local.SyncStateEntity
import com.ehealthwares.rxsoft.util.ServerUrlManager
import com.ehealthwares.rxsoft.ui.designsystem.components.AppOutlinedButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTextButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTopAppBar
import com.ehealthwares.rxsoft.ui.designsystem.token.ShapeTokens
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens

@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    externalVm: SettingsViewModel? = null
) {
    val resolvedVm = externalVm ?: hiltViewModel<SettingsViewModel>()
    val serverUrl by resolvedVm.serverUrl.collectAsState()
    val printerUrl by resolvedVm.printerUrl.collectAsState()
    val discoveryState by resolvedVm.discoveryState.collectAsState()
    val knownAgents by resolvedVm.knownAgents.collectAsState()
    val testPrintState by resolvedVm.testPrintState.collectAsState()
    val serverMode by resolvedVm.serverMode.collectAsState()
    val customServerUrl by resolvedVm.customServerUrl.collectAsState()
    val activeModules by resolvedVm.activeModules.collectAsState()
    val posConfig by resolvedVm.posConfigManager.config.collectAsState()
    val syncTimeoutSeconds by resolvedVm.syncTimeoutSeconds.collectAsState()
    val syncStates by resolvedVm.syncStates.collectAsState()
    val syncProgress by resolvedVm.syncProgress.collectAsState()
    var editingUrl by remember { mutableStateOf(false) }
    var urlInput by remember(customServerUrl) { mutableStateOf(customServerUrl) }
    var editingPrinterUrl by remember { mutableStateOf(false) }
    var printerUrlInput by remember(printerUrl) { mutableStateOf(printerUrl) }
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
                    // Production server switch — custom URL only editable in Custom mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Production server", fontWeight = FontWeight.SemiBold)
                            Text(
                                ServerUrlManager.PRODUCTION_URL,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = serverMode == ServerUrlManager.Mode.PRODUCTION,
                            onCheckedChange = { useProd ->
                                if (useProd) resolvedVm.useProductionServer()
                                else resolvedVm.useCustomServer()
                            },
                        )
                    }

                    Spacer(Modifier.height(SpacingTokens.md))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(SpacingTokens.md))

                    Text("API Base URL", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(SpacingTokens.xxs))
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (serverMode == ServerUrlManager.Mode.CUSTOM) {
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
                                    onClick = { editingUrl = false; urlInput = customServerUrl },
                                )
                                Button(onClick = {
                                    resolvedVm.saveServerUrl(urlInput)
                                    editingUrl = false
                                }) { Text("Save") }
                            }
                        } else {
                            AppTextButton(text = "Edit custom URL", onClick = { editingUrl = true })
                        }
                    }

                    Spacer(Modifier.height(SpacingTokens.lg))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(SpacingTokens.lg))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Printer Server URL", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(SpacingTokens.xxs))
                            Text(
                                text = printerUrl,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        // Auto-discovery: scan the LAN for print-agents.
                        IconButton(
                            onClick = { resolvedVm.discoverPrinters() },
                            enabled = discoveryState !is DiscoveryState.Scanning,
                            modifier = Modifier.semantics {
                                contentDescription = "Auto discover printers"
                            },
                        ) {
                            if (discoveryState is DiscoveryState.Scanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(SpacingTokens.xl),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    DiscoveryResults(
                        state = discoveryState,
                        onSelect = {
                            resolvedVm.selectDiscoveredPrinter(it)
                            printerUrlInput = it.url
                            resolvedVm.resetDiscovery()
                        },
                        onRetry = { resolvedVm.discoverPrinters() },
                        onDismiss = { resolvedVm.resetDiscovery() },
                    )

                    Spacer(Modifier.height(SpacingTokens.sm))
                    if (editingPrinterUrl) {
                        OutlinedTextField(
                            value = printerUrlInput,
                            onValueChange = { printerUrlInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("http://192.168.1.100:8094") },
                            shape = ShapeTokens.md,
                        )
                        Spacer(Modifier.height(SpacingTokens.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                            AppOutlinedButton(
                                text = "Test",
                                onClick = { resolvedVm.testPrinterUrl() },
                            )
                            AppOutlinedButton(
                                text = "Cancel",
                                onClick = { editingPrinterUrl = false; printerUrlInput = printerUrl },
                            )
                            Button(onClick = {
                                resolvedVm.savePrinterUrl(printerUrlInput)
                                editingPrinterUrl = false
                            }) { Text("Save") }
                        }
                    } else {
                        AppTextButton(text = "Edit printer URL", onClick = { editingPrinterUrl = true })
                    }

                    Spacer(Modifier.height(SpacingTokens.sm))
                    TestPrintSection(
                        state = testPrintState,
                        onPrint = { resolvedVm.printTestPage() },
                        onDismiss = { resolvedVm.resetTestPrint() },
                    )

                    if (knownAgents.isNotEmpty()) {
                        Spacer(Modifier.height(SpacingTokens.md))
                        Text(
                            "Previously found",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(SpacingTokens.xxs))
                        knownAgents.forEach { agent ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { resolvedVm.savePrinterUrl(agent.url) }
                                    .padding(vertical = SpacingTokens.xs)
                                    .semantics { contentDescription = "Use printer ${agent.hostname}" },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Print,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.width(SpacingTokens.sm))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(agent.hostname, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        agent.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
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
private fun TestPrintSection(
    state: TestPrintState,
    onPrint: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onPrint,
            enabled = state !is TestPrintState.Printing,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Print test page" },
        ) {
            if (state is TestPrintState.Printing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.width(SpacingTokens.sm))
                Text("Printing…")
            } else {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(SpacingTokens.sm))
                Text("Print test page")
            }
        }

        when (state) {
            is TestPrintState.Success -> {
                Spacer(Modifier.height(SpacingTokens.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(SpacingTokens.sm))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            is TestPrintState.Failure -> {
                Spacer(Modifier.height(SpacingTokens.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(SpacingTokens.sm))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                AppTextButton(text = "Dismiss", onClick = onDismiss)
            }

            else -> Unit
        }
    }
}

@Composable
private fun DiscoveryResults(
    state: DiscoveryState,
    onSelect: (DiscoveredPrinter) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (state) {
        is DiscoveryState.Idle -> Unit

        is DiscoveryState.Scanning -> {
            Spacer(Modifier.height(SpacingTokens.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(SpacingTokens.lg), strokeWidth = 2.dp)
                Spacer(Modifier.width(SpacingTokens.sm))
                Text(
                    "Searching this network…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        is DiscoveryState.Error -> {
            Spacer(Modifier.height(SpacingTokens.sm))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = ShapeTokens.lg,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.md)) {
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Spacer(Modifier.height(SpacingTokens.xs))
                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                        AppTextButton(text = "Retry", onClick = onRetry)
                        AppTextButton(text = "Dismiss", onClick = onDismiss)
                    }
                }
            }
        }

        is DiscoveryState.Found -> {
            Spacer(Modifier.height(SpacingTokens.sm))
            Text(
                "${state.printers.size} printer server(s) found",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(SpacingTokens.xxs))
            state.printers.forEach { printer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SpacingTokens.xxs)
                        .clickable { onSelect(printer) }
                        .semantics { contentDescription = "Use printer at ${printer.ip}" },
                    shape = ShapeTokens.lg,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpacingTokens.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Print,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(SpacingTokens.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                printer.info.hostname.ifBlank { printer.ip },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                printer.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "${printer.info.printer} · ${printer.info.width} col",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(SpacingTokens.xxs))
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                AppTextButton(text = "Scan again", onClick = onRetry)
                AppTextButton(text = "Dismiss", onClick = onDismiss)
            }
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
