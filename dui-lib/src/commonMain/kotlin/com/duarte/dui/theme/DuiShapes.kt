package com.duarte.dui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** The radius tokens from `tokens.json` as shapes. Read them through [DuiTheme.shapes]. */
@Immutable
data class DuiShapes(
    /** 8: chips and small controls. */
    val sm: Shape = RoundedCornerShape(8.dp),
    /** 12: inputs. */
    val md: Shape = RoundedCornerShape(12.dp),
    /** 16: cards and sheets. */
    val lg: Shape = RoundedCornerShape(16.dp),
    /** Fully rounded: buttons, avatars and pills. */
    val full: Shape = CircleShape,
)

val LocalDuiShapes = staticCompositionLocalOf { DuiShapes() }
