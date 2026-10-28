package com.tally.domain

import kotlin.time.Instant

/** A named training plan shown on the home list, ordered by [position]. */
data class Workout(
    val id: String,
    val name: String,
    val position: Int,
    val createdAt: Instant,
)

enum class WorkoutItemType { REGULAR, SUPERSET }

/**
 * One block of a [Workout]. A [WorkoutItemType.REGULAR] block points at a single exercise and is
 * repeated [sets] times. A [WorkoutItemType.SUPERSET] block has no [exerciseId]: its exercises are
 * [SupersetMember]s that are cycled through for [rounds] rounds.
 */
data class WorkoutItem(
    val id: String,
    val workoutId: String,
    val position: Int,
    val type: WorkoutItemType,
    val exerciseId: String?,
    val sets: Int = 3,
    val rounds: Int = 1,
    val reps: Int = 10,
    val restSeconds: Int = 60,
    val notes: String? = null,
)

/** An exercise inside a superset [WorkoutItem], identified by [itemId]. */
data class SupersetMember(
    val id: String,
    val itemId: String,
    val position: Int,
    val exerciseId: String,
    val reps: Int,
)

/** The blocks of one workout, with the members of each superset keyed by item id. */
data class WorkoutContent(
    val items: List<WorkoutItem>,
    val members: Map<String, List<SupersetMember>>,
) {
    companion object {
        val Empty = WorkoutContent(items = emptyList(), members = emptyMap())
    }
}
