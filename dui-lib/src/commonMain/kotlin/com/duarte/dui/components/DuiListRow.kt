package com.duarte.dui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.DuiRawText
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * A row with an optional leading slot, a title and subtitle, and a right-aligned value with a
 * change figure. All strings arrive formatted; [trend] only picks the colour of [change].
 *
 * Set [divider] on every row except the first to draw the `divider` hairline between rows.
 * [selected] tints the row with `accent-soft`.
 */
@Composable
fun DuiListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    change: String? = null,
    trend: DuiTrend = DuiTrend.Up,
    selected: Boolean = false,
    divider: Boolean = false,
    onClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    val colors = DuiTheme.colors
    val typography = DuiTheme.typography
    val spacing = DuiTheme.spacing
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val fill = when {
        pressed -> colors.surfaceHigh
        selected -> colors.accentSoft
        else -> Color.Transparent
    }
    val dividerColor = colors.divider
    val clickModifier = if (onClick != null) {
        Modifier.duiFocusRing(source, RectangleShape).duiClickable(source, Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Row(
        modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { this.selected = selected }
            .then(clickModifier)
            .background(fill)
            .drawBehind {
                if (divider) {
                    val y = DuiDimens.Hairline.toPx() / 2
                    drawLine(dividerColor, Offset(0f, y), Offset(size.width, y), DuiDimens.Hairline.toPx())
                }
            }
            .padding(vertical = spacing.space3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            DuiRawText(title, typography.label, colors.textPrimary, maxLines = 1)
            if (subtitle != null) DuiRawText(subtitle, typography.caption, colors.textSecondary, maxLines = 1)
        }
        if (value != null || change != null) {
            Column(horizontalAlignment = Alignment.End) {
                if (value != null) DuiRawText(value, typography.label, colors.textPrimary, maxLines = 1)
                if (change != null) DuiRawText(change, typography.caption, trend.color(colors), maxLines = 1)
            }
        }
    }
}

/** Round avatar on `surface-high` with one or two initials in `label`. */
@Composable
fun DuiAvatar(initials: String, modifier: Modifier = Modifier) {
    Box(
        modifier.size(DuiDimens.AvatarSize).background(DuiTheme.colors.surfaceHigh, DuiTheme.shapes.full),
        contentAlignment = Alignment.Center,
    ) {
        DuiRawText(initials, DuiTheme.typography.label, DuiTheme.colors.textPrimary, maxLines = 1)
    }
}

/** Round avatar on `surface-high` with an [icon] in `text-primary`. */
@Composable
fun DuiAvatar(icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier.size(DuiDimens.AvatarSize).background(DuiTheme.colors.surfaceHigh, DuiTheme.shapes.full),
        contentAlignment = Alignment.Center,
    ) {
        DuiIcon(icon, contentDescription = null)
    }
}
