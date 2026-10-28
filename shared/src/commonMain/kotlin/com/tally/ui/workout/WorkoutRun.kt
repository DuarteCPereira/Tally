package com.tally.ui.workout

import com.tally.domain.FinishedWorkout
import com.tally.domain.RestMode
import com.tally.domain.RestTimer
import com.tally.domain.WorkoutEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

/**
 * A workout in progress: steps through the [plan] with the [engine] and drives the rest overlay
 * with the [timer]. The rest overlay's phase lives here; the [timer] owns the remaining time.
 */
internal class WorkoutRun(
    private val plan: RunPlan,
    private val engine: WorkoutEngine,
    private val timer: RestTimer,
) {
    private sealed interface RestPhase {
        data object Hidden : RestPhase
        data object AwaitingStart : RestPhase
        data class Running(val isPaused: Boolean) : RestPhase
    }

    private val restPhase = MutableStateFlow<RestPhase>(RestPhase.Hidden)
    private val actualReps = MutableStateFlow(engine.state.value.targetReps ?: 0)

    /** Captured the moment the last step is done, so time spent on the summary isn't counted. */
    var finished: FinishedWorkout? = null
        private set

    fun handle(action: RunAction) {
        when (action) {
            RunAction.CompleteSet -> completeSet()
            RunAction.Skip -> skip()
            RunAction.Previous -> previous()
            is RunAction.AdjustReps -> actualReps.update { (it + action.delta).coerceAtLeast(0) }
            RunAction.StartRest -> startRest()
            RunAction.ToggleRestPause -> toggleRestPause()
            is RunAction.AdjustRest -> timer.addSeconds(action.seconds)
            RunAction.SkipRest -> hideRest()
        }
    }

    fun uiState(saveStatus: Flow<SaveStatus>): Flow<RunUiState> = combine(
        engine.state,
        timer.remainingSeconds,
        restPhase,
        actualReps,
        saveStatus,
    ) { progress, remaining, phase, reps, save ->
        if (progress.finished) {
            RunUiState.Finished(plan.workout.name, progress.completedCount, progress.total, save)
        } else {
            RunUiState.Active(
                workoutName = plan.workout.name,
                progress = progress,
                exercise = plan.exerciseAt(progress),
                circuit = plan.circuitAt(progress),
                actualReps = reps,
                rest = phase.toRestUi(remaining, suggested = progress.restSuggestedSeconds),
            )
        }
    }

    fun dispose() = timer.dispose()

    private fun completeSet() {
        engine.completeSet(actualReps.value)
        onStepChanged()
    }

    private fun skip() {
        timer.skip()
        engine.skip()
        onStepChanged()
    }

    private fun previous() {
        engine.previous()
        hideRest()
        engine.state.value.targetReps?.let { actualReps.value = it }
    }

    private fun startRest() {
        timer.start(engine.state.value.restSuggestedSeconds)
        restPhase.value = RestPhase.Running(isPaused = false)
    }

    private fun toggleRestPause() {
        val phase = restPhase.value as? RestPhase.Running ?: return
        if (phase.isPaused) timer.resume() else timer.pause()
        restPhase.value = phase.copy(isPaused = !phase.isPaused)
    }

    private fun hideRest() {
        timer.skip()
        restPhase.value = RestPhase.Hidden
    }

    /** After a step is completed or skipped: reset the reps and begin the suggested rest, if any. */
    private fun onStepChanged() {
        val progress = engine.state.value
        actualReps.value = progress.targetReps ?: 0
        when {
            progress.finished -> {
                finished = finished ?: engine.finish()
                restPhase.value = RestPhase.Hidden
            }

            progress.restSuggestedSeconds <= 0 -> restPhase.value = RestPhase.Hidden
            progress.restMode == RestMode.AUTO -> startRest()
            else -> restPhase.value = RestPhase.AwaitingStart
        }
    }

    /** A running rest disappears on its own once the timer reaches zero. */
    private fun RestPhase.toRestUi(remaining: Int, suggested: Int): RestUi? = when (this) {
        RestPhase.Hidden -> null
        RestPhase.AwaitingStart -> RestUi.Ready(seconds = suggested)
        is RestPhase.Running -> RestUi.Running(remaining, isPaused).takeIf { remaining > 0 }
    }
}
