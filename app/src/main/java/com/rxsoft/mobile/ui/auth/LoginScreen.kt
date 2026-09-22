package com.rxsoft.mobile.ui.auth

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.R
import com.rxsoft.mobile.BuildConfig
import com.rxsoft.mobile.ui.designsystem.components.AppFilterChip
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextField
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState
import androidx.compose.foundation.layout.widthIn

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    startInPhoneMode: Boolean = false,
    viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val colors = MaterialTheme.colorScheme
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("password") }
    var passwordVisible by remember { mutableStateOf(false) }
    var urlChanged by remember { mutableStateOf(false) }
    var showUrlSaved by remember { mutableStateOf(false) }
    var showUrlField by remember { mutableStateOf(false) }
    val loginState by viewModel.loginState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

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
                        modifier = Modifier.size(100.dp),
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
                    startInPhoneMode = startInPhoneMode,
                    username = username,
                    password = password,
                    passwordVisible = passwordVisible,
                    loginState = loginState,
                    serverUrl = serverUrl,
                    showUrlField = showUrlField,
                    urlChanged = urlChanged,
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                    onLogin = { viewModel.login(username, password) },
                    onShowUrlFieldToggle = { showUrlField = !showUrlField },
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
                    modifier = Modifier.size(80.dp),
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
                            startInPhoneMode = startInPhoneMode,
                            username = username,
                            password = password,
                            passwordVisible = passwordVisible,
                            loginState = loginState,
                            serverUrl = serverUrl,
                            showUrlField = showUrlField,
                            urlChanged = urlChanged,
                            onUsernameChange = { username = it },
                            onPasswordChange = { password = it },
                            onPasswordVisibleToggle = { passwordVisible = !passwordVisible },
                            onLogin = { viewModel.login(username, password) },
                            onShowUrlFieldToggle = { showUrlField = !showUrlField },
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
    startInPhoneMode: Boolean = false,
    username: String,
    password: String,
    passwordVisible: Boolean,
    loginState: UiState<Unit>,
    serverUrl: String,
    showUrlField: Boolean,
    urlChanged: Boolean,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibleToggle: () -> Unit,
    onLogin: () -> Unit,
    onShowUrlFieldToggle: () -> Unit,
    onServerUrlChange: (String) -> Unit,
    onServerUrlSave: () -> Unit,
    onServerUrlReset: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shopperState by viewModel.shopperState.collectAsState()
    val otpRequested by viewModel.otpRequested.collectAsState()
    val devOtp by viewModel.devOtp.collectAsState()

    var phoneMode by remember { mutableStateOf(startInPhoneMode) }
    var phone by remember { mutableStateOf("") }
    var channel by remember { mutableStateOf("whatsapp") }
    var otp by remember { mutableStateOf("") }

    if (!phoneMode) {
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

        if (loginState is UiState.Error) {
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            Text(
                text = (loginState as UiState.Error).message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.lg))
        AppTextButton(
            text = "Continue with phone",
            onClick = {
                phoneMode = true
                otp = ""
            },
        )
    } else {
        // DEV ONLY: show the code the server just generated, and pre-fill the
        // OTP field so sign-in can be tested before SMS/WhatsApp delivery works.
        LaunchedEffect(devOtp) { devOtp?.let { otp = it } }
        devOtp?.let { code ->
            Surface(
                color = colors.primaryContainer,
                shape = ShapeTokens.md,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(SpacingTokens.md)) {
                    Text(
                        "OTP (test only)",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onPrimaryContainer,
                    )
                    Text(
                        code,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.height(SpacingTokens.md))
        }
        Text(
            text = "Sign in with your phone number",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(SpacingTokens.lg))
        AppTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "Phone number",
            singleLine = true,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
        )
        Spacer(modifier = Modifier.height(SpacingTokens.md))
        Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
            AppFilterChip(
                selected = channel == "whatsapp",
                onClick = { channel = "whatsapp" },
                label = "WhatsApp",
            )
            AppFilterChip(
                selected = channel == "sms",
                onClick = { channel = "sms" },
                label = "SMS",
            )
        }
        Spacer(modifier = Modifier.height(SpacingTokens.lg))

        if (!otpRequested) {
            AppPrimaryButton(
                text = "Send code",
                onClick = { viewModel.requestShopperOtp(phone, channel) },
                modifier = Modifier.fillMaxWidth(),
                enabled = phone.isNotBlank() && shopperState !is UiState.Loading,
            )
        } else {
            AppTextField(
                value = otp,
                onValueChange = { otp = it },
                label = "OTP code",
                singleLine = true,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.verifyShopperOtp(phone, otp) },
            )
            Spacer(modifier = Modifier.height(SpacingTokens.lg))
            AppPrimaryButton(
                text = "Verify & Continue",
                onClick = { viewModel.verifyShopperOtp(phone, otp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length >= 4 && shopperState !is UiState.Loading,
            )
            AppTextButton(
                text = "Resend code",
                onClick = { viewModel.requestShopperOtp(phone, channel) },
            )
        }

        if (shopperState is UiState.Error) {
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            Text(
                text = (shopperState as UiState.Error).message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.lg))
        AppTextButton(
            text = "Back to sign in",
            onClick = { phoneMode = false },
        )
    }

    // Debug server URL configuration
    if (BuildConfig.DEBUG && !phoneMode) {
        Spacer(modifier = Modifier.height(SpacingTokens.lg))
        AppTextButton(
            text = if (showUrlField) "Hide server URL" else "Configure server URL",
            onClick = onShowUrlFieldToggle,
        )

        if (showUrlField) {
            Spacer(modifier = Modifier.height(SpacingTokens.md))
            AppTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                label = "Server URL",
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
