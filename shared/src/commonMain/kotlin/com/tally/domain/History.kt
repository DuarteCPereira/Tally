package com.tally.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** A finished run of a workout. The workout name is copied so history survives renames. */
data class Session(
    val id: String,
    val workoutId: String,
    val workoutName: String,
    val startedAt: Instant,
    val durationSeconds: Int,
)

/** One set (regular block) or one member of one round (superset block) of a [Session]. */
data class SessionEntry(
    val id: String,
    val sessionId: String,
    val position: Int,
    val exerciseName: String,
    val itemType: WorkoutItemType,
    val setNumber: Int?,
    val roundNumber: Int?,
    val targetReps: Int,
    val actualReps: Int?,
    val completed: Boolean,
)

data class HistoryStats(
    val totalSessions: Int,
    val totalTime: Duration,
    val totalSets: Int,
    val thisWeekCount: Int,
) {
    companion object {
        private val Week = 7.days

        /** Summarises [sessions] and their [entries]; "this week" means the 7 days before [now]. */
        fun of(sessions: List<Session>, entries: List<SessionEntry>, now: Instant) = HistoryStats(
            totalSessions = sessions.size,
            totalTime = sessions.sumOf { it.durationSeconds.toLong() }.seconds,
            totalSets = entries.count { it.completed },
            thisWeekCount = sessions.count { it.startedAt >= now - Week },
        )
    }
}
