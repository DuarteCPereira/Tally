package com.duarte.dui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.DuiRawText
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * A thin trend line with no axes, gridlines or fill. [trend] picks `positive` or `negative`.
 * [contentDescription] is required because the line carries no numbers ("Value rising over the period").
 */
@Composable
fun DuiLineChart(
    points: List<Float>,
    trend: DuiTrend,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val color = trend.color(DuiTheme.colors)
    Canvas(modifier.fillMaxWidth().height(DuiDimens.ChartHeight), contentDescription) {
        if (points.size < 2) return@Canvas
        val stroke = DuiDimens.ChartStroke.toPx()
        val min = points.min()
        val range = points.max() - min
        val width = size.width - stroke
        val height = size.height - stroke
        val path = Path()
        points.forEachIndexed { i, point ->
            val x = stroke / 2 + width * i / (points.size - 1)
            val fraction = if (range == 0f) 0.5f else (point - min) / range
            val y = stroke / 2 + height * (1f - fraction)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/**
 * Range chips under a chart (1D, 1W, 1M, 1Y). The selected chip uses `surface-high` with
 * `text-primary`; the others are `text-secondary`. Each chip keeps a `space-7` touch target.
 */
@Composable
fun DuiRangeChips(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    val shape = DuiTheme.shapes.full
    // The mockup sets chips in caption size at weight 500.
    val style = DuiTheme.typography.caption.copy(fontWeight = FontWeight.Medium)
    Row(modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(spacing.space1)) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val source = remember { MutableInteractionSource() }
            Box(
                Modifier
                    .heightIn(min = spacing.space7)
                    .selectable(selected, source, indication = null, role = Role.Tab, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                DuiRawText(
                    option,
                    style,
                    if (selected) colors.textPrimary else colors.textSecondary,
                    Modifier
                        .duiFocusRing(source, shape)
                        .background(if (selected) colors.surfaceHigh else Color.Transparent, shape)
                        .padding(horizontal = spacing.space3, vertical = spacing.space1),
                    maxLines = 1,
                )
            }
        }
    }
}
