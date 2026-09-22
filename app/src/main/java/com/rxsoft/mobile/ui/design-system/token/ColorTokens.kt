package com.rxsoft.mobile.ui.designsystem.token

import androidx.compose.ui.graphics.Color

object BrandColors {
    val blue50 = Color(0xFFE3F2FD)
    val blue100 = Color(0xFFBBDEFB)
    val blue200 = Color(0xFF90CAF9)
    val blue300 = Color(0xFF64B5F6)
    val blue400 = Color(0xFF42A5F5)
    val blue500 = Color(0xFF2196F3)
    val blue600 = Color(0xFF1E88E5)
    val blue700 = Color(0xFF1976D2)
    val blue800 = Color(0xFF1565C0)
    val blue900 = Color(0xFF0D47A1)
    val teal50 = Color(0xFFE0F2F1)
    val teal100 = Color(0xFFB2DFDB)
    val teal200 = Color(0xFF80CBC4)
    val teal300 = Color(0xFF4DB6AC)
    val teal400 = Color(0xFF26A69A)
    val teal500 = Color(0xFF009688)
    val teal600 = Color(0xFF00897B)
    val teal700 = Color(0xFF00796B)
    val teal800 = Color(0xFF00695C)
    val teal900 = Color(0xFF004D40)
    val purple500 = Color(0xFF7B1FA2)
    val purple200 = Color(0xFFCE93D8)
    val cyan400 = Color(0xFF1EC6B5)
    val red700 = Color(0xFFD32F2F)
    val red200 = Color(0xFFEF9A9A)
    val green500 = Color(0xFF4CAF50)
    val orange500 = Color(0xFFFF9800)
    val neutral0 = Color(0xFFFFFFFF)
    val neutral50 = Color(0xFFF5F5F5)
    val neutral100 = Color(0xFFF6F8F8)
    val neutral200 = Color(0xFFF3F5F6)
    val neutral400 = Color(0xFFBDBDBD)
    val neutral600 = Color(0xFF757575)
    val neutral800 = Color(0xFF424242)
    val neutral900 = Color(0xFF212121)
    val neutral950 = Color(0xFF121212)
    val neutralDarkSurface = Color(0xFF1E1E1E)
}

object ColorTokens {
    // Legacy M3 light/dark
    val primaryLight = BrandColors.blue800
    val onPrimaryLight = BrandColors.neutral0
    val primaryContainerLight = BrandColors.blue50
    val onPrimaryContainerLight = BrandColors.blue900
    val secondaryLight = BrandColors.teal600
    val onSecondaryLight = BrandColors.neutral0
    val secondaryContainerLight = BrandColors.teal50
    val onSecondaryContainerLight = BrandColors.teal900
    val tertiaryLight = BrandColors.purple500
    val onTertiaryLight = BrandColors.neutral0
    val tertiaryContainerLight = BrandColors.purple200.copy(alpha = 0.3f)
    val onTertiaryContainerLight = BrandColors.purple500
    val errorLight = BrandColors.red700
    val onErrorLight = BrandColors.neutral0
    val errorContainerLight = BrandColors.red200.copy(alpha = 0.3f)
    val onErrorContainerLight = BrandColors.red700
    val backgroundLight = BrandColors.neutral50
    val onBackgroundLight = BrandColors.neutral900
    val surfaceLight = BrandColors.neutral0
    val onSurfaceLight = BrandColors.neutral900
    val surfaceVariantLight = BrandColors.neutral50
    val onSurfaceVariantLight = BrandColors.neutral600
    val outlineLight = BrandColors.neutral400
    val outlineVariantLight = BrandColors.neutral100
    val inverseSurfaceLight = BrandColors.neutral900
    val inverseOnSurfaceLight = BrandColors.neutral0
    val inversePrimaryLight = BrandColors.blue200
    val scrimLight = BrandColors.neutral900.copy(alpha = 0.32f)

    val primaryDark = BrandColors.blue200
    val onPrimaryDark = BrandColors.blue900
    val primaryContainerDark = BrandColors.blue800
    val onPrimaryContainerDark = BrandColors.blue100
    val secondaryDark = BrandColors.teal200
    val onSecondaryDark = BrandColors.teal900
    val secondaryContainerDark = BrandColors.teal600
    val onSecondaryContainerDark = BrandColors.teal100
    val tertiaryDark = BrandColors.purple200
    val onTertiaryDark = BrandColors.purple500
    val tertiaryContainerDark = BrandColors.purple500.copy(alpha = 0.3f)
    val onTertiaryContainerDark = BrandColors.purple200
    val errorDark = BrandColors.red200
    val onErrorDark = BrandColors.red700
    val errorContainerDark = BrandColors.red700.copy(alpha = 0.3f)
    val onErrorContainerDark = BrandColors.red200
    val backgroundDark = BrandColors.neutral950
    val onBackgroundDark = BrandColors.neutral50
    val surfaceDark = BrandColors.neutralDarkSurface
    val onSurfaceDark = BrandColors.neutral50
    val surfaceVariantDark = BrandColors.neutral800
    val onSurfaceVariantDark = BrandColors.neutral400
    val outlineDark = BrandColors.neutral600
    val outlineVariantDark = BrandColors.neutral800
    val inverseSurfaceDark = BrandColors.neutral50
    val inverseOnSurfaceDark = BrandColors.neutral900
    val inversePrimaryDark = BrandColors.blue800
    val scrimDark = BrandColors.neutral0.copy(alpha = 0.32f)

    // Semantic healthcare (light)
    val success = Color(0xFF16A34A)
    val onSuccess = BrandColors.neutral0
    val successContainer = Color(0xFFDCFCE7)
    val onSuccessContainer = Color(0xFF14532D)
    val warning = Color(0xFFF59E0B)
    val onWarning = BrandColors.neutral0
    val warningContainer = Color(0xFFFEF3C7)
    val onWarningContainer = Color(0xFF78350F)
    val info = Color(0xFF3B82F6)
    val onInfo = BrandColors.neutral0
    val infoContainer = Color(0xFFDBEAFE)
    val onInfoContainer = Color(0xFF1E3A5F)
    val pending = Color(0xFFF59E0B)
    val pendingContainer = Color(0xFFFEF3C7)
    val scheduled = Color(0xFF2563EB)
    val scheduledContainer = Color(0xFFDBEAFE)
    val inProgress = Color(0xFF16A34A)
    val inProgressContainer = Color(0xFFDCFCE7)
    val completed = Color(0xFF6B7280)
    val completedContainer = Color(0xFFF1F5F9)
    val cancelled = Color(0xFF9CA3AF)
    val cancelledContainer = Color(0xFFF1F5F9)
    val critical = Color(0xFFDC2626)
    val criticalContainer = Color(0xFFFEE2E2)
    val onCriticalContainer = Color(0xFF7F1D1D)

    // KPI accents
    val kpiBlue = Color(0xFF1565C0)
    val kpiBlueLight = Color(0xFFDBEAFE)
    val kpiGreen = Color(0xFF16A34A)
    val kpiGreenLight = Color(0xFFDCFCE7)
    val kpiPurple = Color(0xFF7C3AED)
    val kpiPurpleLight = Color(0xFFEDE9FE)
    val kpiOrange = Color(0xFFF97316)
    val kpiOrangeLight = Color(0xFFFFF7ED)

    // Shop (backward compat)
    val shopAccent = BrandColors.cyan400
    val shopBackground = BrandColors.neutral100
    val shopSurfaceVariant = BrandColors.neutral200

    // Notification
    val notificationBadge = Color(0xFFDC2626)
    val onNotificationBadge = BrandColors.neutral0
}
