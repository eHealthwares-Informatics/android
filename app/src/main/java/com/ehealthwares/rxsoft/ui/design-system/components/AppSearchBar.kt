package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    searchDescription: String = "Search",
    clearDescription: String = "Clear search",
) {
    AppTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.screenHorizontal)
            .semantics { contentDescription = searchDescription },
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        leadingIconDescription = searchDescription,
        trailingIcon = if (query.isNotEmpty()) Icons.Default.Clear else null,
        trailingIconDescription = if (query.isNotEmpty()) clearDescription else null,
        onTrailingIconClick = if (query.isNotEmpty()) {{ onQueryChange("") }} else null,
        singleLine = true,
    )
}
