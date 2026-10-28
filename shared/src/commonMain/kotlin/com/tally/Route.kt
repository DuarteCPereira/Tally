package com.tally

import androidx.compose.ui.graphics.vector.ImageVector
import com.duarte.dui.components.DuiIcons

/** What the app is showing. The editor and a run are opened from Home and close back to it. */
internal sealed interface Route {
    /** A route drawn inside the scaffold, above the bottom bar. */
    sealed interface Scaffolded : Route

    data class Tab(val tab: AppTab) : Scaffolded

    /** Edits the workout with [workoutId], or creates a new one when it is `null`. */
    data class Editor(val workoutId: String?) : Scaffolded

    /** A running workout takes the whole screen, without the bottom bar. */
    data class Run(val workoutId: String) : Route

    companion object {
        val Home = Tab(AppTab.HOME)
    }
}

internal enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", DuiIcons.Home),
    HISTORY("History", DuiIcons.History),
    SETTINGS("Settings", DuiIcons.Settings),
}
