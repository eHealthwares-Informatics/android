package com.rxsoft.mobile.ui.items

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rxsoft.mobile.data.remote.dto.CategoryDto
import com.rxsoft.mobile.data.remote.dto.CreateItemRequest
import com.rxsoft.mobile.data.remote.dto.ItemDto
import com.rxsoft.mobile.data.remote.dto.PatchItemRequest
import com.rxsoft.mobile.data.remote.dto.UomDto
import com.rxsoft.mobile.data.repository.PosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ItemFormState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingImage: Boolean = false,
    val item: ItemDto? = null,
    val categories: List<CategoryDto> = emptyList(),
    val uoms: List<UomDto> = emptyList(),
    val code: String = "",
    val name: String = "",
    val barcode: String = "",
    val categoryId: String = "",
    val baseUomId: String = "",
    val isActive: Boolean = true,
    val imageUrl: String? = null,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class ItemFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val posRepository: PosRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ItemFormState())
    val state: StateFlow<ItemFormState> = _state.asStateFlow()

    fun load(itemId: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            posRepository.getCategories().onSuccess { cats ->
                _state.value = _state.value.copy(categories = cats)
            }
            posRepository.getUoms().onSuccess { uomList ->
                _state.value = _state.value.copy(uoms = uomList)
            }

            if (itemId != null) {
                posRepository.getItem(itemId)
                    .onSuccess { item ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            item = item,
                            code = item.code ?: "",
                            name = item.name,
                            barcode = item.barcode ?: "",
                            categoryId = item.category?.id ?: "",
                            baseUomId = item.baseUomId ?: "",
                            isActive = true,
                            imageUrl = item.imageUrl
                        )
                    }
                    .onFailure {
                        _state.value = _state.value.copy(isLoading = false, error = it.message)
                    }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun updateCode(value: String) { _state.value = _state.value.copy(code = value) }
    fun updateName(value: String) { _state.value = _state.value.copy(name = value) }
    fun updateBarcode(value: String) { _state.value = _state.value.copy(barcode = value) }
    fun updateCategoryId(value: String) { _state.value = _state.value.copy(categoryId = value) }
    fun updateBaseUomId(value: String) { _state.value = _state.value.copy(baseUomId = value) }
    fun updateIsActive(value: Boolean) { _state.value = _state.value.copy(isActive = value) }

    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isUploadingImage = true)
            val tempFile = withContext(Dispatchers.IO) {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val outFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                outFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                outFile
            }
            if (tempFile == null) {
                _state.value = _state.value.copy(isUploadingImage = false, error = "Could not read image")
                return@launch
            }
            posRepository.uploadImage(tempFile)
                .onSuccess { response ->
                    tempFile.delete()
                    _state.value = _state.value.copy(
                        isUploadingImage = false,
                        imageUrl = response.url
                    )
                }
                .onFailure {
                    tempFile.delete()
                    _state.value = _state.value.copy(
                        isUploadingImage = false,
                        error = it.message ?: "Image upload failed"
                    )
                }
        }
    }

    fun clearImage() {
        _state.value = _state.value.copy(imageUrl = null)
    }

    fun save() {
        val s = _state.value
        val creating = s.item == null
        if (s.name.isBlank() || (creating && (s.categoryId.isBlank() || s.baseUomId.isBlank()))) {
            _state.value = s.copy(error = if (creating) "Name, category, and UOM are required" else "Name is required")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)

            val existing = s.item
            if (existing != null) {
                posRepository.updateItem(
                    existing.id,
                    PatchItemRequest(
                        code = s.code.ifBlank { null },
                        name = s.name,
                        categoryId = s.categoryId,
                        barcode = s.barcode.ifBlank { null },
                        isActive = s.isActive,
                        imageUrl = s.imageUrl
                    )
                ).onSuccess { _state.value = _state.value.copy(isSaving = false, saved = true) }
                 .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
            } else {
                posRepository.createItem(
                    CreateItemRequest(
                        code = s.code.ifBlank { null },
                        name = s.name,
                        categoryId = s.categoryId,
                        baseUomId = s.baseUomId,
                        barcode = s.barcode.ifBlank { null },
                        isActive = s.isActive,
                        imageUrl = s.imageUrl
                    )
                ).onSuccess { _state.value = _state.value.copy(isSaving = false, saved = true) }
                 .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
            }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
