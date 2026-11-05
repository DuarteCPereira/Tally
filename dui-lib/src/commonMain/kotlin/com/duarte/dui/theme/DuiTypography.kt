package com.duarte.dui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.duarte.dui.resources.Res
import com.duarte.dui.resources.inter_medium
import com.duarte.dui.resources.inter_regular
import com.duarte.dui.resources.jetbrains_mono_medium
import org.jetbrains.compose.resources.Font

/**
 * The type styles from `tokens.json`. Styles carry no colour; components pick a colour token.
 * Read them through [DuiTheme.typography].
 *
 * The app uses the five Inter styles from [display] to [caption]. The website adds [hero], and the
 * two JetBrains Mono styles [eyebrow] and [counter].
 */
@Immutable
data class DuiTypography(
    /** Web only: the one headline of a landing page. Steps down to [display] on phones. */
    val hero: TextStyle,
    /** The single most important number on a screen. One per screen. */
    val display: TextStyle,
    /** Screen and section titles. */
    val title: TextStyle,
    /** Buttons, list-row titles and values. */
    val label: TextStyle,
    /** Running text and descriptions. */
    val body: TextStyle,
    /** Subtitles, metadata, tab labels and chart ranges. */
    val caption: TextStyle,
    /** Web only: mono section index and small technical labels above a title ("01 · Build"). */
    val eyebrow: TextStyle,
    /** Mono live counts, timers and readouts in marketing visuals ("rest 01:30"). */
    val counter: TextStyle,
) {
    companion object {
        /** Builds the styles from the [sans] family (Inter) and the [mono] family (JetBrains Mono). */
        fun from(sans: FontFamily, mono: FontFamily = FontFamily.Monospace) = DuiTypography(
            hero = TextStyle(fontFamily = sans, fontSize = 64.sp, lineHeight = 68.sp, fontWeight = FontWeight.Medium, letterSpacing = (-1.5).sp),
            display = TextStyle(fontFamily = sans, fontSize = 40.sp, lineHeight = 44.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
            title = TextStyle(fontFamily = sans, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
            label = TextStyle(fontFamily = sans, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
            body = TextStyle(fontFamily = sans, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
            caption = TextStyle(fontFamily = sans, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
            eyebrow = TextStyle(fontFamily = mono, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
            counter = TextStyle(fontFamily = mono, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        )
    }
}

/** Inter 400 and 500, bundled as Compose resources so text renders the same on every platform. */
@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
)

/** JetBrains Mono 500, the only weight the `eyebrow` and `counter` styles use. */
@Composable
fun monoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
)

val LocalDuiTypography = staticCompositionLocalOf { DuiTypography.from(FontFamily.Default) }
