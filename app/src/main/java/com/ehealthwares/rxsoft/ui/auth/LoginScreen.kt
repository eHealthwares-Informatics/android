package com.ehealthwares.rxsoft.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.R
import com.ehealthwares.rxsoft.BuildConfig
import com.ehealthwares.rxsoft.ui.designsystem.components.AppFilterChip
import com.ehealthwares.rxsoft.ui.designsystem.components.AppLoadingState
import com.ehealthwares.rxsoft.ui.designsystem.components.AppPrimaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTextField
import com.ehealthwares.rxsoft.ui.designsystem.token.ShapeTokens
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.ServerUrlManager
import com.ehealthwares.rxsoft.util.UiState
import androidx.compose.foundation.layout.widthIn
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val colors = MaterialTheme.colorScheme
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("password") }
    var passwordVisible by remember { mutableStateOf(false) }
    var urlChanged by remember { mutableStateOf(false) }
    var showUrlSaved by remember { mutableStateOf(false) }
    // Server config is hidden behind an easter egg: tap the logo 5x to reveal.
    var showServerConfig by remember { mutableStateOf(false) }
    var logoTapCount by remember { mutableStateOf(0) }
    val loginState by viewModel.loginState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val serverMode by viewModel.serverMode.collectAsState()

    // ── Social sign-in state ───────────────────────────────────────
    val socialContext = LocalContext.current
    val socialScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(socialContext) }
    var socialBusy by remember { mutableStateOf(false) }

    val onGoogleTap: () -> Unit = {
        if (!socialBusy) {
            socialBusy = true
            socialScope.launch {
                try {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        // Audience of the returned ID token — the backend
                        // verifies `aud` against this value.
                        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val result = runCatching {
                        credentialManager.getCredential(socialContext, request)
                    }.getOrElse { e ->
                        // Derive flow can report no candidate even when
                        // accounts exist; fall back to the explicit button
                        // flow (launches its own account chooser).
                        if (e is NoCredentialException) {
                            val explicit = GetCredentialRequest.Builder()
                                .addCredentialOption(
                                    GetSignInWithGoogleOption.Builder(
                                        BuildConfig.GOOGLE_WEB_CLIENT_ID,
                                    ).build(),
                                )
                                .build()
                            credentialManager.getCredential(socialContext, explicit)
                        } else {
                            throw e
                        }
                    }
                    val credential = result.credential
                    if (credential is GoogleIdTokenCredential) {
                        viewModel.signInWithGoogle(credential.idToken)
                    } else {
                        viewModel.onSocialError("Unexpected Google credential type")
                    }
                } catch (_: GetCredentialCancellationException) {
                    // User closed the account sheet — not an error.
                } catch (_: NoCredentialException) {
                    viewModel.onSocialError(
                        "No Google account available. Sign in to a Google " +
                            "account on this phone and try again.",
                    )
                } catch (e: GetCredentialException) {
                    viewModel.onSocialError(
                        "Google sign-in unavailable — check Play Services (${e.type})",
                    )
                } finally {
                    socialBusy = false
                }
            }
        }
    }

    val onFacebookTap: () -> Unit = {
        if (!socialBusy) {
            if (BuildConfig.FACEBOOK_APP_ID.isBlank()) {
                viewModel.onSocialError("Facebook sign-in not configured in this build")
            } else {
                val activity = socialContext as? android.app.Activity
                if (activity == null) {
                    viewModel.onSocialError("Facebook sign-in unavailable")
                } else {
                    socialBusy = true
                    FacebookAuthBridge.onFacebookResult = { token, cancelled, error ->
                        when {
                            cancelled -> Unit
                            error != null -> viewModel.onSocialError(error)
                            token != null -> viewModel.signInWithFacebook(token)
                        }
                        socialBusy = false
                    }
                    com.facebook.login.LoginManager.getInstance()
                        .logInWithReadPermissions(
                            activity,
                            listOf("public_profile", "email"),
                        )
                }
            }
        }
    }

    // Easter egg: 5 taps on the logo reveal the server configuration.
    val onLogoTap: () -> Unit = {
        logoTapCount += 1
        if (logoTapCount >= 5) {
            showServerConfig = true
            logoTapCount = 0
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) onLoginSuccess()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp

    if (isLandscape && screenWidthDp > 600) {
        // Split layout for landscape/tablets
        Row(modifier = Modifier.fillMaxSize()) {
            // Left side - Branding
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(48.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ehw_logo),
                        contentDescription = "eHealthWares Logo",
                        modifier = Modifier
                            .size(100.dp)
                            .clickable(onClick = onLogoTap, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "RxSoft",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Point of Sale System",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onPrimary.copy(alpha = 0.8f),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    // Feature highlights
                    FeatureItem(text = "Inventory Management")
                    FeatureItem(text = "Sales Tracking")
                    FeatureItem(text = "Customer Management")
                }
            }

            // Right side - Login form
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colors.background),
                contentAlignment = Alignment.Center,
            ) {
                LoginFormContent(
                    viewModel = viewModel,
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    loginState = loginState,
                    serverUrl = serverUrl,
                    urlChanged = urlChanged,
                    showServerConfig = showServerConfig,
                    serverMode = serverMode,
                    onLogoTap = onLogoTap,
                    onUseProductionServer = { viewModel.useProductionServer(); urlChanged = false },
                    onUseCustomServer = { viewModel.useCustomServer(); urlChanged = false },
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                    onLogin = { viewModel.login(username, password) },
                    onGoogleTap = onGoogleTap,
                    onFacebookTap = onFacebookTap,
                    onServerUrlChange = { viewModel.updateServerUrl(it); urlChanged = true },
                    onServerUrlSave = {
                        viewModel.saveServerUrl(serverUrl)
                        urlChanged = false
                        showUrlSaved = true
                    },
                    onServerUrlReset = {
                        viewModel.updateServerUrl("https://api.ehealthwares.com")
                        viewModel.saveServerUrl("https://api.ehealthwares.com")
                        urlChanged = false
                        showUrlSaved = true
                    },
                )
            }
        }
    } else {
        // Portrait layout - full screen with gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(SpacingTokens.xxxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Logo section
                Image(
                    painter = painterResource(R.drawable.ehw_logo),
                    contentDescription = "eHealthWares Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clickable( onClick = onLogoTap, indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() } ),
                    contentScale = ContentScale.Fit,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "RxSoft",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Point of Sale",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Login form card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ShapeTokens.xl,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(SpacingTokens.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Welcome Back",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sign in to your account",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        LoginFormContent(
                            viewModel = viewModel,
                                    username = username,
                            password = password,
                            passwordVisible = passwordVisible,
                            loginState = loginState,
                            serverUrl = serverUrl,
                            urlChanged = urlChanged,
                            showServerConfig = showServerConfig,
                            serverMode = serverMode,
                            onLogoTap = onLogoTap,
                            onUseProductionServer = { viewModel.useProductionServer(); urlChanged = false },
                            onUseCustomServer = { viewModel.useCustomServer(); urlChanged = false },
                            onUsernameChange = { username = it },
                            onPasswordChange = { password = it },
                            onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                            onLogin = { viewModel.login(username, password) },
                            onGoogleTap = onGoogleTap,
                            onFacebookTap = onFacebookTap,
                            onServerUrlChange = { viewModel.updateServerUrl(it); urlChanged = true },
                            onServerUrlSave = {
                                viewModel.saveServerUrl(serverUrl)
                                urlChanged = false
                                showUrlSaved = true
                            },
                            onServerUrlReset = {
                                viewModel.updateServerUrl("https://api.ehealthwares.com")
                                viewModel.saveServerUrl("https://api.ehealthwares.com")
                                urlChanged = false
                                showUrlSaved = true
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Text(
                    text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} eHealthWares. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.onPrimary.copy(alpha = 0.8f)),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onPrimary.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun LoginFormContent(
    viewModel: AuthViewModel,
    username: String,
    password: String,
    passwordVisible: Boolean,
    loginState: UiState<Unit>,
    serverUrl: String,
    showServerConfig: Boolean,
    serverMode: com.ehealthwares.rxsoft.util.ServerUrlManager.Mode,
    onLogoTap: () -> Unit,
    onUseProductionServer: () -> Unit,
    onUseCustomServer: () -> Unit,
    urlChanged: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onLogin: () -> Unit,
    onGoogleTap: () -> Unit,
    onFacebookTap: () -> Unit,
    onServerUrlChange: (String) -> Unit,
    onServerUrlSave: () -> Unit,
    onServerUrlReset: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme

    AppTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = "Username",
            singleLine = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
        )
        Spacer(modifier = Modifier.height(SpacingTokens.lg))

        AppTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            singleLine = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = onLogin,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            trailingIconDescription = if (passwordVisible) "Hide password" else "Show password",
            onTrailingIconClick = onPasswordVisibleToggle,
        )
        Spacer(modifier = Modifier.height(SpacingTokens.xxl))

        when (loginState) {
            is UiState.Loading -> AppLoadingState()
            else -> AppPrimaryButton(
                text = "Login",
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotBlank() && password.isNotBlank(),
                description = "Login button",
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.md))
        Text(
            text = "or continue with",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(vertical = SpacingTokens.xs),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clickable(onClick = onGoogleTap),
                shape = ShapeTokens.card,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Google",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clickable(onClick = onFacebookTap),
                shape = ShapeTokens.card,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Facebook",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (loginState is UiState.Error) {
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            Text(
                text = (loginState as UiState.Error).message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.lg))
    // Server configuration — hidden by default; revealed by tapping the
    // app logo 5x (easter egg). Shows the two modes: Production / Custom.
    if (showServerConfig) {
        Spacer(modifier = Modifier.height(SpacingTokens.lg))
        Text(
            text = "Server Mode",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(SpacingTokens.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
            AppFilterChip(
                selected = serverMode == com.ehealthwares.rxsoft.util.ServerUrlManager.Mode.PRODUCTION,
                onClick = onUseProductionServer,
                label = "Production",
            )
            AppFilterChip(
                selected = serverMode == com.ehealthwares.rxsoft.util.ServerUrlManager.Mode.CUSTOM,
                onClick = onUseCustomServer,
                label = "Custom / Dev",
            )
        }
        Spacer(modifier = Modifier.height(SpacingTokens.sm))
        Text(
            text = serverUrl,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )

        if (serverMode == com.ehealthwares.rxsoft.util.ServerUrlManager.Mode.CUSTOM) {
            Spacer(modifier = Modifier.height(SpacingTokens.md))
            AppTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                label = "Custom server URL",
                trailingIcon = Icons.Default.Refresh,
                trailingIconDescription = "Restore default URL",
                onTrailingIconClick = onServerUrlReset,
            )
            if (urlChanged) {
                Spacer(modifier = Modifier.height(SpacingTokens.sm))
                AppPrimaryButton(
                    text = "Save URL",
                    onClick = onServerUrlSave,
                    description = "Save server URL",
                )
            }
        }
    }
}
