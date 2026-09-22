package com.rxsoft.mobile.ui.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rxsoft.mobile.ui.auth.AppScreen
import com.rxsoft.mobile.ui.auth.AuthViewModel
import com.rxsoft.mobile.ui.auth.EnterPinScreen
import com.rxsoft.mobile.ui.auth.LoginScreen
import com.rxsoft.mobile.ui.chat.ChatScreen
import com.rxsoft.mobile.ui.chat.ConversationListScreen
import com.rxsoft.mobile.ui.customers.CustomerListScreen
import com.rxsoft.mobile.ui.designsystem.components.AppSideDrawer
import com.rxsoft.mobile.ui.designsystem.components.DrawerMenuItem
import com.rxsoft.mobile.ui.designsystem.components.DrawerMenuSection
import com.rxsoft.mobile.ui.designsystem.components.PharmacyMenuSections
import com.rxsoft.mobile.ui.designsystem.token.MotionTokens
import com.rxsoft.mobile.ui.inventory.StockAdjustmentScreen
import com.rxsoft.mobile.ui.inventory.StockBalanceScreen
import com.rxsoft.mobile.ui.items.ItemFormScreen
import com.rxsoft.mobile.ui.items.ItemListScreen
import com.rxsoft.mobile.ui.orders.CreateOrderScreen
import com.rxsoft.mobile.ui.orders.OrderLinesScreen
import com.rxsoft.mobile.ui.orders.OrderListScreen
import com.rxsoft.mobile.ui.purchases.PurchaseListScreen
import com.rxsoft.mobile.ui.pos.PosOrderDetailScreen
import com.rxsoft.mobile.ui.pos.PosOrderListScreen
import com.rxsoft.mobile.ui.pos.PosTerminalScreen
import com.rxsoft.mobile.ui.pos.SaleLinesScreen
import com.rxsoft.mobile.ui.pricing.PriceListItemsScreen
import com.rxsoft.mobile.ui.pricing.PriceListScreen
import com.rxsoft.mobile.ui.prescription.UploadPrescriptionScreen
import com.rxsoft.mobile.ui.profile.ProfileScreen
import com.rxsoft.mobile.ui.profile.UserDetailScreen
import com.rxsoft.mobile.ui.analytics.AnalyticsScreen
import com.rxsoft.mobile.ui.reports.DailySalesScreen
import com.rxsoft.mobile.ui.settings.AppModule
import com.rxsoft.mobile.ui.settings.SettingsScreen
import com.rxsoft.mobile.ui.settings.SettingsViewModel
import com.rxsoft.mobile.ui.shop.CheckoutScreen
import com.rxsoft.mobile.ui.shop.MedicineCatalogScreen
import com.rxsoft.mobile.ui.shop.ProductDetailScreen
import com.rxsoft.mobile.ui.designsystem.theme.ThemeSettingsScreen
import com.rxsoft.mobile.ui.splash.SplashScreen
import com.rxsoft.mobile.ui.sync.SyncLoadingScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    data object Login : Screen("login", "Login")
    data object PinSetup : Screen("pin_setup", "Create PIN")
    data object PinUnlock : Screen("pin_unlock", "Enter PIN")
    data object Shop : Screen("shop", "Shop")
    data object ProductDetail : Screen("shop/product/{itemId}", "Product Detail") {
        fun createRoute(itemId: String) = "shop/product/$itemId"
    }
    data object Checkout : Screen("shop/checkout", "Checkout")
    data object Prescription : Screen("prescription", "Prescription")
    data object Profile : Screen("profile", "Profile")
    data object UserDetail : Screen("user-detail", "User Detail")
    data object ThemeSettings : Screen("theme_settings", "Appearance")
    data object Pos : Screen("pos", "Sales")
    data object Orders : Screen("orders", "Order")
    data object CreateOrder : Screen("orders/new", "New Order")
    data object OrderLines : Screen("orders/lines", "Order Lines")
    data object Purchases : Screen("purchases", "Purchase")
    data object PosTerminal : Screen("pos/terminal", "New Sale")
    data object SaleLines : Screen("pos/lines", "Sales Lines")
    data object PosDetail : Screen("pos/{saleId}", "Order Detail") {
        fun createRoute(saleId: String) = "pos/$saleId"
    }
    data object Customers : Screen("customers", "Customers")
    data object Items : Screen("items", "Items")
    data object ItemForm : Screen("items/form/{itemId}", "Item Form") {
        fun createRoute(itemId: String?) = "items/form/${itemId ?: "new"}"
    }
    data object Inventory : Screen("inventory", "Stock Balance")
    data object StockAdjustment : Screen("inventory/adjust", "Stock Adjustment")
    data object PriceLists : Screen("price-lists", "Price Lists")
    data object PriceListItems : Screen("price-lists/{priceListId}", "Prices") {
        fun createRoute(priceListId: String) = "price-lists/$priceListId"
    }
    data object Reports : Screen("reports", "Reports")
    data object Analytics : Screen("analytics", "Analytics")
    data object Settings : Screen("settings", "Settings")
    data object Chat : Screen("chat", "Messages")
    data object ChatThread : Screen("chat/{conversationId}?title={title}", "Chat") {
        fun createRoute(conversationId: String, title: String?) =
            "chat/$conversationId" + (title?.let { "?title=${android.net.Uri.encode(it)}" } ?: "")
    }
}

