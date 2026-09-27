package com.rxsoft.mobile.ui.shop.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens

@Composable
fun PaymentSummary(
    subtotal: String = "₦0.00",
    delivery: String = "₦0.00",
    total: String = "₦0.00",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.xl)
        ) {
            Text(
                "Payment Summary",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(SpacingTokens.xl))
            SummaryRow("Subtotal", subtotal)
            Spacer(Modifier.height(SpacingTokens.md))
            SummaryRow("Delivery", delivery)
            HorizontalDivider(Modifier.padding(vertical = SpacingTokens.lg))
            SummaryRow("Total", total, isBold = true)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
        )
    }
}
