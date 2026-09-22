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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.data.remote.dto.PriceListDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppEmptyState
import com.rxsoft.mobile.ui.designsystem.components.AppErrorState
import com.rxsoft.mobile.ui.designsystem.components.AppIconButton
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceListScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onOpen: (String) -> Unit = {},
    viewModel: PriceListViewModel = hiltViewModel(),
) {
    val state by viewModel.lists.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreate by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PriceListDto?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

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
        topBar = { AppTopAppBar(title = "Price Lists", onBack = onBack, onMenuClick = onMenuClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "New price list")
            }
        },
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize().padding(padding),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.load()
            },
        ) {
            when (val s = state) {
                is UiState.Loading, is UiState.Idle -> AppLoadingState()
                is UiState.Error -> AppErrorState(message = s.message, onRetry = { viewModel.load() })
                is UiState.Success -> {
                    if (s.data.isEmpty()) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                    AppEmptyState(
                                        title = "No price lists",
                                        subtitle = "Create a price list to get started",
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                        ) {
                            items(
                                s.data,
                                key = { it.id ?: it.code ?: it.hashCode().toString() },
                            ) { pl ->
                                PriceListCard(
                                    list = pl,
                                    onOpen = { pl.id?.let(onOpen) },
                                    onEdit = { editing = pl },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        PriceListFormDialog(
            title = "New price list",
            initial = null,
            onDismiss = { showCreate = false },
            onSave = { code, name, isDefault, _ ->
                viewModel.create(code ?: "", name ?: "", isDefault == true)
                showCreate = false
            },
        )
    }

    editing?.let { list ->
        PriceListFormDialog(
            title = "Edit price list",
            initial = list,
            onDismiss = { editing = null },
            onSave = { code, name, isDefault, isActive ->
                list.id?.let { viewModel.update(it, code, name, isDefault, isActive) }
                editing = null
            },
        )
    }
}

@Composable
private fun PriceListCard(list: PriceListDto, onOpen: () -> Unit, onEdit: () -> Unit) {
    AppCard(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(list.name ?: "-", fontWeight = FontWeight.Medium)
                Text(
                    listOfNotNull(
                        list.code,
                        if (list.isDefault == true) "Default" else null,
                        if (list.isActive == false) "Inactive" else null,
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AppIconButton(icon = Icons.Default.Edit, onClick = onEdit, description = "Edit ${list.name}")
        }
    }
}

@Composable
private fun PriceListFormDialog(
    title: String,
    initial: PriceListDto?,
    onDismiss: () -> Unit,
    onSave: (code: String?, name: String?, isDefault: Boolean?, isActive: Boolean?) -> Unit,
) {
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var isDefault by remember { mutableStateOf(initial?.isDefault ?: false) }
    var isActive by remember { mutableStateOf(initial?.isActive ?: true) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = ShapeTokens.dialog,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("Default")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Text("Active")
                }
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = "Save",
                onClick = {
                    onSave(code.ifBlank { null }, name.ifBlank { null }, isDefault, isActive)
                },
                enabled = code.isNotBlank() && name.isNotBlank(),
            )
        },
        dismissButton = {
            AppTextButton(text = "Cancel", onClick = onDismiss)
        },
    )
}
