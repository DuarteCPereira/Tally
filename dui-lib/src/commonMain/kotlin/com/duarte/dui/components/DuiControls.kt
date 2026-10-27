package com.duarte.dui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

/** Icon-only button with a `space-7` touch target. Always give it a [contentDescription]. */
@Composable
fun DuiIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false,
) {
    val colors = DuiTheme.colors
    val source = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(DuiTheme.spacing.space7)
            .duiFocusRing(source, DuiTheme.shapes.full)
            .clip(DuiTheme.shapes.full)
            .background(if (filled) colors.surface else Color.Transparent)
            .duiClickable(source, Role.Button, enabled, onClick),
        contentAlignment = Alignment.Center,
    ) {
        DuiIcon(icon, contentDescription, tint = if (enabled) colors.textPrimary else colors.textDisabled)
    }
}

/** Rep-style stepper: a minus button, a large [value] in `display`, and a plus button. */
@Composable
fun DuiStepper(
    value: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true,
    decrementLabel: String = "Decrease",
    incrementLabel: String = "Increase",
) {
    val spacing = DuiTheme.spacing
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
        DuiIconButton(DuiIcons.Minus, decrementLabel, onDecrement, enabled = decrementEnabled, filled = true)
        DuiText(
            value,
            Modifier.widthIn(min = spacing.space7 + spacing.space6),
            style = DuiTextStyle.Display,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
        )
        DuiIconButton(DuiIcons.Plus, incrementLabel, onIncrement, enabled = incrementEnabled, filled = true)
    }
}

/** Small pill on `accent-soft` with `accent` text. */
@Composable
fun DuiBadge(text: String, modifier: Modifier = Modifier) {
    DuiText(
        text,
        modifier
            .background(DuiTheme.colors.accentSoft, DuiTheme.shapes.full)
            .padding(horizontal = DuiTheme.spacing.space3, vertical = DuiTheme.spacing.space1),
        style = DuiTextStyle.Caption,
        color = DuiTheme.colors.accent,
        maxLines = 1,
    )
}

/** A tile with a `caption` label, an optional icon and a value. [emphasis] sets the value in `display`. */
@Composable
fun DuiStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    emphasis: Boolean = false,
) {
    val spacing = DuiTheme.spacing
    Column(
        modifier.background(DuiTheme.colors.surface, DuiTheme.shapes.lg).padding(spacing.space4),
        verticalArrangement = Arrangement.spacedBy(spacing.space5),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DuiText(label, Modifier.weight(1f), style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary, maxLines = 1)
            if (icon != null) DuiIcon(icon, contentDescription = null, tint = DuiTheme.colors.textSecondary)
        }
        DuiText(value, style = if (emphasis) DuiTextStyle.Display else DuiTextStyle.Title, maxLines = 1)
    }
}

/** Two to four options in one `surface` group; the selected one sits on `surface-high`. */
@Composable
fun DuiSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    Row(
        modifier.fillMaxWidth().background(colors.surface, DuiTheme.shapes.md).padding(spacing.space1).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space1),
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            val source = remember { MutableInteractionSource() }
            val shape = DuiTheme.shapes.sm
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = spacing.space7 - spacing.space2)
                    .duiFocusRing(source, shape)
                    .clip(shape)
                    .background(if (selected) colors.surfaceHigh else Color.Transparent)
                    .selectable(selected, source, indication = null, role = Role.RadioButton, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center,
            ) {
                DuiText(
                    option,
                    style = DuiTextStyle.Label,
                    color = if (selected) colors.textPrimary else colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

/** A setting row: optional icon, [title], optional [description] and an on/off switch. */
@Composable
fun DuiSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    leading: ImageVector? = null,
) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    val source = remember { MutableInteractionSource() }
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = spacing.space7)
            .duiFocusRing(source, DuiTheme.shapes.sm)
            .toggleable(checked, source, indication = null, role = Role.Switch, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        if (leading != null) DuiIcon(leading, contentDescription = null, tint = colors.accent)
        Column(Modifier.weight(1f)) {
            DuiText(title, style = DuiTextStyle.Label)
            if (description != null) DuiText(description, style = DuiTextStyle.Caption, color = colors.textSecondary)
        }
        Box(
            Modifier
                .size(DuiDimens.SwitchTrackWidth, DuiDimens.SwitchTrackHeight)
                .background(if (checked) colors.accent else colors.surfaceHigh, DuiTheme.shapes.full),
            contentAlignment = Alignment.CenterStart,
        ) {
            val inset = (DuiDimens.SwitchTrackHeight - DuiDimens.SwitchThumb) / 2
            val travel = DuiDimens.SwitchTrackWidth - DuiDimens.SwitchThumb - inset * 2
            Box(
                Modifier
                    .offset(x = inset + if (checked) travel else 0.dp)
                    .size(DuiDimens.SwitchThumb)
                    .background(if (checked) colors.onAccent else colors.textSecondary, DuiTheme.shapes.full),
            )
        }
    }
}

/** Thin `accent` progress bar on a `surface-high` track. [progress] is 0..1. */
@Composable
fun DuiProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val fraction = progress.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(DuiDimens.ProgressHeight)
            .background(DuiTheme.colors.surfaceHigh, DuiTheme.shapes.full)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) },
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(DuiDimens.ProgressHeight)
                .background(DuiTheme.colors.accent, DuiTheme.shapes.full),
        )
    }
}

/** Indeterminate spinner: an `accent` arc over a `surface-high` ring. */
@Composable
fun DuiLoadingIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1000, easing = LinearEasing), RepeatMode.Restart),
    )
    val colors = DuiTheme.colors
    Canvas(modifier.size(DuiTheme.spacing.space7)) {
        val stroke = Stroke(DuiDimens.IconStroke.toPx() * 2, cap = StrokeCap.Round)
        drawCircle(colors.surfaceHigh, style = stroke, radius = size.minDimension / 2 - stroke.width / 2)
        drawArc(
            colors.accent,
            startAngle = angle,
            sweepAngle = 90f,
            useCenter = false,
            style = stroke,
            topLeft = androidx.compose.ui.geometry.Offset(stroke.width / 2, stroke.width / 2),
            size = androidx.compose.ui.geometry.Size(size.width - stroke.width, size.height - stroke.width),
        )
    }
}
