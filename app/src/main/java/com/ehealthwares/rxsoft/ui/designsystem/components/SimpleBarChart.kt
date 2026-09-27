package com.rxsoft.mobile.ui.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/**
 * A single data point for a bar chart.
 */
data class BarChartData(
    val label: String,
    val value: Double,
)

/**
 * A simple vertical bar chart rendered on Canvas.
 *
 * @param data     list of data points (bar label + numeric value)
 * @param barColor fill colour of each bar
 * @param maxBars  maximum bars to show (defaults to data.size)
 * @param title    optional chart title
 */
@Composable
fun SimpleBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    maxBars: Int = 14,
    title: String? = null,
) {
    if (data.isEmpty()) return

    val subset = remember(data) { data.takeLast(maxBars) }
    val maxValue = remember(subset) { subset.maxOf { it.value }.coerceAtLeast(1.0) }
    val format = remember { NumberFormat.getCurrencyInstance(Locale("en", "NG")) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
        }

        // ── Chart canvas ────────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 4.dp),
        ) {
            drawBarChart(
                data = subset,
                maxValue = maxValue,
                barColor = barColor,
            )
        }

        Spacer(Modifier.height(4.dp))

        // ── X-axis labels (show at most 7 to avoid overlap) ─────────────────
        val labelStep = (subset.size / 7).coerceAtLeast(1)
        val displayLabels = subset.filterIndexed { i, _ -> i % labelStep == 0 || i == subset.lastIndex }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            displayLabels.forEach { point ->
                val shortLabel = if (point.label.length > 5) point.label.takeLast(5) else point.label
                Text(
                    text = shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                )
            }
        }

        // ── Y-axis max label ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = "max ${format.format(maxValue)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
            )
        }
    }
}

private fun DrawScope.drawBarChart(
    data: List<BarChartData>,
    maxValue: Double,
    barColor: Color,
) {
    val n = data.size
    if (n == 0) return

    val totalWidth = size.width
    val totalHeight = size.height
    val barGap = 4.dp.toPx()
    val barWidthPx = ((totalWidth - barGap * (n + 1)) / n).coerceAtLeast(4.dp.toPx())
    val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    val gridColor = Color.LightGray.copy(alpha = 0.4f)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))

    // Horizontal grid lines
    for (i in 0..4) {
        val y = totalHeight * (1f - i / 4f)
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(totalWidth, y),
            pathEffect = dashEffect,
        )
    }

    // Bars
    data.forEachIndexed { index, point ->
        val barHeight = (point.value / maxValue * totalHeight).toFloat().coerceAtLeast(2.dp.toPx())
        val x = barGap + index * (barWidthPx + barGap)
        val y = totalHeight - barHeight

        drawRoundRect(
            color = barColor,
            topLeft = Offset(x, y),
            size = Size(barWidthPx, barHeight),
            cornerRadius = cornerRadius,
        )

        // Value label on top of bar
        if (point.value > 0) {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 10.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            val label = if (point.value >= 1000) "${(point.value / 1000).toInt()}k" else "${point.value.toInt()}"
            drawContext.canvas.nativeCanvas.drawText(
                label,
                x + barWidthPx / 2,
                y - 4.dp.toPx(),
                paint,
            )
        }
    }
}
