package com.rxsoft.mobile.ui.customers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import com.rxsoft.mobile.data.remote.dto.CustomerDto
import com.rxsoft.mobile.ui.designsystem.components.AppCard
import com.rxsoft.mobile.ui.designsystem.templates.ListScreenTemplate
import com.rxsoft.mobile.util.UiState

@Composable
fun CustomerListScreen(
    onMenuClick: (() -> Unit)? = null,
    viewModel: CustomerListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val customersState by viewModel.customers.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()

    ListScreenTemplate(
        title = "Customers",
        state = customersState,
        onMenuClick = onMenuClick,
        isLoadingMore = isLoadingMore,
        searchQuery = "",
        onSearchQueryChange = { viewModel.loadCustomers(it.ifBlank { null }) },
        onRefresh = { viewModel.loadCustomers() },
        onLoadMore = { viewModel.loadMoreCustomers() },
        emptyTitle = "No customers found",
        emptySubtitle = "Customers will appear here once added",
    ) { data ->
        items(data, key = { it.id }) { customer ->
            CustomerCard(customer)
        }
    }
}

@Composable
private fun CustomerCard(customer: CustomerDto) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Text(customer.name, style = MaterialTheme.typography.titleMedium)
        customer.phone?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
        customer.email?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}
