package com.ehealthwares.rxsoft.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.ehealthwares.rxsoft.data.repository.OrderSubmitResult
import com.ehealthwares.rxsoft.ui.designsystem.components.AppPrimaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSecondaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppTopAppBar
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState
import java.math.BigDecimal

internal data class DraftOrderLine(
    val label: String,
    val itemId: String? = null,
    val genericItemCode: String? = null,
    val freetextName: String? = null,
    val quantity: Int = 1,
    val unitPrice: BigDecimal = BigDecimal.ZERO,
)
/** Full-screen New Order. One combined field: pick a catalog item or generic, or type free text. */
@Composable
fun CreateOrderScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit = {},
    onMenuClick: (() -> Unit)? = null,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val createState by viewModel.createState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searching by viewModel.searching.collectAsState()
    var paymentMethod by remember { mutableStateOf("cash") }
    var lineQuery by remember { mutableStateOf("") }
    var selItemId by remember { mutableStateOf<String?>(null) }
    var selItemLabel by remember { mutableStateOf("") }
    var selGenericCode by remember { mutableStateOf<String?>(null) }
    var selGenericName by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf("") }
    var lines by remember { mutableStateOf(listOf<DraftOrderLine>()) }
    DisposableEffect(Unit) {
        viewModel.resetCreateState()
        onDispose { viewModel.resetCreateState(); viewModel.clearSearch() }
    }
    val done = (createState as? UiState.Success<*>)?.data
    LaunchedEffect(done) {
        if (done is OrderSubmitResult.Pushed || done is OrderSubmitResult.Queued) {
            viewModel.loadOrders(); onSaved()
        }
    }
    fun clearInputs() {
        lineQuery = ""; selItemId = null; selItemLabel = ""
        selGenericCode = null; selGenericName = ""
        qtyText = "1"; priceText = ""; viewModel.clearSearch()
    }


    fun addLine() {
        val itemId = selItemId
        val gcode = selGenericCode
        // Nothing picked: whatever is typed becomes the free-text name.
        val free = if (itemId == null && gcode == null) lineQuery.trim().ifEmpty { null } else null
        if (itemId == null && gcode == null && free == null) return
        val qty = qtyText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val price = priceText.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val label = buildList {
            if (itemId != null) add(if (selItemLabel.isNotBlank()) selItemLabel else "Item")
            else if (gcode != null) add(if (selGenericName.isNotBlank()) selGenericName else gcode)
            if (free != null) add(free)
        }.joinToString(" - ").ifEmpty { "Line" }
        lines = lines + DraftOrderLine(label, itemId, gcode, free, qty, price)
        clearInputs()
    }
    Scaffold(
        topBar = { AppTopAppBar(title = "New Order", onBack = onBack, onMenuClick = onMenuClick) },
        bottomBar = {
            Surface {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        horizontal = SpacingTokens.screenHorizontal,
                        vertical = SpacingTokens.sm,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    AppSecondaryButton(text = "Cancel", onClick = onBack, modifier = Modifier.weight(1f))
                    AppPrimaryButton(
                        text = "Place Order (${lines.size})",
                        enabled = lines.isNotEmpty() && createState !is UiState.Loading,
                        onClick = {
                            viewModel.createOrder(
                                paymentMethod,
                                lines.map { l ->
                                    OrdersViewModel.buildItem(
                                        itemId = l.itemId,
                                        freetextName = l.freetextName,
                                        genericItemCode = l.genericItemCode,
                                        quantity = l.quantity,
                                        unitPrice = l.unitPrice,
                                    )
                                },
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
    ) { pad ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.sm),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Text("Payment method", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                FilterChip(selected = paymentMethod == "cash", onClick = { paymentMethod = "cash" }, label = { Text("Cash") })
                FilterChip(selected = paymentMethod == "transfer", onClick = { paymentMethod = "transfer" }, label = { Text("Transfer") })
            }
            HorizontalDivider()
            Text("Order line", style = MaterialTheme.typography.titleSmall)
            CreateOrderLineFields(
                query = lineQuery,
                searching = searching,
                searchResults = searchResults,
                selItemId = selItemId,
                selItemLabel = selItemLabel,
                pickedGenericCode = selGenericCode,
                pickedGenericName = selGenericName,
                lines = lines,
                qtyText = qtyText,
                priceText = priceText,
                createState = createState,
                onQueryChange = { q ->
                    lineQuery = q
                    // Editing the text drops the previous pick (unless the text
                    // still matches the picked label).
                    if (selItemId != null && q != selItemLabel) { selItemId = null; selItemLabel = "" }
                    if (selGenericCode != null && q != selGenericName) { selGenericCode = null; selGenericName = "" }
                    viewModel.search(q)
                },
                onPickCatalog = { hit ->
                    selItemId = hit.item.itemId
                    selItemLabel = hit.item.displayName ?: hit.item.name
                    lineQuery = selItemLabel
                    selGenericCode = null; selGenericName = ""
                },
                onPickGeneric = { hit ->
                    selGenericCode = hit.product.code
                    selGenericName = hit.product.name
                    lineQuery = hit.product.name
                    selItemId = null; selItemLabel = ""
                },
                onQtyChange = { qtyText = it.filter { c -> c.isDigit() } },
                onPriceChange = { priceText = it },
                onAddLine = { addLine() },
                onRemoveLine = { line -> lines = lines - line },
            )
            Spacer(Modifier.height(SpacingTokens.lg))
        }
    }
}
