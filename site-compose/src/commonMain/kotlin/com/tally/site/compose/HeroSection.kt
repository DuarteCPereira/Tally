package com.tally.site.compose

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.duarte.dui.components.DuiSiteSection
import com.duarte.dui.components.DuiTallyMark
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.tally.site.Callout
import com.tally.site.Hero
import com.tally.site.LinkTarget

/** The first section: the headline card on a hairline grid, next to the live run screen. */
@Composable
internal fun HeroSection(hero: Hero, onLink: (LinkTarget) -> Unit, modifier: Modifier = Modifier) {
    DuiSiteSection(modifier, grid = true) {
        ResponsiveGrid(
            items = listOf(HeroCell.Copy, HeroCell.Phone),
            minCellWidth = TwoColumnMinWidth,
            gap = DuiTheme.spacing.space8,
            verticalAlignment = Alignment.CenterVertically,
            equalHeights = false,
        ) { cell ->
            when (cell) {
                HeroCell.Copy -> HeroCopy(hero, onLink)
                HeroCell.Phone -> HeroPhone(hero)
            }
        }
    }
}

private enum class HeroCell { Copy, Phone }

@Composable
private fun HeroCopy(hero: Hero, onLink: (LinkTarget) -> Unit) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    BoxWithConstraints {
        val compact = maxWidth < CompactCopyWidth
        Column(
            Modifier
                .background(colors.bg, DuiTheme.shapes.lg)
                .padding(spacing.space6),
            verticalArrangement = Arrangement.spacedBy(spacing.space5),
        ) {
            LiveChip(hero.chip)
            DuiTallyMark(size = MarkSize, animate = !LocalReduceMotion.current)
            // The hero steps down to display size on phones.
            DuiText(hero.title, style = if (compact) DuiTextStyle.Display else DuiTextStyle.Hero)
            DuiText(
                hero.body,
                Modifier.widthIn(max = BodyMaxWidth),
                color = colors.textSecondary,
            )
            StoreButtons(hero.stores, onLink)
            DuiText(hero.tagline, style = DuiTextStyle.Eyebrow, color = colors.textSecondary)
        }
    }
}

/** "Workout tracker · Android and iOS", with a pulsing accent dot. */
@Composable
private fun LiveChip(text: String) {
    val spacing = DuiTheme.spacing
    val pulse = if (LocalReduceMotion.current) {
        1f
    } else {
        val transition = rememberInfiniteTransition()
        val alpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = PulseMinAlpha,
            animationSpec = InfiniteRepeatableSpec(tween(PulseHalfMillis), RepeatMode.Reverse),
        )
        alpha
    }
    Row(
        Modifier
            .background(DuiTheme.colors.surface, DuiTheme.shapes.full)
            .padding(horizontal = spacing.space3, vertical = spacing.space1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        Box(
            Modifier
                .size(spacing.space2)
                .alpha(pulse)
                .background(DuiTheme.colors.accent, DuiTheme.shapes.full),
        )
        DuiText(text, style = DuiTextStyle.Eyebrow, color = DuiTheme.colors.textSecondary)
    }
}

/** The run screen in a frame, with callouts that point into it on wide screens. */
@Composable
private fun HeroPhone(hero: Hero) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val showCallouts = maxWidth >= CalloutMinWidth
        Box {
            AppScreenFrame(hero.screen, scale = HeroScale)
            if (showCallouts) {
                // matchParentSize hands the callouts the frame's exact size to position against.
                hero.callouts.forEach { Box(Modifier.matchParentSize()) { CalloutLabel(it) } }
            }
        }
    }
}

/**
 * A mono label with a leader line and a dot, placed relative to the frame like the mockup:
 * the dot sits [CalloutInset] inside the frame edge and the label hangs outside it.
 */
@Composable
private fun CalloutLabel(callout: Callout) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    val dot = @Composable {
        Box(Modifier.size(CalloutDot).background(colors.accent, DuiTheme.shapes.full))
    }
    val line = @Composable {
        Box(Modifier.width(CalloutLine).height(1.dp).background(colors.textSecondary))
    }
    val label = @Composable {
        DuiText(
            callout.text,
            Modifier
                .border(1.dp, colors.divider, DuiTheme.shapes.full)
                .background(colors.bg, DuiTheme.shapes.full)
                .padding(horizontal = spacing.space3, vertical = CalloutPaddingVertical),
            style = DuiTextStyle.Counter,
            maxLines = 1,
        )
    }
    Row(
        Modifier.calloutPosition(callout),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (callout.side) {
            Callout.Side.Start -> { label(); line(); dot() }
            Callout.Side.End -> { dot(); line(); label() }
        }
    }
}

/** Places the callout outside the parent's bounds without changing the parent's size. */
private fun Modifier.calloutPosition(callout: Callout): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = Int.MAX_VALUE))
    layout(0, 0) {
        val inset = CalloutInset.roundToPx()
        val y = (constraints.maxHeight * callout.topFraction).toInt() - placeable.height / 2
        val x = when (callout.side) {
            Callout.Side.Start -> inset - placeable.width
            Callout.Side.End -> constraints.maxWidth - inset
        }
        placeable.place(x, y)
    }
}

private val CompactCopyWidth = 520.dp
private val BodyMaxWidth = 460.dp
private val MarkSize = 72.dp
private const val HeroScale = 0.8f
private const val PulseMinAlpha = 0.25f
private const val PulseHalfMillis = 800

/** Below this the callouts would cover the copy, so they hide (the mockup's 980px breakpoint, per column). */
private val CalloutMinWidth = 460.dp
private val CalloutInset = 32.dp
private val CalloutLine = 56.dp
private val CalloutDot = 7.dp
private val CalloutPaddingVertical = 6.dp
