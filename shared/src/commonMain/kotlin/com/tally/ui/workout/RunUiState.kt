package com.tally.ui.workout

import com.tally.domain.Exercise
import com.tally.domain.WorkoutProgress

sealed interface RunUiState {
    data object Loading : RunUiState

    /** The workout no longer exists or could not be read. */
    data object Unavailable : RunUiState

    data class Active(
        val workoutName: String,
        val progress: WorkoutProgress,
        val exercise: Exercise?,
        val circuit: CircuitUi?,
        val actualReps: Int,
        val rest: RestUi?,
    ) : RunUiState {
        val stepTitle: String
            get() = if (progress.isSuperset) {
                "Round ${progress.roundNumber ?: 1}"
            } else {
                "Set ${progress.setNumber ?: 1}"
            }
    }

    data class Finished(
        val workoutName: String,
        val completed: Int,
        val total: Int,
        val save: SaveStatus,
    ) : RunUiState
}

enum class SaveStatus { IDLE, SAVING, FAILED, SAVED }

/** The superset being worked through: its members in order, and where the user is in it. */
data class CircuitUi(
    val members: List<CircuitMemberUi>,
    val currentIndex: Int,
    val round: Int,
    val rounds: Int,
)

data class CircuitMemberUi(val name: String, val reps: Int)

/** The rest overlay. Absent while no rest is shown. */
sealed interface RestUi {
    /** Manual rest mode: the suggested rest is shown and waits for the user to start it. */
    data class Ready(val seconds: Int) : RestUi

    data class Running(val remainingSeconds: Int, val isPaused: Boolean) : RestUi
}
