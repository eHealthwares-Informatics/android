package com.ehealthwares.rxsoft.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ehealthwares.rxsoft.ui.designsystem.theme.AppearanceMode
import com.ehealthwares.rxsoft.ui.designsystem.theme.RxSoftTheme
import com.ehealthwares.rxsoft.ui.designsystem.theme.ThemeSettings
import com.ehealthwares.rxsoft.ui.designsystem.theme.ThemeViewModel

@Composable
fun RxSoftMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val themeSettings by themeViewModel.themeSettings.collectAsState()

    RxSoftTheme(
        themeSettings = themeSettings,
        isSystemDark = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
