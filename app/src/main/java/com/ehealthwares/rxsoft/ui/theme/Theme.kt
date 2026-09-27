package com.rxsoft.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.ui.designsystem.theme.AppearanceMode
import com.rxsoft.mobile.ui.designsystem.theme.RxSoftTheme
import com.rxsoft.mobile.ui.designsystem.theme.ThemeSettings
import com.rxsoft.mobile.ui.designsystem.theme.ThemeViewModel

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
