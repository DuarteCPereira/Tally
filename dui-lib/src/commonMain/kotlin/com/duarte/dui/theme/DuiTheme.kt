package com.duarte.dui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember

/**
 * Provides the DUI tokens to [content]. Follows the system light/dark setting unless [darkTheme]
 * is passed explicitly.
 */
@Composable
fun DuiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DuiColors.Dark else DuiColors.Light
    val sans = interFontFamily()
    val mono = monoFontFamily()
    val typography = remember(sans, mono) { DuiTypography.from(sans, mono) }
    val selectionColors = remember(colors) {
        TextSelectionColors(handleColor = colors.accent, backgroundColor = colors.accentSoft)
    }
    CompositionLocalProvider(
        LocalDuiColors provides colors,
        LocalDuiTypography provides typography,
        LocalDuiSpacing provides DuiSpacing(),
        LocalDuiShapes provides DuiShapes(),
        LocalTextSelectionColors provides selectionColors,
        content = content,
    )
}

/** Access to the current DUI tokens, e.g. `DuiTheme.colors.textPrimary`. */
object DuiTheme {
    val colors: DuiColors
        @Composable @ReadOnlyComposable get() = LocalDuiColors.current
    val typography: DuiTypography
        @Composable @ReadOnlyComposable get() = LocalDuiTypography.current
    val spacing: DuiSpacing
        @Composable @ReadOnlyComposable get() = LocalDuiSpacing.current
    val shapes: DuiShapes
        @Composable @ReadOnlyComposable get() = LocalDuiShapes.current
}
