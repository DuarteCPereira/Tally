package com.duarte.dui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.DuiRawText
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/** One bottom-bar destination. [icon] is a 24dp outline icon; it is tinted by the bar. */
@Immutable
data class DuiTab(val label: String, val icon: ImageVector)

/**
 * Bottom tab bar on `bg` with a `divider` hairline on top. The active tab uses `accent`, the others
 * `text-secondary`. Pads itself for the bottom safe area on Android and iOS. Up to five tabs.
 */
@Composable
fun DuiBottomBar(
    items: List<DuiTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(items.size <= DuiDimens.MaxTabs) { "DuiBottomBar takes at most ${DuiDimens.MaxTabs} tabs" }
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    val dividerColor = colors.divider
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.bg)
            .drawBehind {
                val y = DuiDimens.Hairline.toPx() / 2
                drawLine(dividerColor, Offset(0f, y), Offset(size.width, y), DuiDimens.Hairline.toPx())
            }
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .padding(top = spacing.space2, bottom = spacing.space1)
            .selectableGroup(),
    ) {
        items.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            val tint = if (selected) colors.accent else colors.textSecondary
            val source = remember { MutableInteractionSource() }
            Column(
                Modifier
                    .weight(1f)
                    .heightIn(min = spacing.space7)
                    .duiFocusRing(source, DuiTheme.shapes.sm)
                    .selectable(selected, source, indication = null, role = Role.Tab, onClick = { onSelect(index) }),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.space1),
            ) {
                Image(
                    rememberVectorPainter(tab.icon),
                    contentDescription = null,
                    modifier = Modifier.size(DuiDimens.IconSize),
                    colorFilter = ColorFilter.tint(tint),
                )
                DuiRawText(tab.label, DuiTheme.typography.caption, tint, maxLines = 1)
            }
        }
    }
}
