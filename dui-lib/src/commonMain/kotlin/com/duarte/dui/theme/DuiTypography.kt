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
import org.jetbrains.compose.resources.Font

/**
 * The five type styles from `tokens.json`. Styles carry no colour; components pick a colour token.
 * Read them through [DuiTheme.typography].
 */
@Immutable
data class DuiTypography(
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
) {
    companion object {
        fun from(fontFamily: FontFamily) = DuiTypography(
            display = TextStyle(fontFamily = fontFamily, fontSize = 40.sp, lineHeight = 44.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
            title = TextStyle(fontFamily = fontFamily, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
            label = TextStyle(fontFamily = fontFamily, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
            body = TextStyle(fontFamily = fontFamily, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
            caption = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
        )
    }
}

/** Inter 400 and 500, bundled as Compose resources so text renders the same on Android and iOS. */
@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
)

val LocalDuiTypography = staticCompositionLocalOf { DuiTypography.from(FontFamily.Default) }
