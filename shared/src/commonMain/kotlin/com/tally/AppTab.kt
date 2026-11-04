package com.tally

import androidx.compose.ui.graphics.vector.ImageVector
import com.duarte.dui.components.DuiIcons

/** A tab of the bottom bar. Which one is selected is UI state, not part of the navigation back stack. */
internal enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", DuiIcons.Home),
    HISTORY("History", DuiIcons.History),
    SETTINGS("Settings", DuiIcons.Settings),
}
