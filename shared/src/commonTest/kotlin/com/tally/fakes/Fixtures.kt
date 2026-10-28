package com.tally.fakes

import com.tally.domain.Exercise
import com.tally.domain.SupersetMember
import com.tally.domain.Workout
import com.tally.domain.WorkoutContent
import com.tally.domain.WorkoutItem
import com.tally.domain.WorkoutItemType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.time.Instant

/**
 * A ViewModel scope for tests: runs work eagerly on the test scheduler and is cancelled with the
 * test, like `viewModelScope` is when the ViewModel is cleared.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.viewModelTestScope(): CoroutineScope =
    CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))

object Exercises {
    val squat = Exercise(id = "squat", name = "Back squat", defaultSets = 2, defaultReps = 5, defaultRestSeconds = 90)
    val pushUp = Exercise(id = "push-up", name = "Push-up", defaultReps = 12)
    val row = Exercise(id = "row", name = "Ring row", defaultReps = 10)
}

object Workouts {
    val legDay = Workout(id = "legs", name = "Leg day", position = 1, createdAt = Instant.fromEpochSeconds(0))
    val upper = Workout(id = "upper", name = "Upper body", position = 0, createdAt = Instant.fromEpochSeconds(0))

    /** Leg day: two sets of squats, then a two-round push-up / row superset with 30 s rest. */
    val legDayContent = WorkoutContent(
        items = listOf(
            WorkoutItem(
                id = "i1",
                workoutId = "legs",
                position = 0,
                type = WorkoutItemType.REGULAR,
                exerciseId = Exercises.squat.id,
                sets = 2,
                reps = 5,
                restSeconds = 90,
            ),
            WorkoutItem(
                id = "i2",
                workoutId = "legs",
                position = 1,
                type = WorkoutItemType.SUPERSET,
                exerciseId = null,
                sets = 1,
                rounds = 2,
                reps = 8,
                restSeconds = 30,
            ),
        ),
        members = mapOf(
            "i2" to listOf(
                SupersetMember(id = "m1", itemId = "i2", position = 0, exerciseId = Exercises.pushUp.id, reps = 8),
                SupersetMember(id = "m2", itemId = "i2", position = 1, exerciseId = Exercises.row.id, reps = 8),
            ),
        ),
    )
}
