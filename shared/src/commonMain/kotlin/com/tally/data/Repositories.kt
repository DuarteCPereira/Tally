package com.tally.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.tally.database.WorkoutDatabase
import com.tally.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlin.time.Clock

interface WorkoutRepository {
    val workouts: Flow<List<Workout>>
    suspend fun add(workout: Workout)
    suspend fun duplicate(id: String): Workout?
    suspend fun delete(id: String)
    suspend fun reorder(ids: List<String>)

    /** Reads the saved blocks of a workout, in order. */
    suspend fun content(workoutId: String): WorkoutContent

    /** Replaces every saved block of a workout. */
    suspend fun replaceContent(workoutId: String, content: WorkoutContent)
}

class SqlDelightWorkoutRepository(private val database: WorkoutDatabase) : WorkoutRepository {
    private val queries = database.exerciseQueries
    override val workouts: Flow<List<Workout>> = queries.selectAllWorkouts()
        .asFlow().mapToList(Dispatchers.Default)
        .map { rows -> rows.map { Workout(it.id, it.name, it.position.toInt(), Instant.parse(it.createdAt)) } }

    override suspend fun add(workout: Workout) {
        val position = queries.selectAllWorkouts().executeAsList().size
        queries.insertWorkout(workout.id, workout.name, position.toLong(), workout.createdAt.toString())
    }

    override suspend fun duplicate(id: String): Workout? {
        val source = queries.selectAllWorkouts().executeAsList().firstOrNull { it.id == id } ?: return null
        val now = Clock.System.now()
        val copy = Workout("${source.id}-copy-${now.toEpochMilliseconds()}", "${source.name} copy", 0, now)
        queries.transaction {
            val position = queries.selectAllWorkouts().executeAsList().size
            queries.insertWorkout(copy.id, copy.name, position.toLong(), copy.createdAt.toString())
            queries.selectWorkoutItems(id).executeAsList().forEachIndexed { index, item ->
                val copyItemId = "${item.id}-copy-${now.toEpochMilliseconds()}-$index"
                queries.insertWorkoutItem(copyItemId, copy.id, index.toLong(), item.type, item.exerciseId,
                    item.sets, item.rounds, item.reps, item.restSeconds, item.notes)
                queries.selectSupersetMembers(item.id).executeAsList().forEachIndexed { memberIndex, member ->
                    queries.insertSupersetMember("${member.id}-copy-${now.toEpochMilliseconds()}-$memberIndex", copyItemId,
                        memberIndex.toLong(), member.exerciseId, member.reps)
                }
            }
        }
        return copy.copy(position = queries.selectAllWorkouts().executeAsList().lastIndex)
    }

    override suspend fun delete(id: String) {
        queries.transaction {
            queries.deleteWorkout(id)
            queries.selectAllWorkouts().executeAsList().forEachIndexed { index, workout ->
                queries.updateWorkoutPosition(index.toLong(), workout.id)
            }
        }
    }

    override suspend fun content(workoutId: String): WorkoutContent {
        val items = queries.selectWorkoutItems(workoutId).executeAsList().map { row ->
            WorkoutItem(
                id = row.id,
                workoutId = row.workoutId,
                position = row.position.toInt(),
                type = WorkoutItemType.valueOf(row.type),
                exerciseId = row.exerciseId,
                sets = row.sets.toInt(),
                rounds = row.rounds.toInt(),
                reps = row.reps.toInt(),
                restSeconds = row.restSeconds.toInt(),
                notes = row.notes,
            )
        }
        val members = items.filter { it.type == WorkoutItemType.SUPERSET }.associate { item ->
            item.id to queries.selectSupersetMembers(item.id).executeAsList().map { row ->
                SupersetMember(row.id, row.itemId, row.position.toInt(), row.exerciseId, row.reps.toInt())
            }
        }
        return WorkoutContent(items, members)
    }

    override suspend fun replaceContent(workoutId: String, content: WorkoutContent) {
        queries.transaction {
            // Delete explicitly instead of relying on cascades, then write the new blocks.
            queries.deleteSupersetMembersForWorkout(workoutId)
            queries.deleteWorkoutItems(workoutId)
            content.items.forEach { item ->
                queries.insertWorkoutItem(item.id, workoutId, item.position.toLong(), item.type.name, item.exerciseId,
                    item.sets.toLong(), item.rounds.toLong(), item.reps.toLong(), item.restSeconds.toLong(), item.notes)
                content.members[item.id].orEmpty().forEach { member ->
                    queries.insertSupersetMember(member.id, item.id, member.position.toLong(), member.exerciseId, member.reps.toLong())
                }
            }
        }
    }