/** Routes that appear as top-level drawer destinations (used to highlight in the drawer). */
private val drawerRoutes = setOf(
    Screen.Pos.route,
    Screen.PosTerminal.route,
    Screen.SaleLines.route,
    Screen.Orders.route,
    Screen.OrderLines.route,
    Screen.Purchases.route,
    Screen.Customers.route,
    Screen.PriceLists.route,
    Screen.Shop.route,
    Screen.UserDetail.route,
    Screen.Items.route,
    Screen.Inventory.route,
    Screen.Reports.route,
    Screen.Analytics.route,
    Screen.Settings.route,
    Screen.Chat.route,
)

/** Drawer routes for the shop-only mobile shopper shell. */
private val shopperDrawerRoutes = setOf(
    Screen.Shop.route,
    Screen.UserDetail.route,
    Screen.Chat.route,
)

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val screenState by authViewModel.screenState.collectAsState()
    val syncProgress by authViewModel.syncProgress.collectAsState()
    val syncError by authViewModel.syncError.collectAsState()
    val canContinueOffline by authViewModel.canContinueOffline.collectAsState()
    var splashDone by remember { mutableStateOf(false) }
    var pinResetTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        authViewModel.resetPinTrigger.collect {
            pinResetTrigger++
        }
    }

    when {
        !splashDone -> {
            SplashScreen(onSplashFinished = { splashDone = true })
        }
        screenState is AppScreen.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }
        screenState is AppScreen.Login -> LoginScreen(
            onLoginSuccess = { authViewModel.onLoginSuccess() },
        )
        screenState is AppScreen.PinSetup -> EnterPinScreen(
            onNavigateToHome = { authViewModel.onPinAuthenticated() },
            onNavigateToLogin = { authViewModel.onPinCancelled() },
        )
        screenState is AppScreen.PinUnlock -> EnterPinScreen(
            onNavigateToHome = { authViewModel.onPinAuthenticated() },
            onNavigateToLogin = { authViewModel.onPinCancelled() },
            resetTrigger = pinResetTrigger,
        )
        screenState is AppScreen.Syncing -> SyncLoadingScreen(
            progress = syncProgress,
            error = syncError,
            canContinueOffline = canContinueOffline,
            onRetry = { authViewModel.retryStartupSync() },
            onContinueOffline = { authViewModel.continueOffline() },
        )
        screenState is AppScreen.ShopperAuth -> LoginScreen(
            onLoginSuccess = { authViewModel.onLoginSuccess() },
            startInPhoneMode = true,
        )
        screenState is AppScreen.Main -> {
            val main = screenState as AppScreen.Main
            when {
                main.guest -> GuestShopScreen(onSignIn = { authViewModel.requireLogin() })
                main.shopper -> ShopperScaffold(authViewModel)
                else -> MainScaffold(authViewModel)
            }
        }
    }
}

