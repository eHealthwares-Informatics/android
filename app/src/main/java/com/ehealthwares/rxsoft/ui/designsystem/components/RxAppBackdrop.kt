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
 * Which of the RxShop v3 backdrop artworks to show.
 *
 * [Login] — leaf branches in the corners, soft blobs and scattered medical
 * motifs (tablets, capsules, hearts, ECG lines). For splash/auth-like screens.
 * [Pattern] — the same motifs, fainter and evenly spread. For inner screens.
 */
enum class RxBackdropStyle { Login, Pattern }

/**
 * RxShop v3 decorative backdrop rendered as a VectorDrawable
 * (bg_app_backdrop_login / bg_app_backdrop_pattern). Dimmed automatically in
 * dark mode so foreground content stays readable.
 *
 * Place this inside a [androidx.compose.foundation.layout.Box] as the first
 * child so it sits behind the screen content.
 */
@Composable
fun RxAppBackdrop(
    style: RxBackdropStyle = RxBackdropStyle.Pattern,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    Image(
        painter = painterResource(
            if (style == RxBackdropStyle.Login) R.drawable.bg_app_backdrop_login
            else R.drawable.bg_app_backdrop_pattern
        ),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = if (isDark) 0.35f else 0.8f,
    )
}