    override suspend fun reorder(ids: List<String>) {
        queries.transaction {
            // Move out of the target range first to avoid UNIQUE/order collisions in future schemas.
            queries.selectAllWorkouts().executeAsList().forEachIndexed { index, workout ->
                queries.updateWorkoutPosition((ids.size + index).toLong(), workout.id)
            }
            ids.forEachIndexed { index, id -> queries.updateWorkoutPosition(index.toLong(), id) }
        }
    }
}

interface ExerciseRepository {
    val exercises: Flow<List<Exercise>>
    suspend fun save(exercise: Exercise)
    suspend fun delete(id: String)
}

class SqlDelightExerciseRepository(private val database: WorkoutDatabase) : ExerciseRepository {
    private val queries = database.exerciseQueries
    override val exercises: Flow<List<Exercise>> = queries.selectAllExercises()
        .asFlow().mapToList(Dispatchers.Default)
        .map { rows -> rows.map { Exercise(it.id, it.name, it.defaultSets.toInt(), it.defaultReps.toInt(), it.defaultRestSeconds.toInt(), it.notes, it.imagePath) } }

    override suspend fun save(exercise: Exercise) {
        queries.upsertExercise(exercise.id, exercise.name, exercise.defaultSets.toLong(), exercise.defaultReps.toLong(),
            exercise.defaultRestSeconds.toLong(), exercise.notes, exercise.imagePath)
    }

    override suspend fun delete(id: String) { queries.deleteExercise(id) }
}

interface SettingsRepository {
    val settings: Flow<WorkoutSettings>
    suspend fun update(settings: WorkoutSettings)
}

class SqlDelightSettingsRepository(private val database: WorkoutDatabase) : SettingsRepository {
    private val queries = database.exerciseQueries
    override val settings: Flow<WorkoutSettings> = queries.selectSetting("restMode").asFlow().mapToOneOrNull(Dispatchers.Default)
        .combine(queries.selectSetting("sound").asFlow().mapToOneOrNull(Dispatchers.Default)) { restMode, sound -> restMode to sound }
        .combine(queries.selectSetting("vibration").asFlow().mapToOneOrNull(Dispatchers.Default)) { pair, vibration -> Triple(pair.first, pair.second, vibration) }
        .combine(queries.selectSetting("theme").asFlow().mapToOneOrNull(Dispatchers.Default)) { values, theme ->
            WorkoutSettings(
                restMode = values.first?.let { runCatching { RestMode.valueOf(it) }.getOrNull() } ?: RestMode.AUTO,
                sound = values.second?.toBooleanStrictOrNull() ?: true,
                vibration = values.third?.toBooleanStrictOrNull() ?: true,
                theme = theme?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            )
        }

    override suspend fun update(settings: WorkoutSettings) {
        queries.transaction {
            queries.upsertSetting("restMode", settings.restMode.name)
            queries.upsertSetting("sound", settings.sound.toString())
            queries.upsertSetting("vibration", settings.vibration.toString())
            queries.upsertSetting("theme", settings.theme.name)
        }
    }
}

interface HistoryRepository {
    val sessions: Flow<List<Session>>
    val stats: Flow<HistoryStats>
    suspend fun addSession(session: Session, entries: List<SessionEntry> = emptyList())
}

class SqlDelightHistoryRepository(private val database: WorkoutDatabase) : HistoryRepository {
    private val queries = database.exerciseQueries
    override val sessions: Flow<List<Session>> = queries.selectAllSessions()
        .asFlow().mapToList(Dispatchers.Default)
        .map { rows -> rows.map { Session(it.id, it.workoutId, it.workoutName, Instant.parse(it.startedAt), it.durationSeconds.toInt()) } }
    override val stats: Flow<HistoryStats> = combine(
        sessions,
        queries.selectAllSessionEntries().asFlow().mapToList(Dispatchers.Default),
    ) { allSessions, allEntries ->
        val weekAgo = Clock.System.now().minus(7, DateTimeUnit.DAY, TimeZone.UTC)
        HistoryStats(allSessions.size, allSessions.sumOf { it.durationSeconds.toLong() },
            allEntries.count { it.completed != 0L }, allSessions.count { it.startedAt >= weekAgo })
    }

    override suspend fun addSession(session: Session, entries: List<SessionEntry>) {
        queries.transaction {
            queries.insertSession(session.id, session.workoutId, session.workoutName, session.startedAt.toString(), session.durationSeconds.toLong())
            entries.forEachIndexed { index, entry ->
                queries.insertSessionEntry(entry.id, session.id, index.toLong(), entry.exerciseName, entry.itemType.name,
                    entry.setNumber?.toLong(), entry.roundNumber?.toLong(), entry.targetReps.toLong(), entry.actualReps?.toLong(),
                    if (entry.completed) 1L else 0L)
            }
        }
    }
}
