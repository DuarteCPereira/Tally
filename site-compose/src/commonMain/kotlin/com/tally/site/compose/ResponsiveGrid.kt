package com.tally.site.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Compose's take on CSS `grid-template-columns: repeat(auto-fit, minmax(min(100%, [minCellWidth]), 1fr))`:
 * as many equal columns as fit at [minCellWidth], one column on narrow screens.
 *
 * Cells in a row share the tallest cell's height ([equalHeights]), like grid rows do.
 */
@Composable
internal fun <T> ResponsiveGrid(
    items: List<T>,
    minCellWidth: Dp,
    gap: Dp,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    equalHeights: Boolean = true,
    cell: @Composable (T) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = ((maxWidth + gap) / (minCellWidth + gap)).toInt().coerceIn(1, items.size.coerceAtLeast(1))
        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            items.chunked(columns).forEach { row ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .then(if (equalHeights) Modifier.height(IntrinsicSize.Max) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = verticalAlignment,
                ) {
                    row.forEach { item ->
                        val fill = if (equalHeights) Modifier.fillMaxHeight() else Modifier
                        Column(Modifier.weight(1f).then(fill), verticalArrangement = Arrangement.Center) { cell(item) }
                    }
                    // Keep the last row's cells the same width as the rows above.
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}
