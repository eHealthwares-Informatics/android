package com.ehealthwares.rxsoft.ui.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ehealthwares.rxsoft.ui.designsystem.theme.AppThemeColors
import com.ehealthwares.rxsoft.ui.designsystem.token.DamorexColors
import com.ehealthwares.rxsoft.ui.designsystem.token.SpacingTokens
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// ── Theme helpers ─────────────────────────────────────────────────────────────

/**
 * True when the *app-selected* theme (not necessarily the system one) is dark.
 * Everything in the list kit derives from this so colors follow the
 * Appearance setting (Light/Dark/System) chosen in Theme Settings.
 */
@Composable
fun isListThemeDark(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

// ── Palette helpers ───────────────────────────────────────────────────────────

/** Status → (container, content) colors matching the mock: green/blue/red. */
data class StatusColors(val container: Color, val content: Color)

@Composable
private fun statusColors(status: String): StatusColors {
    val colors = AppThemeColors.current
    val dark = isListThemeDark()
    // Blue keeps a fixed hue but softens its container on dark surfaces.
    val blue = if (dark) Color(0xFF7EB8F7) else Color(0xFF1E88E5)
    val blueSoft = if (dark) Color(0xFF16283F) else Color(0xFFE3F2FD)
    return when (status.lowercase().replace(" ", "_")) {
        "completed", "paid", "settled", "received", "active" ->
            StatusColors(colors.greenSoft, colors.greenDark)
        "processing", "pending", "approved", "partially_received", "open", "queued_offline" ->
            StatusColors(blueSoft, blue)
        "cancelled", "voided", "refunded", "rejected", "failed", "inactive" ->
            StatusColors(
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
            )
        else -> StatusColors(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Title-cases a raw status string: "partially_received" -> "Partially Received". */
fun prettyStatus(status: String): String =
    status.replace("_", " ").trim().split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { w -> w.lowercase().replaceFirstChar { it.uppercase() } }

/** Rounded status pill (Completed=green, Processing=blue, Cancelled=red…). */
@Composable
fun StatusPill(status: String, modifier: Modifier = Modifier) {
    val colors = statusColors(status)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = colors.container,
    ) {
        Text(
            text = prettyStatus(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.content,
            maxLines = 1,
        )
    }
}

/** Green rounded-square icon tile used at the left of list headers/cards. */
@Composable
fun AccentIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.primary,
    tint: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(container, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ── Filter chips ──────────────────────────────────────────────────────────────

/** Horizontally scrollable filter chip row; selected chip is primary-filled. */
@Composable
fun FilterChipRow(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isListThemeDark()
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        items(options, key = { it }) { option ->
            val isSelected = option == selected
            val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val fg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            val border = when {
                isSelected -> MaterialTheme.colorScheme.primary
                dark -> MaterialTheme.colorScheme.outlineVariant
                else -> DamorexColors.border
            }
            Box(
                modifier = Modifier
                    .background(bg, RoundedCornerShape(50))
                    .border(1.dp, border, RoundedCornerShape(50))
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge,
                    color = fg,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                )
            }
        }
    }
}

// ── Accordion card ────────────────────────────────────────────────────────────

/**
 * Mock-styled accordion card: green icon tile, title, date/time and meta rows,
 * status pill + bold amount on the right, chevron toggle. The [content] is
 * only composed while expanded (lazy). All colors follow the app theme.
 */
@Composable
fun AccordionListCard(
    icon: ImageVector,
    title: String,
    dateLabel: String?,
    metaLabel: String?,
    statusText: String?,
    amount: BigDecimal?,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    amountFormat: NumberFormat = rememberNairaFormat(),
    content: @Composable () -> Unit,
) {
    val dark = isListThemeDark()
    val cardBg = MaterialTheme.colorScheme.surface
    val borderColor = if (dark) MaterialTheme.colorScheme.outlineVariant else DamorexColors.border
    val titleColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AccentIconBox(icon = icon)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (dateLabel != null) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedColor,
                        maxLines = 1,
                    )
                }
                if (metaLabel != null) {
                    Text(
                        text = metaLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = mutedColor,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (statusText != null) StatusPill(status = statusText)
                if (amount != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = amountFormat.format(amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = mutedColor,
                modifier = Modifier.size(20.dp),
            )
        }

        // Composed only while expanded — keeps long lists cheap.
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

/** "Items (N)" section label inside an expanded accordion card. */
@Composable
fun AccordionItemsHeader(label: String, modifier: Modifier = Modifier) {
    val colors = AppThemeColors.current
    Text(
        text = label,
        modifier = modifier
            .fillMaxWidth()
            .background(colors.mint.copy(alpha = if (isListThemeDark()) 0.6f else 1f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = colors.greenDark,
    )
}

/** Highlighted total row at the bottom of an expanded accordion card. */
@Composable
fun AccordionTotalRow(label: String, amount: BigDecimal, modifier: Modifier = Modifier) {
    val format = rememberNairaFormat()
    val colors = AppThemeColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.greenSoft, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = format.format(amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.greenDark,
        )
    }
}

// ── Formatting helpers ────────────────────────────────────────────────────────

@Composable
fun rememberNairaFormat(): NumberFormat =
    androidx.compose.runtime.remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

/** Plain grouped number format (no currency symbol) — for quantities. */
@Composable
fun rememberQuantityFormat(): NumberFormat =
    androidx.compose.runtime.remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

/** ISO-8601 -> "MMM dd, yyyy · h:mm a" (mock style); falls back to the raw string. */
fun formatListTimestamp(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        val cleaned = iso.substringBefore('.').replace("Z", "").replace(" ", "T")
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = parser.parse(cleaned) ?: return@runCatching null
        SimpleDateFormat("MMM dd, yyyy · h:mm a", Locale.getDefault()).format(date)
    }.getOrNull() ?: iso
}
