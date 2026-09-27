package com.rxsoft.mobile.ui.designsystem.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val themePreferences = ThemePreferences(application)

    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    init {
        viewModelScope.launch {
            themePreferences.themeSettings.collect { settings ->
                _themeSettings.value = settings
            }
        }
    }

    fun updateAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch {
            themePreferences.updateAppearanceMode(mode)
        }
    }

    fun updateColorTheme(theme: ColorTheme) {
        viewModelScope.launch {
            themePreferences.updateColorTheme(theme)
        }
    }

    fun updateCustomPrimaryColor(color: Long) {
        viewModelScope.launch {
            themePreferences.updateCustomPrimaryColor(color)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            themePreferences.resetToDefaults()
        }
    }
}
