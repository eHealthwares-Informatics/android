package com.ehealthwares.rxsoft.ui.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.ehealthwares.rxsoft.R

/**
 * Decorative medical-themed backdrop rendered as a VectorDrawable.
 * The drawable uses `@color/art_*` resources that automatically switch
 * between light and dark variants via Android resource qualifiers
 * (`values/colors.xml` vs `values-night/colors.xml`).
 *
 * Place this inside a [androidx.compose.foundation.layout.Box] as the
 * first child so it sits behind the screen content.
 */
@Composable
fun MedicalArtBackdrop(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Image(
        painter = painterResource(R.drawable.bg_medical_art),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds,
        alpha = if (isDark) 0.55f else 0.35f,
    )
}
