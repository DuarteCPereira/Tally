package com.duarte.dui.components

import androidx.compose.ui.graphics.Color
import com.duarte.dui.theme.DuiColors

/** Direction of a change. Picks `positive` or `negative`; the sign itself stays in the app's string. */
enum class DuiTrend { Up, Down }

internal fun DuiTrend.color(colors: DuiColors): Color = when (this) {
    DuiTrend.Up -> colors.positive
    DuiTrend.Down -> colors.negative
}
