package com.duarte.dui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import com.duarte.dui.internal.DuiRawText
import com.duarte.dui.theme.DuiTheme

/**
 * The headline value of a screen: a `caption` label, the amount in `display` and an optional signed
 * change in `label`. Use it once per screen. [amount] and [change] arrive formatted.
 */
@Composable
fun DuiAmountText(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    change: String? = null,
    trend: DuiTrend = DuiTrend.Up,
) {
    val colors = DuiTheme.colors
    val typography = DuiTheme.typography
    Column(modifier.semantics(mergeDescendants = true) {}) {
        DuiRawText(label, typography.caption, colors.textSecondary)
        DuiRawText(amount, typography.display, colors.textPrimary, Modifier.padding(vertical = DuiTheme.spacing.space1))
        if (change != null) DuiRawText(change, typography.label, trend.color(colors))
    }
}
