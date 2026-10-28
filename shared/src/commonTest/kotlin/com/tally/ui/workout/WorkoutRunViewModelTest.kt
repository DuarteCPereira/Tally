package com.tally.ui.workout

import com.tally.Feedback
import com.tally.TimerNotifier
import com.tally.domain.RestMode
import com.tally.domain.RestTimer
import com.tally.domain.WorkoutSettings
import com.tally.fakes.Exercises
import com.tally.fakes.FakeExerciseRepository
import com.tally.fakes.FakeHistoryRepository
import com.tally.fakes.FakeSettingsRepository
import com.tally.fakes.FakeWorkoutRepository
import com.tally.fakes.Workouts
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutRunViewModelTest {
    private val history = FakeHistoryRepository()

    @Test
    fun `Given the workout no longer exists then the run is unavailable`() = runTest {
        val sut = run(workoutId = "missing")

        assertEquals(expected = RunUiState.Unavailable, actual = sut.state.value)
    }

    @Test
    fun `When the first step loads then it shows the exercise and its target reps`() = runTest {
        val sut = run()

        val active = assertIs<RunUiState.Active>(sut.state.value)
        assertEquals(expected = Exercises.squat, actual = active.exercise)
        assertEquals(expected = "Set 1", actual = active.stepTitle)
        assertEquals(expected = 5, actual = active.actualReps)
        assertNull(active.rest)
    }

    @Test
    fun `Given auto rest when a set is completed then rest counts down and closes at zero`() = runTest {
        val sut = run()

        sut.onAction(RunAction.CompleteSet)
        assertEquals(expected = RestUi.Running(remainingSeconds = 90, isPaused = false), actual = rest(sut))

        advanceTimeBy(30.seconds)
        runCurrent()
        assertEquals(expected = RestUi.Running(remainingSeconds = 60, isPaused = false), actual = rest(sut))

        advanceTimeBy(61.seconds)
        runCurrent()
        assertNull(rest(sut))
    }

    @Test
    fun `Given manual rest when a set is completed then rest waits to be started`() = runTest {
        val sut = run(settings = WorkoutSettings(restMode = RestMode.MANUAL))

        sut.onAction(RunAction.CompleteSet)
        assertEquals(expected = RestUi.Ready(seconds = 90), actual = rest(sut))

        sut.onAction(RunAction.StartRest)
        assertEquals(expected = RestUi.Running(remainingSeconds = 90, isPaused = false), actual = rest(sut))
    }

    @Test
    fun `When going back during rest then the rest is cancelled and the target reps restored`() = runTest {
        val sut = run()
        sut.onAction(RunAction.AdjustReps(delta = -2))
        sut.onAction(RunAction.CompleteSet)

        sut.onAction(RunAction.Previous)

        val active = assertIs<RunUiState.Active>(sut.state.value)
        assertNull(active.rest)
        assertEquals(expected = "Set 1", actual = active.stepTitle)
        assertEquals(expected = 5, actual = active.actualReps)
    }

    @Test
    fun `When a superset member is reached then the circuit shows the current member and round`() = runTest {
        val sut = run()
        repeat(times = 2) {
            sut.onAction(RunAction.CompleteSet)
            sut.onAction(RunAction.SkipRest)
        }
        sut.onAction(RunAction.CompleteSet)

        val circuit = assertIs<RunUiState.Active>(sut.state.value).circuit
        assertEquals(
            expected = CircuitUi(
                members = listOf(CircuitMemberUi("Push-up", reps = 8), CircuitMemberUi("Ring row", reps = 8)),
                currentIndex = 1,
                round = 1,
                rounds = 2,
            ),
            actual = circuit,
        )
    }

    @Test
    fun `Given saving fails when retried then the session is saved once`() = runTest {
        val sut = run()
        repeat(times = 6) { sut.onAction(RunAction.Skip) }
        history.failNextSave = true

        sut.save()
        assertEquals(expected = SaveStatus.FAILED, actual = finished(sut).save)

        sut.save()
        sut.save()
        assertEquals(expected = SaveStatus.SAVED, actual = finished(sut).save)
        assertEquals(expected = 1, actual = history.storedSessions.value.orEmpty().size)
        assertEquals(expected = 6, actual = history.storedEntries.value.size)
    }

    private fun TestScope.run(
        workoutId: String = Workouts.legDay.id,
        settings: WorkoutSettings = WorkoutSettings(),
    ) = WorkoutRunViewModel(
        workoutId = workoutId,
        repositories = RunRepositories(
            workouts = FakeWorkoutRepository(
                listOf(Workouts.legDay),
                mapOf(Workouts.legDay.id to Workouts.legDayContent),
            ),
            exercises = FakeExerciseRepository(listOf(Exercises.squat, Exercises.pushUp, Exercises.row)),
            settings = FakeSettingsRepository(settings),
            history = history,
        ),
        createTimer = { timer() },
        scope = viewModelTestScope(),
    )

    /** A rest timer whose wall clock is the test scheduler's virtual time. */
    private fun TestScope.timer() = RestTimer(
        timerNotifier = object : TimerNotifier {
            override fun schedule(endTimeEpochMillis: Long) = Unit
            override fun cancel() = Unit
        },
        feedback = object : Feedback {
            override fun vibrate() = Unit
            override fun playSound() = Unit
        },
        nowMillis = { testScheduler.currentTime },
        scope = backgroundScope,
    )

    private fun rest(sut: WorkoutRunViewModel) = assertIs<RunUiState.Active>(sut.state.value).rest

    private fun finished(sut: WorkoutRunViewModel) = assertIs<RunUiState.Finished>(sut.state.value)
}
