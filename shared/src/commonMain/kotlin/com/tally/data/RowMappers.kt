package com.tally.data

import com.tally.domain.Exercise
import com.tally.domain.Session
import com.tally.domain.SessionEntry
import com.tally.domain.SupersetMember
import com.tally.domain.Workout
import com.tally.domain.WorkoutItem
import com.tally.domain.WorkoutItemType
import kotlin.time.Instant
import com.tally.database.Exercise as ExerciseRow
import com.tally.database.Session as SessionRow
import com.tally.database.SessionEntry as SessionEntryRow
import com.tally.database.SupersetMember as SupersetMemberRow
import com.tally.database.Workout as WorkoutRow
import com.tally.database.WorkoutItem as WorkoutItemRow

internal fun WorkoutRow.toWorkout() = Workout(
    id = id,
    name = name,
    position = position.toInt(),
    createdAt = Instant.parse(createdAt),
)

internal fun WorkoutItemRow.toWorkoutItem() = WorkoutItem(
    id = id,
    workoutId = workoutId,
    position = position.toInt(),
    type = WorkoutItemType.valueOf(type),
    exerciseId = exerciseId,
    sets = sets.toInt(),
    rounds = rounds.toInt(),
    reps = reps.toInt(),
    restSeconds = restSeconds.toInt(),
    notes = notes,
)

internal fun SupersetMemberRow.toSupersetMember() = SupersetMember(
    id = id,
    itemId = itemId,
    position = position.toInt(),
    exerciseId = exerciseId,
    reps = reps.toInt(),
)

internal fun ExerciseRow.toExercise() = Exercise(
    id = id,
    name = name,
    defaultSets = defaultSets.toInt(),
    defaultReps = defaultReps.toInt(),
    defaultRestSeconds = defaultRestSeconds.toInt(),
    notes = notes,
    imagePath = imagePath,
)

internal fun SessionRow.toSession() = Session(
    id = id,
    workoutId = workoutId,
    workoutName = workoutName,
    startedAt = Instant.parse(startedAt),
    durationSeconds = durationSeconds.toInt(),
)

internal fun SessionEntryRow.toSessionEntry() = SessionEntry(
    id = id,
    sessionId = sessionId,
    position = position.toInt(),
    exerciseName = exerciseName,
    itemType = WorkoutItemType.valueOf(itemType),
    setNumber = setNumber?.toInt(),
    roundNumber = roundNumber?.toInt(),
    targetReps = targetReps.toInt(),
    actualReps = actualReps?.toInt(),
    completed = completed != 0L,
)
