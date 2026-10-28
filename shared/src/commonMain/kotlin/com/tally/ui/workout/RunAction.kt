package com.tally.ui.workout

/** Everything the user can do while running a workout. */
sealed interface RunAction {
    data object CompleteSet : RunAction

    /** Skips the current set. Rest still follows, as it would after completing it. */
    data object Skip : RunAction

    /** Goes back one step to correct it, cancelling any rest in progress. */
    data object Previous : RunAction

    data class AdjustReps(val delta: Int) : RunAction

    /** Starts a rest that is waiting for the user (manual rest mode). */
    data object StartRest : RunAction

    data object ToggleRestPause : RunAction

    data class AdjustRest(val seconds: Int) : RunAction

    data object SkipRest : RunAction
}
