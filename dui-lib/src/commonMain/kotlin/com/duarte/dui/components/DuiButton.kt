package com.duarte.dui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.duarte.dui.internal.DuiDimens
import com.duarte.dui.internal.DuiRawText
import com.duarte.dui.internal.LocalOnSurface
import com.duarte.dui.internal.duiClickable
import com.duarte.dui.internal.duiFocusRing
import com.duarte.dui.theme.DuiTheme

enum class DuiButtonStyle {
    /** `fill-primary` with `on-primary`. The main action; at most one per screen. */
    Primary,

    /** `accent` with `on-accent`. Only when the action must stand out, never alongside a primary. */
    Accent,

    /** `surface` with `text-primary`. Supporting actions. */
    Secondary,

    /** No fill, `accent` label. Inline or low-priority actions. */
    Ghost,
}

/** Button heights. */
enum class DuiButtonSize {
    /** 48 (`space-7`), full width. The app default. */
    Regular,

    /** 40, as wide as its label. Web headers and other tight spots. */
    Small,
}

/**
 * Pill-shaped button, full width at [DuiButtonSize.Regular] and label-wide at [DuiButtonSize.Small]. When [enabled] is false it uses `surface` with `text-disabled`
 * whatever the [style].
 */
@Composable
fun DuiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DuiButtonStyle = DuiButtonStyle.Primary,
    enabled: Boolean = true,
    size: DuiButtonSize = DuiButtonSize.Regular,
) {
    val colors = DuiTheme.colors
    val shape = DuiTheme.shapes.full
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val onSurface = LocalOnSurface.current
    val (fill, content) = when {
        !enabled -> (if (onSurface) colors.surfaceHigh else colors.surface) to colors.textDisabled
        style == DuiButtonStyle.Primary -> colors.fillPrimary to colors.onPrimary
        style == DuiButtonStyle.Accent -> colors.accent to colors.onAccent
        style == DuiButtonStyle.Secondary -> (if (onSurface) (if (pressed) colors.divider else colors.surfaceHigh) else (if (pressed) colors.surfaceHigh else colors.surface)) to colors.textPrimary
        else -> (if (pressed) (if (onSurface) colors.surfaceHigh else colors.surface) else Color.Transparent) to colors.accent
    }
    Box(
        modifier
            .then(if (size == DuiButtonSize.Regular) Modifier.fillMaxWidth() else Modifier)
            .height(if (size == DuiButtonSize.Regular) DuiTheme.spacing.space7 else DuiDimens.SmallButtonHeight)
            .duiFocusRing(source, shape)
            .duiClickable(source, Role.Button, enabled, onClick)
            .background(fill, shape)
            .padding(horizontal = if (size == DuiButtonSize.Regular) DuiTheme.spacing.space5 else DuiTheme.spacing.space4),
        contentAlignment = Alignment.Center,
    ) {
        DuiRawText(text, DuiTheme.typography.label, content, maxLines = 1)
    }
}
