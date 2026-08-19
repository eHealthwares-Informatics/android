package com.rxsoft.mobile.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.rxsoft.mobile.BuildConfig
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppPrimaryButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTextField
import com.rxsoft.mobile.ui.designsystem.token.ColorTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var urlChanged by remember { mutableStateOf(false) }
    var showUrlSaved by remember { mutableStateOf(false) }
    var showUrlField by remember { mutableStateOf(false) }
    val loginState by viewModel.loginState.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) onLoginSuccess()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SpacingTokens.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "RxSoft Mobile",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            Text(
                text = "Point of Sale",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(SpacingTokens.xxxxl))

            AppTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                singleLine = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            )
            Spacer(modifier = Modifier.height(SpacingTokens.lg))

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                singleLine = true,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.login(username, password) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                trailingIconDescription = if (passwordVisible) "Hide password" else "Show password",
                onTrailingIconClick = { passwordVisible = !passwordVisible },
            )
            Spacer(modifier = Modifier.height(SpacingTokens.xxl))

            when (loginState) {
                is UiState.Loading -> AppLoadingState()
                else -> AppPrimaryButton(
                    text = "Login",
                    onClick = { viewModel.login(username, password) },
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
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(SpacingTokens.xxl))

            if (BuildConfig.DEBUG) {
                AppTextButton(
                    text = if (showUrlField) "Hide server URL" else "Configure server URL",
                    onClick = { showUrlField = !showUrlField },
                )
            }

            if (showUrlField) {
                Spacer(modifier = Modifier.height(SpacingTokens.lg))
                AppTextField(
                    value = serverUrl,
                    onValueChange = { viewModel.updateServerUrl(it); urlChanged = true },
                    label = "Server URL",
                    trailingIcon = Icons.Default.Refresh,
                    trailingIconDescription = "Restore default URL",
                    onTrailingIconClick = {
                        viewModel.updateServerUrl("http://10.0.2.2:8000/api/")
                        viewModel.saveServerUrl("http://10.0.2.2:8000/api/")
                        urlChanged = false
                        showUrlSaved = true
                    },
                )
                if (urlChanged) {
                    Spacer(modifier = Modifier.height(SpacingTokens.sm))
                    AppPrimaryButton(
                        text = "Save URL",
                        onClick = {
                            viewModel.saveServerUrl(serverUrl)
                            urlChanged = false
                            showUrlSaved = true
                        },
                        description = "Save server URL",
                    )
                }
            }
        }
    }
}
