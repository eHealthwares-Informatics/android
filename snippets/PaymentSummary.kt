package com.ehealthwares.rxsoft.ui.shop.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaymentSummary(
    subtotal: String = "$0.00",
    delivery: String = "$0.00",
    total: String = "$0.00",
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                "Payment Summary",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(20.dp))

            SummaryRow(
                "Subtotal",
                subtotal
            )

            Spacer(Modifier.height(12.dp))

            SummaryRow(
                "Delivery",
                delivery
            )

            Divider(
                Modifier.padding(vertical = 16.dp)
            )

            SummaryRow(
                "Total",
                total,
                isBold = true
            )

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
            style =
            if (isBold)
                MaterialTheme.typography.titleMedium
            else
                MaterialTheme.typography.bodyLarge
        )

    }

}