/** Shop-only shell for self-onboarded mobile shoppers (no POS/inventory). */
@Composable
private fun ShopperScaffold(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val sections = listOf(
        DrawerMenuSection(
            title = "Shop",
            icon = Icons.Default.Store,
            items = listOf(
                DrawerMenuItem(route = Screen.Shop.route, title = "Catalog", icon = Icons.Default.Medication),
            ),
        ),
        DrawerMenuSection(
            title = "Account",
            icon = Icons.Default.AccountCircle,
            items = listOf(
                DrawerMenuItem(route = Screen.UserDetail.route, title = "User Detail", icon = Icons.Default.AccountCircle),
            ),
        ),
        DrawerMenuSection(
            title = "Chat",
            icon = Icons.Default.ChatBubble,
            items = listOf(
                DrawerMenuItem(route = Screen.Chat.route, title = "Messages", icon = Icons.Default.ChatBubble),
            ),
        ),
    )

    val showDrawer = currentRoute in shopperDrawerRoutes
    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }

    val content: @Composable () -> Unit = {
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Shop.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Screen.Shop.route) {
                    authViewModel.recordActivity()
                    MedicineCatalogScreen(
                        onProductClick = { product ->
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        },
                        onCartClick = { navController.navigate(Screen.Checkout.route) },
                        onMenuClick = openDrawer,
                    )
                }
                composable(
                    route = Screen.ProductDetail.route,
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                    authViewModel.recordActivity()
                    ProductDetailScreen(
                        itemId = itemId,
                        onBack = { navController.popBackStack() },
                        onAddToCart = { navController.navigate(Screen.Checkout.route) },
                    )
                }
                composable(Screen.Checkout.route) {
                    authViewModel.recordActivity()
                    CheckoutScreen(
                        onBack = { navController.popBackStack() },
                        onAddProduct = {
                            navController.navigate(Screen.Shop.route) {
                                popUpTo(Screen.Shop.route) { inclusive = true }
                            }
                        },
                        onOrderCreated = {
                            navController.navigate(Screen.Shop.route) {
                                popUpTo(Screen.Shop.route) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Screen.Chat.route) {
                    authViewModel.recordActivity()
                    ConversationListScreen(
                        onConversationClick = { conversationId, title ->
                            navController.navigate(Screen.ChatThread.createRoute(conversationId, title))
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(
                    route = Screen.ChatThread.route,
                    arguments = listOf(
                        navArgument("conversationId") { type = NavType.StringType },
                        navArgument("title") { type = NavType.StringType; defaultValue = "" },
                    ),
                ) { backStackEntry ->
                    val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                    val title = backStackEntry.arguments?.getString("title")?.ifEmpty { null }
                    authViewModel.recordActivity()
                    ChatScreen(
                        conversationId = conversationId,
                        title = title,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Screen.UserDetail.route) {
                    authViewModel.recordActivity()
                    UserDetailScreen(
                        onBack = null,
                        onMenuClick = openDrawer,
                        onSignOut = { authViewModel.logout() },
                    )
                }
            }
        }
    }

    if (showDrawer) {
        AppSideDrawer(
            sections = sections,
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            drawerState = drawerState,
        ) { content() }
    } else {
        content()
    }
}

@Composable
private fun GuestShopScreen(onSignIn: () -> Unit) {
    MedicineCatalogScreen(
        onProductClick = {},
        onCartClick = { onSignIn() },
        onSignIn = onSignIn,
    )
}

@Composable
fun MainScaffold(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val activeModules by settingsViewModel.activeModules.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Build drawer sections based on active modules.
    val sections = remember(activeModules) {
        buildList {
            // Sales section (POS, Orders, Purchases)
            if (activeModules.contains(AppModule.POS)) {
                add(PharmacyMenuSections.sales)
            }
            // Shop section
            if (activeModules.contains(AppModule.SHOP)) {
                add(PharmacyMenuSections.shop)
            }
            // Inventory section (its own module — must NOT be triggered by POS)
            if (activeModules.contains(AppModule.INVENTORY)) {
                add(PharmacyMenuSections.inventory)
            }
            // Reports & Analytics (its own module — must NOT be triggered by POS)
            if (activeModules.contains(AppModule.SALES)) {
                add(PharmacyMenuSections.reports)
                add(PharmacyMenuSections.analytics)
            }
            // Settings
            add(PharmacyMenuSections.settings)
        }
    }

    // Only show drawer on top-level screens.
    val showDrawer = currentRoute in drawerRoutes

    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val openDrawer: () -> Unit = {
        coroutineScope.launch { drawerState.open() }
    }

    // Wrapper that conditionally shows the drawer.
    if (showDrawer) {
        AppSideDrawer(
            sections = sections,
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            drawerState = drawerState,
        ) {
            MainContent(
                navController = navController,
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                onMenuClick = openDrawer,
            )
        }
    } else {
        MainContent(
            navController = navController,
            authViewModel = authViewModel,
            settingsViewModel = settingsViewModel,
            onMenuClick = null,
        )
    }
}

@Composable
private fun MainContent(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onMenuClick: (() -> Unit)?,
) {
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Pos.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(MotionTokens.enter),
                    initialOffsetX = { it / 4 },
                ) + fadeIn(animationSpec = tween(MotionTokens.fadeDuration))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(MotionTokens.exit),
                    targetOffsetX = { -it / 4 },
                ) + fadeOut(animationSpec = tween(MotionTokens.fadeDuration))
            },
        ) {
            composable(Screen.Pos.route) {
                authViewModel.recordActivity()
                PosOrderListScreen(
                    posConfigManager = authViewModel.posConfigManager,
                    onMenuClick = onMenuClick,
                    onNewSale = { navController.navigate(Screen.PosTerminal.route) },
                    onSaleClick = { saleId -> navController.navigate(Screen.PosDetail.createRoute(saleId)) },
                )
            }
            composable(Screen.Orders.route) {
                authViewModel.recordActivity()
                OrderListScreen(
                    onBack = null,
                    onMenuClick = onMenuClick,
                    onNewOrder = { navController.navigate(Screen.CreateOrder.route) },
                )
            }
            composable(Screen.CreateOrder.route) {
                authViewModel.recordActivity()
                CreateOrderScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(Screen.OrderLines.route) {
                authViewModel.recordActivity()
                OrderLinesScreen(
                    onBack = null,
                    onMenuClick = onMenuClick,
                )
            }
            composable(Screen.Purchases.route) {
                authViewModel.recordActivity()
                PurchaseListScreen(onMenuClick = onMenuClick)
            }
            composable(Screen.PosTerminal.route) {
                authViewModel.recordActivity()
                PosTerminalScreen(
                    onOrderCreated = { saleId ->
                        navController.navigate(Screen.PosDetail.createRoute(saleId)) {
                            popUpTo(Screen.Pos.route)
                        }
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.SaleLines.route) {
                authViewModel.recordActivity()
                SaleLinesScreen(
                    onBack = null,
                    onMenuClick = onMenuClick,
                )
            }
            composable(
                route = Screen.PosDetail.route,
                arguments = listOf(navArgument("saleId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId") ?: return@composable
                authViewModel.recordActivity()
                PosOrderDetailScreen(saleId = saleId, onBack = { navController.popBackStack() })
            }

            composable(Screen.Customers.route) {
                authViewModel.recordActivity()
                CustomerListScreen(onMenuClick = onMenuClick)
            }

            composable(Screen.PriceLists.route) {
                authViewModel.recordActivity()
                PriceListScreen(
                    onBack = null,
                    onMenuClick = onMenuClick,
                    onOpen = { id -> navController.navigate(Screen.PriceListItems.createRoute(id)) },
                )
            }
            composable(
                route = Screen.PriceListItems.route,
                arguments = listOf(navArgument("priceListId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val priceListId = backStackEntry.arguments?.getString("priceListId") ?: return@composable
                authViewModel.recordActivity()
                PriceListItemsScreen(
                    priceListId = priceListId,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Items.route) {
                authViewModel.recordActivity()
                ItemListScreen(
                    onAddItem = { navController.navigate(Screen.ItemForm.createRoute(null)) },
                    onEditItem = { itemId -> navController.navigate(Screen.ItemForm.createRoute(itemId)) },
                    onMenuClick = onMenuClick,
                )
            }
            composable(
                route = Screen.ItemForm.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                authViewModel.recordActivity()
                ItemFormScreen(
                    itemId = if (itemId == "new") null else itemId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(Screen.Inventory.route) {
                authViewModel.recordActivity()
                StockBalanceScreen(
                    onAdjustmentClick = { navController.navigate(Screen.StockAdjustment.route) },
                    onMenuClick = onMenuClick,
                )
            }
            composable(Screen.StockAdjustment.route) {
                authViewModel.recordActivity()
                StockAdjustmentScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Analytics.route) {
                authViewModel.recordActivity()
                AnalyticsScreen(onMenuClick = onMenuClick)
            }

            composable(Screen.Reports.route) {
                authViewModel.recordActivity()
                DailySalesScreen(onMenuClick = onMenuClick)
            }

            composable(Screen.Shop.route) {
                authViewModel.recordActivity()
                MedicineCatalogScreen(
                    onProductClick = { product ->
                        navController.navigate(Screen.ProductDetail.createRoute(product.id))
                    },
                    onCartClick = { navController.navigate(Screen.Checkout.route) },
                )
            }
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                authViewModel.recordActivity()
                ProductDetailScreen(
                    itemId = itemId,
                    onBack = { navController.popBackStack() },
                    onAddToCart = { navController.navigate(Screen.Checkout.route) },
                )
            }
            composable(Screen.Checkout.route) {
                authViewModel.recordActivity()
                CheckoutScreen(
                    onBack = { navController.popBackStack() },
                    onAddProduct = {
                        navController.navigate(Screen.Shop.route) {
                            popUpTo(Screen.Shop.route) { inclusive = true }
                        }
                    },
                    onOrderCreated = { saleId ->
                        navController.navigate(Screen.PosDetail.createRoute(saleId)) {
                            popUpTo(Screen.Shop.route)
                        }
                    },
                )
            }

            composable(Screen.Prescription.route) {
                authViewModel.recordActivity()
                UploadPrescriptionScreen(
                    onBack = { navController.popBackStack() },
                    onPrescriptionMedicine = { navController.navigate(Screen.Shop.route) },
                    onGeneralMedicine = { navController.navigate(Screen.Shop.route) },
                )
            }

            composable(Screen.Profile.route) {
                authViewModel.recordActivity()
                ProfileScreen(
                    onAppearance = { navController.navigate(Screen.ThemeSettings.route) },
                )
            }

            composable(Screen.UserDetail.route) {
                authViewModel.recordActivity()
                UserDetailScreen(
                    onBack = null,
                    onMenuClick = onMenuClick,
                    onAppearance = { navController.navigate(Screen.ThemeSettings.route) },
                    onSignOut = { authViewModel.logout() },
                )
            }

            composable(Screen.ThemeSettings.route) {
                authViewModel.recordActivity()
                ThemeSettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Settings.route) {
                authViewModel.recordActivity()
                SettingsScreen(
                    externalVm = settingsViewModel,
                    onSignOut = { authViewModel.logout() },
                    onMenuClick = onMenuClick,
                )
            }

            composable(Screen.Chat.route) {
                authViewModel.recordActivity()
                ConversationListScreen(
                    onConversationClick = { conversationId, title ->
                        navController.navigate(Screen.ChatThread.createRoute(conversationId, title))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.ChatThread.route,
                arguments = listOf(
                    navArgument("conversationId") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType; defaultValue = "" },
                ),
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
                val title = backStackEntry.arguments?.getString("title")?.ifEmpty { null }
                authViewModel.recordActivity()
                ChatScreen(
                    conversationId = conversationId,
                    title = title,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
