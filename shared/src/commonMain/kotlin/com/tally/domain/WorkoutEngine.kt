package com.tally.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock
import kotlin.time.Instant

/** Immutable snapshot of a running workout. Set and round numbers are one-based. */
data class WorkoutProgress(
    val currentItem: WorkoutItem?,
    val memberIndex: Int?,
    val setNumber: Int?,
    val roundNumber: Int?,
    val total: Int,
    val targetReps: Int?,
    val completedCount: Int,
    val isSuperset: Boolean,
    val restSuggestedSeconds: Int,
    val finished: Boolean,
    val restMode: RestMode,
    val canGoBack: Boolean,
) {
    val progress: Float get() = if (total == 0) 1f else completedCount.toFloat() / total
    val shouldAutoRest: Boolean get() = restMode == RestMode.AUTO && restSuggestedSeconds > 0
}

data class FinishedWorkout(val session: Session, val entries: List<SessionEntry>)

/**
 * Deterministic state machine for a workout. A superset is flattened round-by-round, with its
 * members consecutive; rest is suggested only between complete rounds.
 */
class WorkoutEngine(
    private val workout: Workout,
    items: List<WorkoutItem>,
    membersByItemId: Map<String, List<SupersetMember>> = emptyMap(),
    private val exerciseNames: Map<String, String> = emptyMap(),
    private val settings: WorkoutSettings = WorkoutSettings(),
    private val startedAt: Instant = Clock.System.now(),
    private val clock: Clock = Clock.System,
    private val idGenerator: () -> String = ::newId,
) {
    private data class Step(
        val item: WorkoutItem,
        val member: SupersetMember?,
        val memberIndex: Int?,
        val setNumber: Int?,
        val roundNumber: Int?,
        val targetReps: Int,
    )

    private data class StepResult(val actualReps: Int?, val completed: Boolean)

    private val steps: List<Step> = items.sortedBy { it.position }.flatMap { item ->
        when (item.type) {
            WorkoutItemType.REGULAR -> (1..item.sets.coerceAtLeast(0)).map { set ->
                Step(item, member = null, memberIndex = null, set, roundNumber = null, item.reps)
            }

            WorkoutItemType.SUPERSET -> {
                val members = membersByItemId[item.id].orEmpty().sortedBy { it.position }
                (1..item.rounds.coerceAtLeast(0)).flatMap { round ->
                    members.mapIndexed { index, member ->
                        Step(item, member, index, setNumber = null, round, member.reps)
                    }
                }
            }
        }
    }
    private val results = MutableList<StepResult?>(steps.size) { null }
    private var cursor = 0
    private var restSuggestion = 0
    private val _state = MutableStateFlow(snapshot())
    val state: StateFlow<WorkoutProgress> = _state.asStateFlow()

    /** Mark the current set/member complete with the performed rep count and advance. */
    fun completeSet(actualReps: Int) {
        require(actualReps >= 0) { "Actual reps cannot be negative" }
        if (cursor >= steps.size) return
        results[cursor] = StepResult(actualReps, completed = true)
        advance()
    }

    /** Skip the current set/member. It remains in the history as incomplete with no actual reps. */
    fun skip() {
        if (cursor >= steps.size) return
        results[cursor] = StepResult(actualReps = null, completed = false)
        advance()
    }

    /** Move back one step without erasing its recorded result, allowing it to be corrected. */
    fun previous() {
        if (cursor == 0) return
        cursor--
        restSuggestion = 0
        _state.value = snapshot()
    }

    /** Return the finished history payload, or null until all steps have been visited. */
    fun finish(): FinishedWorkout? {
        if (cursor < steps.size) return null
        val sessionId = idGenerator()
        val entries = steps.mapIndexed { index, step ->
            val result = results[index]
            SessionEntry(
                id = idGenerator(),
                sessionId = sessionId,
                position = index,
                exerciseName = step.exerciseName(),
                itemType = step.item.type,
                setNumber = step.setNumber,
                roundNumber = step.roundNumber,
                targetReps = step.targetReps,
                actualReps = result?.actualReps,
                completed = result?.completed == true,
            )
        }
        val session = Session(
            id = sessionId,
            workoutId = workout.id,
            workoutName = workout.name,
            startedAt = startedAt,
            durationSeconds = (clock.now() - startedAt).inWholeSeconds.coerceAtLeast(0).toInt(),
        )
        return FinishedWorkout(session, entries)
    }

    private fun Step.exerciseName(): String =
        (member?.exerciseId ?: item.exerciseId)?.let { exerciseNames[it] }.orEmpty()

    private fun advance() {
        val previousStep = steps[cursor]
        cursor++
        val next = steps.getOrNull(cursor)
        restSuggestion = if (next != null && isRestBoundary(previousStep, next)) {
            previousStep.item.restSeconds.coerceAtLeast(0)
        } else {
            0
        }
        _state.value = snapshot()
    }

    private fun isRestBoundary(previous: Step, next: Step): Boolean = when (previous.item.type) {
        WorkoutItemType.REGULAR -> true
        WorkoutItemType.SUPERSET ->
            previous.item.id != next.item.id || previous.roundNumber != next.roundNumber
    }

    private fun snapshot(): WorkoutProgress {
        val step = steps.getOrNull(cursor)
        return WorkoutProgress(
            currentItem = step?.item,
            memberIndex = step?.memberIndex,
            setNumber = step?.setNumber,
            roundNumber = step?.roundNumber,
            total = steps.size,
            targetReps = step?.targetReps,
            completedCount = results.count { it?.completed == true },
            isSuperset = step?.item?.type == WorkoutItemType.SUPERSET,
            restSuggestedSeconds = restSuggestion,
            finished = cursor >= steps.size,
            restMode = settings.restMode,
            canGoBack = cursor > 0,
        )
    }
}
