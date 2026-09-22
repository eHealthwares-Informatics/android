package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import kotlinx.coroutines.launch

/**
 * A menu item in the side drawer.
 */
data class DrawerMenuItem(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
)

/**
 * A collapsible section in the side drawer.
 */
data class DrawerMenuSection(
    val title: String,
    val icon: ImageVector,
    val items: List<DrawerMenuItem>,
)

/**
 * Side navigation drawer with categorized menu sections.
 *
 * @param sections Menu sections to display
 * @param currentRoute Currently selected route
 * @param onNavigate Callback when a menu item is clicked
 * @param drawerState Controls drawer open/close state
 * @param content Main content to display
 */
@Composable
fun AppSideDrawer(
    sections: List<DrawerMenuSection>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    drawerState: androidx.compose.material3.DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    content: @Composable () -> Unit,
) {
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.xxl),
                ) {
                    Text(
                        text = "RxSoft",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(SpacingTokens.xs))
                    Text(
                        text = "Pharmacy Management",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Menu sections
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                ) {
                    sections.forEach { section ->
                        val isExpanded = expandedSections[section.title] ?: false
                        val hasSelectedItem = section.items.any { it.route == currentRoute }

                        // Section header (expandable)
                        NavigationDrawerItem(
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = section.title,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (hasSelectedItem) FontWeight.Bold else FontWeight.Normal,
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = section.title,
                                )
                            },
                            selected = false,
                            onClick = {
                                expandedSections[section.title] = !isExpanded
                            },
                            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )

                        // Section items
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Column {
                                section.items.forEach { item ->
                                    NavigationDrawerItem(
                                        label = {
                                            Text(
                                                text = item.title,
                                                modifier = Modifier.padding(start = SpacingTokens.sm),
                                            )
                                        },
                                        icon = item.icon?.let { icon ->
                                            {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = item.title,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        },
                                        selected = currentRoute == item.route,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            onNavigate(item.route)
                                        },
                                        modifier = Modifier.padding(
                                            start = SpacingTokens.xxxl,
                                            end = SpacingTokens.sm,
                                        ),
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        ),
                                    )
                                }
                            }
                        }
                    }

                    // Bottom spacer
                    Spacer(modifier = Modifier.height(SpacingTokens.xxl))
                }
            }
        },
        content = content,
    )
}

/**
 * Predefined menu sections for the pharmacy app.
 */
object PharmacyMenuSections {
    val sales = DrawerMenuSection(
        title = "Sales",
        icon = Icons.Default.Receipt,
        items = listOf(
            DrawerMenuItem(route = "pos/terminal", title = "New Sale", icon = Icons.Default.Add),
            DrawerMenuItem(route = "pos", title = "POS Terminal", icon = Icons.Default.PointOfSale),
            DrawerMenuItem(route = "pos/lines", title = "Sales Lines", icon = Icons.Default.ShoppingCart),
            DrawerMenuItem(route = "orders", title = "Orders", icon = Icons.Default.ReceiptLong),
            DrawerMenuItem(route = "orders/new", title = "New Order", icon = Icons.Default.PostAdd),
            DrawerMenuItem(route = "orders/lines", title = "Order Lines", icon = Icons.Default.Receipt),
            DrawerMenuItem(route = "purchases", title = "Purchases", icon = Icons.Default.ShoppingBag),
            DrawerMenuItem(route = "customers", title = "Customers", icon = Icons.Default.People),
            DrawerMenuItem(route = "price-lists", title = "Price Lists", icon = Icons.Default.PriceChange),
        ),
    )

    val shop = DrawerMenuSection(
        title = "Shop",
        icon = Icons.Default.Store,
        items = listOf(
            DrawerMenuItem(route = "shop", title = "Catalog", icon = Icons.Default.Medication),
            DrawerMenuItem(route = "chat", title = "Messages", icon = Icons.Default.ChatBubble),
        ),
    )

    val inventory = DrawerMenuSection(
        title = "Inventory",
        icon = Icons.Default.Inventory2,
        items = listOf(
            DrawerMenuItem(route = "items", title = "Items", icon = Icons.Default.Medication),
            DrawerMenuItem(route = "inventory", title = "Stock Balance", icon = Icons.Default.Inventory2),
            DrawerMenuItem(route = "inventory/adjust", title = "Adjustment", icon = Icons.Default.Receipt),
        ),
    )

    val reports = DrawerMenuSection(
        title = "Reports",
        icon = Icons.Default.BarChart,
        items = listOf(
            DrawerMenuItem(route = "reports", title = "Daily Sales", icon = Icons.Default.BarChart),
        ),
    )

    val analytics = DrawerMenuSection(
        title = "Analytics",
        icon = Icons.Default.BarChart,
        items = listOf(
            DrawerMenuItem(route = "analytics", title = "Dashboard", icon = Icons.Default.BarChart),
        ),
    )

    val settings = DrawerMenuSection(
        title = "Settings",
        icon = Icons.Default.Settings,
        items = listOf(
            DrawerMenuItem(route = "user-detail", title = "User Detail", icon = Icons.Default.AccountCircle),
            DrawerMenuItem(route = "settings", title = "App Settings", icon = Icons.Default.Settings),
        ),
    )

    val all = listOf(sales, shop, inventory, reports, analytics, settings)
}
