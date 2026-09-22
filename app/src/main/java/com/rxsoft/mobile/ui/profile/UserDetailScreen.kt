package com.rxsoft.mobile.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.rxsoft.mobile.data.remote.dto.CurrentUserResponse
import com.rxsoft.mobile.data.remote.dto.UserPosConfig
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.components.AppErrorState
import com.rxsoft.mobile.ui.designsystem.components.AppLoadingState
import com.rxsoft.mobile.ui.designsystem.components.AppTextButton
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    onBack: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onAppearance: () -> Unit = {},
    onSignOut: () -> Unit = {},
    viewModel: UserDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.user.collectAsState()
    val posConfig by viewModel.posConfig.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    Scaffold(
        topBar = { AppTopAppBar(title = "User Detail", onBack = onBack, onMenuClick = onMenuClick) },
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
            },
        ) {
            when (val s = state) {
                is UiState.Loading, is UiState.Idle -> AppLoadingState()
                is UiState.Error -> AppErrorState(message = s.message, onRetry = { viewModel.refresh() })
                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(SpacingTokens.screenHorizontal),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                    ) {
                        item { UserHeader(s.data) }
                        item { UserFieldsCard(s.data) }
                        item { PosConfigCard(posConfig, onReload = { viewModel.refresh() }) }
                        item { AppTextButton(text = "Appearance", onClick = onAppearance) }
                        item {
                            Button(
                                onClick = onSignOut,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Text("Sign Out")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserHeader(user: CurrentUserResponse) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.padding(end = SpacingTokens.md),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(user.username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                user.phone?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun UserFieldsCard(user: CurrentUserResponse) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
            Field("Username", user.username)
            user.phone?.let { Field("Phone", it) }
            Field("Roles", user.roles.joinToString(", ").ifBlank { "-" })
            Field("Permissions", (user.permissions?.size ?: 0).toString())
            user.modules?.takeIf { it.isNotEmpty() }?.let { modules ->
                Field("Modules", modules.mapNotNull { it.name ?: it.code }.joinToString(", "))
            }
        }
    }
}

@Composable
private fun PosConfigCard(config: UserPosConfig?, onReload: () -> Unit) {
    val reloadDescription = "Reload the Product Configurations including recent Prices."
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("POS Configuration", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = onReload) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = reloadDescription,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Field("Stock location", config?.stockLocation?.name ?: "Not configured")
            Field("Store", config?.stockLocationId ?: config?.storeId ?: "Not configured")
            Field(
                "Price list",
                config?.defaultPriceList?.name
                    ?: config?.defaultPriceList?.code
                    ?: config?.defaultPriceListId
                    ?: "Not configured",
            )
            Text(
                reloadDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Field(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
