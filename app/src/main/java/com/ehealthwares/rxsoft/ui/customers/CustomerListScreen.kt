package com.ehealthwares.rxsoft.ui.customers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.data.remote.dto.CustomerDto
import com.ehealthwares.rxsoft.ui.designsystem.components.AccordionListCard
import com.ehealthwares.rxsoft.ui.designsystem.components.AppPrimaryButton
import com.ehealthwares.rxsoft.ui.designsystem.components.AppSearchBar
import com.ehealthwares.rxsoft.ui.designsystem.components.FilterChipRow
import com.ehealthwares.rxsoft.ui.designsystem.templates.ListScreenTemplate
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import com.ehealthwares.rxsoft.util.UiState

private const val ALL_CUSTOMERS = "All"

private val customerChips = listOf(ALL_CUSTOMERS, "Active", "Inactive")

@Composable
fun CustomerListScreen(
    onMenuClick: (() -> Unit)? = null,
    viewModel: CustomerListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val customersState by viewModel.customers.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val createState by viewModel.createState.collectAsState()

    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedChip by rememberSaveable { mutableStateOf(ALL_CUSTOMERS) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val allCustomers = (customersState as? UiState.Success)?.data.orEmpty()
    val visibleCustomers = remember(allCustomers, selectedChip, searchText) {
        allCustomers.filter { customer ->
            val inStatus = when (selectedChip) {
                ALL_CUSTOMERS -> true
                "Active" -> customer.isActive
                else -> !customer.isActive
            }
            val n = searchText.trim().lowercase()
            val inSearch = n.isBlank() ||
                customer.name.lowercase().contains(n) ||
                (customer.phone ?: "").contains(n) ||
                (customer.email ?: "").lowercase().contains(n) ||
                (customer.code ?: "").lowercase().contains(n)
            inStatus && inSearch
        }
    }

    ListScreenTemplate(
        title = "Customers",
        state = customersState,
        isLoadingMore = isLoadingMore,
        onMenuClick = onMenuClick,
        subtitle = "Customer Directory",
        headerIcon = Icons.Outlined.PeopleAlt,
        onSearchClick = { searchOpen = !searchOpen },
        onAddClick = { showCreateDialog = true },
        addDescription = "New customer",
        filters = {
            Column {
                AnimatedVisibility(visible = searchOpen) {
                    AppSearchBar(
                        query = searchText,
                        onQueryChange = { searchText = it },
                        placeholder = "Search name, phone, email, code",
                        modifier = Modifier.padding(bottom = SpacingTokens.xs),
                    )
                }
                FilterChipRow(
                    options = customerChips,
                    selected = selectedChip,
                    onSelect = { selectedChip = it },
                )
            }
        },
        onRefresh = { viewModel.loadCustomers() },
        onLoadMore = {
            // Infinite scroll: don't cascade page loads while a narrow
            // status/search filter hides rows.
            val filtering = selectedChip != ALL_CUSTOMERS || searchText.isNotBlank()
            if (!filtering || visibleCustomers.size >= 10) viewModel.loadMoreCustomers()
        },
        emptyTitle = "No customers found",
        emptySubtitle = "Tap + to add your first customer",
    ) { _ ->
        if (visibleCustomers.isEmpty() && allCustomers.isNotEmpty()) {
            item {
                Text(
                    "No customers match this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(SpacingTokens.lg),
                )
            }
        }
        items(visibleCustomers, key = { it.id }) { customer ->
            CustomerAccordionCard(
                customer = customer,
                expanded = expandedId == customer.id,
                onToggle = { expandedId = if (expandedId == customer.id) null else customer.id },
            )
        }
    }

    if (showCreateDialog) {
        CreateCustomerDialog(
            submitting = createState is UiState.Loading,
            error = (createState as? UiState.Error)?.message,
            onConfirm = { name, phone, email, address ->
                viewModel.createCustomer(name, phone, email, address)
            },
            onDismiss = {
                showCreateDialog = false
                viewModel.resetCreateState()
            },
        )
    }
    // Close + reset after a successful create.
    androidx.compose.runtime.LaunchedEffect(createState) {
        if (createState is UiState.Success) {
            showCreateDialog = false
            viewModel.resetCreateState()
        }
    }
}

@Composable
private fun CustomerAccordionCard(
    customer: CustomerDto,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    AccordionListCard(
        icon = Icons.Outlined.Person,
        title = customer.name,
        dateLabel = customer.phone,
        metaLabel = customer.email,
        statusText = if (customer.isActive) "active" else "inactive",
        amount = null,
        expanded = expanded,
        onToggle = onToggle,
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            DetailRow("Code", customer.code, titleColor, mutedColor)
            DetailRow("Phone", customer.phone, titleColor, mutedColor)
            DetailRow("Email", customer.email, titleColor, mutedColor)
            DetailRow("Address", customer.addressLine1, titleColor, mutedColor)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
    titleColor: androidx.compose.ui.graphics.Color,
    mutedColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = mutedColor)
        Text(
            value ?: "—",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(180.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

/** New-customer dialog opened by the FAB. */
@Composable
private fun CreateCustomerDialog(
    submitting: Boolean,
    error: String?,
    onConfirm: (name: String, phone: String?, email: String?, address: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("New customer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Name") },
                    enabled = !submitting,
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !submitting,
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    enabled = !submitting,
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Address") },
                    enabled = !submitting,
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = "Save",
                enabled = !submitting && name.isNotBlank(),
                onClick = { onConfirm(name, phone, email, address) },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("Cancel") }
        },
    )
}
