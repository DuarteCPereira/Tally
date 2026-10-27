package com.duarte.dui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The 4dp-based spacing scale from `tokens.json`. Read it through [DuiTheme.spacing]. */
@Immutable
data class DuiSpacing(
    /** 4: icon-to-label gaps and tight inline spacing. */
    val space1: Dp = 4.dp,
    /** 8: between related elements, such as a label and its amount. */
    val space2: Dp = 8.dp,
    /** 12: list row vertical padding. */
    val space3: Dp = 12.dp,
    /** 16: card padding and screen edge margin. */
    val space4: Dp = 16.dp,
    /** 24: between sections and horizontal button padding. */
    val space5: Dp = 24.dp,
    /** 32: large separation above a screen's main block. */
    val space6: Dp = 32.dp,
    /** 48: button height and minimum touch target. */
    val space7: Dp = 48.dp,
)

val LocalDuiSpacing = staticCompositionLocalOf { DuiSpacing() }
