package com.tally.ui.workout

import com.tally.domain.Exercise
import com.tally.domain.Workout
import com.tally.domain.WorkoutContent
import com.tally.domain.WorkoutProgress

/** The workout being run, with its blocks and the library exercises they refer to. */
internal data class RunPlan(
    val workout: Workout,
    val content: WorkoutContent,
    val exercises: Map<String, Exercise>,
)

/** The exercise for the current step: the active superset member's, or the block's own. */
internal fun RunPlan.exerciseAt(progress: WorkoutProgress): Exercise? {
    val item = progress.currentItem ?: return null
    val member = content.members[item.id].orEmpty().getOrNull(progress.memberIndex ?: -1)
    return exercises[member?.exerciseId ?: item.exerciseId]
}

/** The superset panel for the current step, or `null` on a regular block. */
internal fun RunPlan.circuitAt(progress: WorkoutProgress): CircuitUi? {
    val item = progress.currentItem?.takeIf { progress.isSuperset } ?: return null
    return CircuitUi(
        members = content.members[item.id].orEmpty().sortedBy { it.position }.map { member ->
            CircuitMemberUi(name = exercises[member.exerciseId]?.name ?: "Exercise", reps = member.reps)
        },
        currentIndex = progress.memberIndex ?: 0,
        round = progress.roundNumber ?: 1,
        rounds = item.rounds,
    )
}
