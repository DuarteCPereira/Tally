package com.tally.ui.history

import com.tally.domain.HistoryStats
import com.tally.domain.Session
import com.tally.domain.SessionEntry
import com.tally.domain.WorkoutItemType
import kotlinx.datetime.TimeZone

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Content(val stats: StatsUi, val sessions: List<SessionUi>) : HistoryUiState
}

/** The four headline numbers, already formatted for display. */
data class StatsUi(
    val sessions: String,
    val totalTime: String,
    val totalSets: String,
    val thisWeek: String,
)

/** A session card: [summary] shows while collapsed, [exercises] while expanded. */
data class SessionUi(
    val id: String,
    val title: String,
    val date: String,
    val duration: String,
    val summary: String?,
    val exercises: List<ExerciseResultUi>,
)

data class ExerciseResultUi(val name: String, val lines: List<ResultLineUi>)

/** One set or round: "Set 2" with "10 reps", or "Round 1" with "Skipped". */
data class ResultLineUi(val label: String, val result: String, val done: Boolean)

internal fun HistoryStats.toStatsUi() = StatsUi(
    sessions = totalSessions.toString(),
    totalTime = formatDuration(totalTime),
    totalSets = totalSets.toString(),
    thisWeek = thisWeekCount.toString(),
)

/** Builds a card for this session from [entries], which must all belong to it. */
internal fun Session.toSessionUi(entries: List<SessionEntry>, timeZone: TimeZone): SessionUi {
    val ordered = entries.sortedBy { it.position }
    return SessionUi(
        id = id,
        title = workoutName,
        date = formatDateTime(startedAt, timeZone),
        duration = formatDuration(durationSeconds),
        summary = ordered.takeIf { it.isNotEmpty() }?.let { all ->
            "${all.map { it.exerciseName }.distinct().size} exercises  ·  " +
                "${all.count { it.completed }} sets"
        },
        exercises = ordered.groupBy { it.exerciseName }.map { (name, exerciseEntries) ->
            ExerciseResultUi(name, lines = exerciseEntries.toResultLines())
        },
    )
}

private fun List<SessionEntry>.toResultLines(): List<ResultLineUi> =
    if (any { it.itemType == WorkoutItemType.SUPERSET }) {
        map { entry ->
            val round = entry.roundNumber ?: entry.setNumber
            ResultLineUi(label = "Round ${round ?: "—"}", entry.result, entry.completed)
        }
    } else {
        sortedBy { it.setNumber ?: Int.MAX_VALUE }.map { entry ->
            ResultLineUi(label = "Set ${entry.setNumber ?: "—"}", entry.result, entry.completed)
        }
    }

private val SessionEntry.result: String
    get() = when {
        !completed -> "Skipped"
        else -> "${actualReps ?: targetReps} reps"
    }
