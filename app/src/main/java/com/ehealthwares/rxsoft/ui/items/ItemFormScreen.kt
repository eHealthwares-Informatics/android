package com.ehealthwares.rxsoft.ui.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ehealthwares.rxsoft.ui.designsystem.components.AppAlertDialog
import com.ehealthwares.rxsoft.ui.designsystem.components.AppIconButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppLoadingState
import com.ehealthwares.rxsoft.ui.designsystem.components.AppOutlinedCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AppPrimaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTextField
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTopAppBar
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    itemId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    viewModel: ItemFormViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showUomDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadImage(it) }
    }

    LaunchedEffect(itemId) {
        viewModel.load(itemId)
    }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    if (showCategoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    AppSearchBar(
                        query = state.categoryQuery,
                        onQueryChange = viewModel::updateCategoryQuery,
                        placeholder = "Search categories",
                        searchDescription = "Search categories",
                        modifier = Modifier.padding(bottom = SpacingTokens.sm),
                    )
                    when {
                        state.refError != null -> EmptyListMessage(
                            message = state.refError,
                            onRetry = { viewModel.openCategoryDialog() },
                        )
                        state.categories.isEmpty() -> EmptyListMessage(
                            message = if (state.categoryQuery.isBlank()) "Loading categories…" else "No categories match \"${state.categoryQuery}\"",
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                        ) {
                            items(state.categories, key = { it.id ?: it.name ?: "" }) { cat ->
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        viewModel.selectCategory(cat.id ?: cat.name ?: "")
                                        showCategoryDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = cat.name ?: cat.id ?: "",
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showUomDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showUomDialog = false },
            title = { Text("Select UOM") },
            text = {
                Column {
                    AppSearchBar(
                        query = state.uomQuery,
                        onQueryChange = viewModel::updateUomQuery,
                        placeholder = "Search UOMs",
                        searchDescription = "Search UOMs",
                        modifier = Modifier.padding(bottom = SpacingTokens.sm),
                    )
                    when {
                        state.refError != null -> EmptyListMessage(
                            message = state.refError,
                            onRetry = { viewModel.openUomDialog() },
                        )
                        state.uoms.isEmpty() -> EmptyListMessage(
                            message = if (state.uomQuery.isBlank()) "Loading UOMs…" else "No UOMs match \"${state.uomQuery}\"",
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                        ) {
                            items(state.uoms, key = { it.id }) { uom ->
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        viewModel.selectUom(uom.id)
                                        showUomDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = uom.name,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showUomDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            AppTopAppBar(
                title = if (itemId != null) "Edit Item" else "New Item",
                onBack = onBack,
                onMenuClick = onMenuClick,
            )
        },
    ) { padding ->
        if (state.isLoading) {
            AppLoadingState(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(SpacingTokens.screenHorizontal)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
            ) {
                // Image section
                if (state.imageUrl != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Item image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                        AppIconButton(
                            icon = Icons.Default.Clear,
                            onClick = { viewModel.clearImage() },
                            description = "Remove image",
                        )
                    }
                } else {
                    AppOutlinedCard(
                        onClick = { imagePickerLauncher.launch("image/*") },
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (state.isUploadingImage) {
                                androidx.compose.material3.CircularProgressIndicator()
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    androidx.compose.material3.Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                    )
                                    Spacer(modifier = Modifier.height(SpacingTokens.xs))
                                    Text("Add Image", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                AppTextField(
                    value = state.code,
                    onValueChange = viewModel::updateCode,
                    label = "Code (optional)",
                )

                AppTextField(
                    value = state.name,
                    onValueChange = viewModel::updateName,
                    label = "Name",
                )

                // Category: typing searches the server live; picking from the
                // suggestions (or the dialog) sets the actual id.
                val catName = state.categories.find { it.id == state.categoryId }?.name
                val categoryText = state.categoryQuery.ifBlank { catName ?: state.categoryId }
                val showCategorySuggestions = state.categoryQuery.isNotBlank() &&
                    state.categories.isNotEmpty()
                AppTextField(
                    value = categoryText,
                    onValueChange = viewModel::updateCategoryQuery,
                    label = "Category",
                    trailingIcon = Icons.Default.ArrowDropDown,
                    onTrailingIconClick = {
                        viewModel.openCategoryDialog()
                        showCategoryDialog = true
                    },
                    isError = state.categoryId.isBlank() && state.categoryQuery.isNotBlank() &&
                        state.categories.isEmpty(),
                )
                if (showCategorySuggestions) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        state.categories.forEach { cat ->
                            Text(
                                text = cat.name ?: cat.id ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        viewModel.selectCategory(cat.id ?: cat.name ?: "")
                                    }
                                    .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs),
                            )
                        }
                    }
                }

                // UOM: pick from the searchable dialog — fills base, purchase & sale.
                val uomName = state.uoms.find { it.id == state.baseUomId }?.name
                AppTextField(
                    value = state.baseUomId.takeIf { it.isNotBlank() }?.let { uomName ?: it }.orEmpty(),
                    onValueChange = { },
                    label = "UOM (sets base, purchase & sale)",
                    readOnly = true,
                    trailingIcon = Icons.Default.ArrowDropDown,
                    onTrailingIconClick = {
                        viewModel.openUomDialog()
                        showUomDialog = true
                    },
                )

                AppTextField(
                    value = state.alias,
                    onValueChange = viewModel::updateAlias,
                    label = "Alias (optional)",
                )

                AppTextField(
                    value = state.genericProductCode,
                    onValueChange = viewModel::updateGenericProductCode,
                    label = "Generic Product Code (optional)",
                )

                AppTextField(
                    value = state.genericDrugCode,
                    onValueChange = viewModel::updateGenericDrugCode,
                    label = "Generic Drug Code (optional)",
                )

                AppTextField(
                    value = state.barcode,
                    onValueChange = viewModel::updateBarcode,
                    label = "Barcode (optional)",
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next,
                )

                AppTextField(
                    value = state.shelfLifeDays,
                    onValueChange = viewModel::updateShelfLifeDays,
                    label = "Shelf Life Days (optional)",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Active", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = state.isActive, onCheckedChange = viewModel::updateIsActive)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Track Lot", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = state.trackLot, onCheckedChange = viewModel::updateTrackLot)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Track Expiry", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = state.trackExpiry, onCheckedChange = viewModel::updateTrackExpiry)
                }

                state.error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    LaunchedEffect(it) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.clearError()
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.lg))

                AppPrimaryButton(
                    text = if (state.isSaving) "Saving..." else "Save",
                    onClick = viewModel::save,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving && !state.isUploadingImage &&
                        state.name.isNotBlank() &&
                        (state.item != null || (state.categoryId.isNotBlank() && state.baseUomId.isNotBlank())),
                )
            }
        }
    }
}

/** Message shown inside a picker dialog when the list is empty or failed. */
@Composable
private fun EmptyListMessage(
    message: String?,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = SpacingTokens.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message ?: "Nothing found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
