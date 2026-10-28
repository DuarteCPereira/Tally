package com.tally.ui.editor

import com.tally.domain.Exercise
import com.tally.domain.SupersetMember
import com.tally.domain.WorkoutContent
import com.tally.domain.WorkoutItem
import com.tally.domain.WorkoutItemType

/** A block of the workout being edited. [key] is stable across reorders, for list identity. */
sealed interface DraftBlock {
    val key: String

    /** A regular block. Its sets, reps, rest and notes are the [exercise]'s library defaults. */
    data class Single(override val key: String, val exercise: Exercise) : DraftBlock

    /** A superset: every member is done for [reps] reps, [rounds] times, resting between rounds. */
    data class Superset(
        override val key: String,
        val members: List<Exercise>,
        val rounds: Int,
        val reps: Int,
        val restSeconds: Int,
    ) : DraftBlock
}

/** Turns the editor's draft blocks into the rows saved for a workout, with fresh ids. */
internal fun List<DraftBlock>.toContent(workoutId: String, newId: () -> String): WorkoutContent {
    val items = mutableListOf<WorkoutItem>()
    val members = mutableMapOf<String, List<SupersetMember>>()
    forEachIndexed { position, block ->
        val itemId = newId()
        when (block) {
            is DraftBlock.Single -> items += block.exercise.toWorkoutItem(itemId, workoutId, position)
            is DraftBlock.Superset -> {
                items += block.toWorkoutItem(itemId, workoutId, position)
                members[itemId] = block.members.mapIndexed { index, member ->
                    SupersetMember(newId(), itemId, position = index, member.id, block.reps)
                }
            }
        }
    }
    return WorkoutContent(items, members)
}

/**
 * Rebuilds draft blocks from saved [WorkoutContent], resolving exercises from the [library].
 * Blocks or members whose exercise is no longer in the library are left out.
 */
internal fun WorkoutContent.toDraftBlocks(library: List<Exercise>, newKey: () -> String): List<DraftBlock> {
    val exercises = library.associateBy { it.id }
    return items.sortedBy { it.position }.mapNotNull { item ->
        when (item.type) {
            WorkoutItemType.REGULAR -> exercises[item.exerciseId]?.let { DraftBlock.Single(newKey(), it) }
            WorkoutItemType.SUPERSET -> DraftBlock.Superset(
                key = newKey(),
                members = members[item.id].orEmpty()
                    .sortedBy { it.position }
                    .mapNotNull { exercises[it.exerciseId] },
                rounds = item.rounds,
                reps = item.reps,
                restSeconds = item.restSeconds,
            )
        }
    }
}

private fun Exercise.toWorkoutItem(id: String, workoutId: String, position: Int) = WorkoutItem(
    id = id,
    workoutId = workoutId,
    position = position,
    type = WorkoutItemType.REGULAR,
    exerciseId = this.id,
    sets = defaultSets,
    rounds = 1,
    reps = defaultReps,
    restSeconds = defaultRestSeconds,
    notes = notes,
)

private fun DraftBlock.Superset.toWorkoutItem(id: String, workoutId: String, position: Int) = WorkoutItem(
    id = id,
    workoutId = workoutId,
    position = position,
    type = WorkoutItemType.SUPERSET,
    exerciseId = null,
    sets = 1,
    rounds = rounds,
    reps = reps,
    restSeconds = restSeconds,
)
