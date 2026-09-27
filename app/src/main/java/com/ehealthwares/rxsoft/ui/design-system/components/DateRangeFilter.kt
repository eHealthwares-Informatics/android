package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rxsoft.mobile.ui.designsystem.token.SpacingTokens
import java.time.DayOfWeek
import java.time.LocalDate

/** A named precomputed date range relative to "today". */
data class DateRangePreset(
    val label: String,
    val compute: (today: LocalDate) -> Pair<LocalDate?, LocalDate?>,
)

val DefaultDateRangePresets: List<DateRangePreset> = listOf(
    DateRangePreset("All") { null to null },
    DateRangePreset("Today") { t -> t to t },
    DateRangePreset("This week") { t -> t.with(DayOfWeek.MONDAY) to t },
    DateRangePreset("Last 7 days") { t -> t.minusDays(6) to t },
    DateRangePreset("This month") { t -> t.withDayOfMonth(1) to t },
    DateRangePreset("Last 30 days") { t -> t.minusDays(29) to t },
    DateRangePreset("Last month") { t ->
        val lm = t.minusMonths(1)
        lm.withDayOfMonth(1) to lm.withDayOfMonth(lm.lengthOfMonth())
    },
    DateRangePreset("This year") { t -> t.withDayOfYear(1) to t },
)

/**
 * Date-range filter: an inclusive From/To pair (tapped to open a date picker)
 * plus a preset **select** (All, This week, Last 7 days, …).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterRow(
    fromDate: String,
    toDate: String,
    onFromDateClick: () -> Unit,
    onToDateClick: () -> Unit,
    onClear: () -> Unit,
    onQuickSelect: (LocalDate?, LocalDate?) -> Unit,
    presets: List<DateRangePreset> = DefaultDateRangePresets,
) {
    val today = remember { LocalDate.now() }
    var expanded by remember { mutableStateOf(false) }
    var selectedPreset by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.screenHorizontal, vertical = SpacingTokens.xs),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        // ── Preset select ───────────────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selectedPreset ?: "Custom range",
                onValueChange = {},
                readOnly = true,
                label = { Text("Range") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                presets.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.label) },
                        onClick = {
                            selectedPreset = preset.label
                            expanded = false
                            val (from, to) = preset.compute(today)
                            onQuickSelect(from, to)
                        },
                    )
                }
            }
        }

        // ── From / To fields ────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = fromDate,
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
                    .clickable { onFromDateClick() },
                label = { Text("From") },
                readOnly = true,
                singleLine = true,
            )
            OutlinedTextField(
                value = toDate,
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToDateClick() },
                label = { Text("To") },
                readOnly = true,
                singleLine = true,
            )
            if (fromDate != "All" || toDate != "All") {
                TextButton(onClick = {
                    selectedPreset = null
                    onClear()
                }) {
                    Text("Clear")
                }
            }
        }
    }
}
