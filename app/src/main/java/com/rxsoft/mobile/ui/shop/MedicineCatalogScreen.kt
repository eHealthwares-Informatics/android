package com.rxsoft.mobile.ui.shop

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.FilledIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.rxsoft.mobile.ui.designsystem.components.AppSearchBar
import com.rxsoft.mobile.ui.designsystem.components.AppTopAppBar
import com.rxsoft.mobile.ui.designsystem.theme.AppearanceMode
import com.rxsoft.mobile.ui.designsystem.theme.ThemeViewModel
import com.rxsoft.mobile.ui.designsystem.token.ElevationTokens
import com.rxsoft.mobile.ui.designsystem.token.ShapeTokens
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import com.rxsoft.mobile.ui.shop.model.Product
import com.rxsoft.mobile.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineCatalogScreen(
    onProductClick: (Product) -> Unit = {},
    onCartClick: () -> Unit = {},
    onSignIn: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    viewModel: MedicineCatalogViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val colors = MaterialTheme.colorScheme
    val itemsState by viewModel.items.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(itemsState) {
        if (itemsState !is UiState.Loading) isRefreshing = false
    }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val themeSettings by themeViewModel.themeSettings.collectAsState()

    val isDark = when (themeSettings.appearanceMode) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> {
            val cfg = LocalContext.current.resources.configuration
            (cfg.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }
    }

    var search by remember { mutableStateOf("") }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AppTopAppBar(
                title = "Medicine",
                onMenuClick = onMenuClick,
                actions = {
                    onSignIn?.let { signIn ->
                        IconButton(onClick = signIn) {
                            Icon(Icons.Default.Login, contentDescription = "Sign in")
                        }
                        Spacer(Modifier.width(SpacingTokens.sm))
                    }
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge { Text(cartCount.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Outlined.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                    Spacer(Modifier.width(SpacingTokens.sm))
                    IconButton(onClick = {
                        val newMode = if (isDark) AppearanceMode.LIGHT else AppearanceMode.DARK
                        themeViewModel.updateAppearanceMode(newMode)
                    }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "Switch to light mode" else "Switch to dark mode",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = SpacingTokens.xl)
        ) {
            Spacer(Modifier.height(SpacingTokens.md))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AppSearchBar(
                    query = search,
                    onQueryChange = {
                        search = it
                        viewModel.updateSearchQuery(it)
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = "Search medicines",
                    searchDescription = "Search medicines",
                )
                Spacer(Modifier.width(SpacingTokens.md))
                FilledIconButton(
                    onClick = { },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = colors.primary
                    )
                ) {
                    Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = colors.onPrimary)
                }
            }

            Spacer(Modifier.height(SpacingTokens.xl))

            PullToRefreshBox(
                modifier = Modifier.weight(1f),
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.loadItems()
                },
            ) {
                when (val state = itemsState) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is UiState.Error -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(state.message, color = colors.error)
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val filtered by remember {
                            derivedStateOf {
                                if (search.isBlank()) state.data
                                else state.data.filter {
                                    it.name.contains(search, ignoreCase = true) ||
                                    it.manufacturer.contains(search, ignoreCase = true) ||
                                    it.code?.contains(search, ignoreCase = true) == true ||
                                    it.barcode?.contains(search, ignoreCase = true) == true
                                }
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
                            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = SpacingTokens.lg)
                        ) {
                            items(filtered, key = { it.id }) { product ->
                                MedicineCard(
                                    product = product,
                                    onFavourite = { },
                                    onClick = { onProductClick(product) },
                                    onAddToCart = { viewModel.addToCart(product) },
                                )
                            }
                        }
                    }
                    is UiState.Idle -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item { Box(Modifier.fillParentMaxSize()) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicineCard(
    product: Product,
    onFavourite: () -> Unit,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { contentDescription = product.name },
        shape = ShapeTokens.xl,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.sm)
    ) {
        Column {
            Box {
                if (product.imageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.ShoppingCart, contentDescription = "Product image placeholder", tint = colors.onSurfaceVariant)
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(SpacingTokens.sm),
                    shape = CircleShape,
                    color = colors.surface,
                    shadowElevation = ElevationTokens.xxs,
                ) {
                    IconButton(onClick = onFavourite) {
                        Icon(
                            imageVector = if (product.isFavourite) Icons.Outlined.Favorite
                            else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (product.isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (product.isFavourite) colors.error else colors.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(SpacingTokens.md)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
                Spacer(Modifier.height(SpacingTokens.xs))
                Text(
                    product.manufacturer,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(SpacingTokens.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$${product.price.toInt()}",
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    product.oldPrice?.let {
                        Spacer(Modifier.width(SpacingTokens.sm))
                        Text(
                            "$${it.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }
                }
                Spacer(Modifier.height(SpacingTokens.md))
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = ShapeTokens.textField,
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = "Add to cart", modifier = Modifier.size(SpacingTokens.xl), tint = colors.onPrimary)
                    Spacer(Modifier.width(SpacingTokens.sm))
                    Text("Add", color = colors.onPrimary)
                }
            }
        }
    }
}
