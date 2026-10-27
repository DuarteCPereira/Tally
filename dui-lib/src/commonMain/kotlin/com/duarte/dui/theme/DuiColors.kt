package com.duarte.dui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The 15 colour tokens from `tokens.json`. Read them through [DuiTheme.colors]. */
@Immutable
data class DuiColors(
    /** Screen background. Everything sits on this. */
    val bg: Color,
    /** Cards, grouped lists and secondary buttons: one step off the background, no border. */
    val surface: Color,
    /** Pressed or selected state on a surface, and selected chips. */
    val surfaceHigh: Color,
    /** 1px hairline between list rows and sections. Never used as a box border. */
    val divider: Color,
    /** Headlines, amounts and body text on bg and surface. */
    val textPrimary: Color,
    /** Labels, captions, subtitles and inactive tabs on bg and surface. */
    val textSecondary: Color,
    /** Disabled labels only. Never use it for readable content. */
    val textDisabled: Color,
    /** Primary button fill. Inverts between themes. */
    val fillPrimary: Color,
    /** Label on [fillPrimary]. */
    val onPrimary: Color,
    /** The one accent: links, active tab, selected state, focus ring and the accent button. */
    val accent: Color,
    /** Label on [accent]. */
    val onAccent: Color,
    /** Quiet accent wash behind a selected row or badge. Pair with [accent] text. */
    val accentSoft: Color,
    /** Gains and success. Always shown with a + sign, never colour alone. */
    val positive: Color,
    /** Losses and errors. Always shown with a - sign or message, never colour alone. */
    val negative: Color,
    /** Dim layer behind dialogs and menus. */
    val scrim: Color,
    /** Keyboard and accessibility focus ring. Aliases [accent]. */
    val focus: Color = accent,
) {
    companion object {
        val Light = DuiColors(
            bg = Color(0xFFFFFFFF),
            surface = Color(0xFFF5F5F6),
            surfaceHigh = Color(0xFFECECEE),
            divider = Color(0xFFE4E4E7),
            textPrimary = Color(0xFF0B0B0C),
            textSecondary = Color(0xFF6B6B70),
            textDisabled = Color(0xFFA0A0A6),
            fillPrimary = Color(0xFF0B0B0C),
            onPrimary = Color(0xFFFFFFFF),
            accent = Color(0xFF1D5BF0),
            onAccent = Color(0xFFFFFFFF),
            accentSoft = Color(0xFFE8EFFE),
            positive = Color(0xFF0B7A3E),
            negative = Color(0xFFD62839),
            scrim = Color(0x66000000),
        )

        val Dark = DuiColors(
            bg = Color(0xFF0B0B0C),
            surface = Color(0xFF171719),
            surfaceHigh = Color(0xFF222226),
            divider = Color(0xFF2A2A2E),
            textPrimary = Color(0xFFF5F5F5),
            textSecondary = Color(0xFF8E8E93),
            textDisabled = Color(0xFF5A5A60),
            fillPrimary = Color(0xFFF5F5F5),
            onPrimary = Color(0xFF0B0B0C),
            accent = Color(0xFF6C9CFF),
            onAccent = Color(0xFF0B0B0C),
            accentSoft = Color(0xFF16233F),
            positive = Color(0xFF3DDC8A),
            negative = Color(0xFFFF6B70),
            scrim = Color(0xA6000000),
        )
    }
}

val LocalDuiColors = staticCompositionLocalOf { DuiColors.Light }
