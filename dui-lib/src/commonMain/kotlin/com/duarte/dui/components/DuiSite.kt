package com.duarte.dui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.LocalOnSurface
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/**
 * A link in the site header or footer. [target] is opaque to the library: an anchor ("#features")
 * or a URL, handed back to the caller on click. [badge] marks a feature that is not live yet ("Soon").
 */
@Immutable
data class DuiNavLink(val label: String, val target: String, val badge: String? = null)

/** Background of a [DuiSiteSection]. Alternate them down the page. */
enum class DuiSiteSectionTone {
    /** `bg`, the default. */
    Bg,

    /** `surface`, one step off `bg`. Content inside steps up like it does in a card. */
    Surface,
}

/**
 * A full-width page section with a 1200 content column, `space-9` vertical padding (`space-8` on
 * phones) and `space-8` between its blocks.
 *
 * [grid] draws a 48 `divider` hairline grid behind the content. Use it on one or two sections per
 * page, never all. [divider] draws a hairline along the top edge, to separate two sections of the
 * same tone. A section opens with a [DuiEyebrow] and a title.
 */
@Composable
fun DuiSiteSection(
    modifier: Modifier = Modifier,
    tone: DuiSiteSectionTone = DuiSiteSectionTone.Bg,
    grid: Boolean = false,
    divider: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .background(if (tone == DuiSiteSectionTone.Surface) colors.surface else colors.bg)
            .then(if (grid) Modifier.hairlineGrid() else Modifier)
            .then(if (divider) Modifier.topHairline() else Modifier),
        contentAlignment = Alignment.TopCenter,
    ) {
        val compact = maxWidth < DuiDimens.SiteCompactBreakpoint
        val padding = PaddingValues(
            horizontal = if (compact) spacing.space4 else spacing.space5,
            vertical = if (compact) spacing.space8 else spacing.space9,
        )
        CompositionLocalProvider(LocalOnSurface provides (tone == DuiSiteSectionTone.Surface)) {
            Column(
                Modifier
                    .padding(padding)
                    .widthIn(max = DuiDimens.SiteContentMaxWidth)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.space8),
                content = content,
            )
        }
    }
}

/**
 * The website header: the [DuiTallyMark] and [brand], the [links], and one small [action] such as a
 * [DuiButton] with [DuiButtonSize.Small]. It is `space-8` tall on `bg` with a hairline below; place it
 * above the scrolling content to keep it sticky. Below 720 the links hide and only the brand and the
 * action stay.
 *
 * @param current the [DuiNavLink.target] of the page or section the user is on, shown in `text-primary`.
 */
@Composable
fun DuiSiteHeader(
    links: List<DuiNavLink>,
    onLinkClick: (DuiNavLink) -> Unit,
    modifier: Modifier = Modifier,
    brand: String = "Tally",
    onBrandClick: () -> Unit = {},
    current: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val spacing = DuiTheme.spacing
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .background(DuiTheme.colors.bg)
            .bottomHairline()
            .heightIn(min = spacing.space8)
            .padding(horizontal = spacing.space5),
        contentAlignment = Alignment.CenterStart,
    ) {
        val compact = maxWidth < DuiDimens.SiteCompactBreakpoint
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space5),
        ) {
            SiteLink(onClick = onBrandClick, role = Role.Button) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    DuiTallyMark(size = 32.dp)
                    DuiText(brand, style = DuiTextStyle.Label, maxLines = 1)
                }
            }
            if (compact) {
                Spacer(Modifier.weight(1f))
            } else {
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space5, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    links.forEach { link -> HeaderLink(link, selected = link.target == current, onClick = { onLinkClick(link) }) }
                }
            }
            action?.invoke()
        }
    }
}

/** The website footer: the mark and [note] on the left, [links] on the right. Wraps on phones. */
@Composable
fun DuiSiteFooter(
    links: List<DuiNavLink>,
    note: String,
    onLinkClick: (DuiNavLink) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = DuiTheme.spacing
    FlowRow(
        modifier
            .fillMaxWidth()
            .background(DuiTheme.colors.bg)
            .topHairline()
            .padding(horizontal = spacing.space5, vertical = spacing.space6),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(spacing.space5),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            DuiTallyMark(size = DuiDimens.IconSize)
            DuiText(note, style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary, maxLines = 1)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space5)) {
            links.forEach { link ->
                SiteLink(onClick = { onLinkClick(link) }) { hovered ->
                    DuiText(
                        link.label,
                        style = DuiTextStyle.Caption,
                        color = if (hovered) DuiTheme.colors.textPrimary else DuiTheme.colors.textSecondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderLink(link: DuiNavLink, selected: Boolean, onClick: () -> Unit) {
    SiteLink(onClick = onClick) { hovered ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space2)) {
            DuiText(
                link.label,
                style = DuiTextStyle.Label,
                color = if (hovered || selected) DuiTheme.colors.textPrimary else DuiTheme.colors.textSecondary,
                maxLines = 1,
            )
            link.badge?.let { DuiBadge(it) }
        }
    }
}

/** A text link with a 48 touch height, a hand cursor, the focus ring and a hover flag for its content. */
@Composable
private fun SiteLink(
    onClick: () -> Unit,
    role: Role = Role.Button,
    content: @Composable (hovered: Boolean) -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    Box(
        Modifier
            .heightIn(min = DuiTheme.spacing.space7)
            .pointerHoverIcon(PointerIcon.Hand)
            .duiFocusRing(source, DuiTheme.shapes.sm)
            .duiClickable(source, role, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        content(hovered)
    }
}

@Composable
private fun Modifier.hairlineGrid(): Modifier {
    val color = DuiTheme.colors.divider
    return drawBehind {
        val cell = DuiDimens.SiteGridCell.toPx()
        val line = DuiDimens.Hairline.toPx()
        // Centre the grid horizontally like the mockup's `background-position: center top`.
        val offsetX = (size.width / 2) % cell
        var x = offsetX
        while (x < size.width) {
            drawLine(color, Offset(x, 0f), Offset(x, size.height), line)
            x += cell
        }
        var y = 0f
        while (y < size.height) {
            drawLine(color, Offset(0f, y), Offset(size.width, y), line)
            y += cell
        }
    }
}

@Composable
private fun Modifier.topHairline(): Modifier {
    val color = DuiTheme.colors.divider
    return drawBehind {
        val line = DuiDimens.Hairline.toPx()
        drawLine(color, Offset(0f, line / 2), Offset(size.width, line / 2), line)
    }
}

@Composable
private fun Modifier.bottomHairline(): Modifier {
    val color = DuiTheme.colors.divider
    return drawBehind {
        val line = DuiDimens.Hairline.toPx()
        drawLine(color, Offset(0f, size.height - line / 2), Offset(size.width, size.height - line / 2), line)
    }
}
