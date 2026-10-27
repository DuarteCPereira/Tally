package com.duarte.workouttracker.domain

import kotlinx.datetime.Instant

enum class WorkoutItemType { REGULAR, SUPERSET }
enum class RestMode { AUTO, MANUAL }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class Exercise(
    val id: String,
    val name: String,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultRestSeconds: Int = 60,
    val notes: String? = null,
    val imagePath: String? = null,
)

data class Workout(val id: String, val name: String, val position: Int, val createdAt: Instant)

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

/** The blocks of one workout, with the members of each superset keyed by item id. */
data class WorkoutContent(val items: List<WorkoutItem>, val members: Map<String, List<SupersetMember>>)

data class SupersetMember(val id: String, val itemId: String, val position: Int, val exerciseId: String, val reps: Int)
data class WorkoutSettings(
    val restMode: RestMode = RestMode.AUTO,
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
)
data class Session(val id: String, val workoutId: String, val workoutName: String, val startedAt: Instant, val durationSeconds: Int)
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
data class HistoryStats(val totalSessions: Int, val totalTimeSeconds: Long, val totalSets: Int, val thisWeekCount: Int)
