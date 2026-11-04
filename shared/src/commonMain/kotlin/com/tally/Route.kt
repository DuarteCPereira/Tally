package com.tally

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * A destination, and the key of its entry in the navigation back stack. [Main] is always the root
 * and the editor and a run are pushed on top of it. Serializable so the back stack survives the
 * app being recreated.
 */
@Serializable
internal sealed interface Route : NavKey {
    /** The bottom-bar tabs. Switching between them is not navigation, see [AppTab]. */
    @Serializable
    data object Main : Route

    /** Edits the workout with [workoutId], or creates a new one when it is `null`. Keeps the bottom bar. */
    @Serializable
    data class Editor(val workoutId: String?) : Route

    /** A running workout takes the whole screen, without the bottom bar. */
    @Serializable
    data class Run(val workoutId: String) : Route
}
