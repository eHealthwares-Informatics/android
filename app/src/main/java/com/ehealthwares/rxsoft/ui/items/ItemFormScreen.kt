package com.rxsoft.mobile.ui.items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rxsoft.mobile.ui.designsystem.components.AppAlertDialog
import com.rxsoft.mobile.ui.designsystem.components.AppIconButton
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppOutlinedCard
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextField
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    itemId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
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

    if (showCategoryDialog && state.categories.isNotEmpty()) {
        AppAlertDialog(
            title = "Select Category",
            text = state.categories.joinToString("\n") { it.name ?: it.id ?: "" },
            onDismiss = { showCategoryDialog = false },
            onConfirm = { showCategoryDialog = false },
            confirmLabel = "Cancel",
            dismissLabel = "",
        )
    }

    androidx.compose.material3.Scaffold(
        topBar = {
            AppTopAppBar(
                title = if (itemId != null) "Edit Item" else "New Item",
                onBack = onBack,
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

                val catName = state.categories.find { it.id == state.categoryId }?.name ?: state.categoryId
                AppTextField(
                    value = if (state.categoryId.isNotEmpty()) catName else "",
                    onValueChange = {},
                    label = "Category",
                    readOnly = true,
                    enabled = state.categories.isNotEmpty(),
                )

                val uomName = state.uoms.find { it.id == state.baseUomId }?.name ?: state.baseUomId
                AppTextField(
                    value = if (state.baseUomId.isNotEmpty()) uomName else "",
                    onValueChange = {},
                    label = "UOM",
                    readOnly = true,
                    enabled = state.uoms.isNotEmpty(),
                )

                AppTextField(
                    value = state.barcode,
                    onValueChange = viewModel::updateBarcode,
                    label = "Barcode (optional)",
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Done,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Active", style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = state.isActive, onCheckedChange = viewModel::updateIsActive)
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
