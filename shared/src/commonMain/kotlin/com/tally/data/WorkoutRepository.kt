package com.tally.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tally.database.ExerciseQueries
import com.tally.database.WorkoutDatabase
import com.tally.domain.Workout
import com.tally.domain.WorkoutContent
import com.tally.domain.WorkoutItem
import com.tally.domain.WorkoutItemType
import com.tally.domain.newId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import org.koin.core.annotation.Single

interface WorkoutRepository {
    /** All workouts, ordered by position. Emits again whenever a workout changes. */
    val workouts: Flow<List<Workout>>

    suspend fun workout(id: String): Workout?

    /** Reads the saved blocks of a workout, in order. */
    suspend fun content(workoutId: String): WorkoutContent

    /**
     * Saves [workout] with its [content] in one transaction. A new workout is appended to the end
     * of the list; an existing one keeps its position and only has its name and blocks replaced.
     */
    suspend fun save(workout: Workout, content: WorkoutContent)

    /** Appends a copy of the workout and all its blocks, or returns `null` if it doesn't exist. */
    suspend fun duplicate(id: String): Workout?

    suspend fun delete(id: String)

    suspend fun reorder(ids: List<String>)
}

@Single(binds = [WorkoutRepository::class])
class SqlDelightWorkoutRepository(
    database: WorkoutDatabase,
    private val io: CoroutineDispatcher = Dispatchers.IO,
    private val clock: Clock = Clock.System,
) : WorkoutRepository {
    private val queries = database.exerciseQueries

    override val workouts: Flow<List<Workout>> = queries.selectAllWorkouts()
        .asFlow()
        .mapToList(io)
        .map { rows -> rows.map { it.toWorkout() } }

    override suspend fun workout(id: String): Workout? = withContext(io) {
        queries.selectWorkout(id).executeAsOneOrNull()?.toWorkout()
    }

    override suspend fun content(workoutId: String): WorkoutContent = withContext(io) {
        queries.readContent(workoutId)
    }

    override suspend fun save(workout: Workout, content: WorkoutContent) = withContext(io) {
        queries.transaction {
            if (queries.selectWorkout(workout.id).executeAsOneOrNull() == null) {
                queries.appendWorkout(workout)
            } else {
                queries.updateWorkoutName(workout.name, workout.id)
            }
            queries.replaceContent(workout.id, content)
        }
    }

    override suspend fun duplicate(id: String): Workout? = withContext(io) {
        queries.transactionWithResult {
            val source = queries.selectWorkout(id).executeAsOneOrNull()?.toWorkout()
                ?: return@transactionWithResult null
            val copy = Workout(
                id = newId(),
                name = "${source.name} copy",
                position = queries.countWorkouts().executeAsOne().toInt(),
                createdAt = clock.now(),
            )
            queries.appendWorkout(copy)
            queries.replaceContent(copy.id, queries.readContent(id).copyFor(copy.id))
            copy
        }
    }

    override suspend fun delete(id: String) = withContext(io) {
        queries.transaction {
            queries.deleteWorkout(id)
            queries.selectAllWorkouts().executeAsList().forEachIndexed { index, workout ->
                queries.updateWorkoutPosition(index.toLong(), workout.id)
            }
        }
    }

    override suspend fun reorder(ids: List<String>) = withContext(io) {
        queries.transaction {
            // Move out of the target range first to avoid UNIQUE/order collisions in future schemas
            queries.selectAllWorkouts().executeAsList().forEachIndexed { index, workout ->
                queries.updateWorkoutPosition((ids.size + index).toLong(), workout.id)
            }
            ids.forEachIndexed { index, id -> queries.updateWorkoutPosition(index.toLong(), id) }
        }
    }
}

private fun ExerciseQueries.appendWorkout(workout: Workout) {
    insertWorkout(
        id = workout.id,
        name = workout.name,
        position = countWorkouts().executeAsOne(),
        createdAt = workout.createdAt.toString(),
    )
}

private fun ExerciseQueries.readContent(workoutId: String): WorkoutContent {
    val items = selectWorkoutItems(workoutId).executeAsList().map { it.toWorkoutItem() }
    val members = items
        .filter { it.type == WorkoutItemType.SUPERSET }
        .associate { item ->
            item.id to selectSupersetMembers(item.id).executeAsList().map { it.toSupersetMember() }
        }
    return WorkoutContent(items, members)
}

private fun ExerciseQueries.replaceContent(workoutId: String, content: WorkoutContent) {
    // Delete explicitly instead of relying on cascades, then write the new blocks
    deleteSupersetMembersForWorkout(workoutId)
    deleteWorkoutItems(workoutId)
    content.items.forEach { item ->
        insert(item.copy(workoutId = workoutId))
        content.members[item.id].orEmpty().forEach { member ->
            insertSupersetMember(
                id = member.id,
                itemId = item.id,
                position = member.position.toLong(),
                exerciseId = member.exerciseId,
                reps = member.reps.toLong(),
            )
        }
    }
}

private fun ExerciseQueries.insert(item: WorkoutItem) {
    insertWorkoutItem(
        id = item.id,
        workoutId = item.workoutId,
        position = item.position.toLong(),
        type = item.type.name,
        exerciseId = item.exerciseId,
        sets = item.sets.toLong(),
        rounds = item.rounds.toLong(),
        reps = item.reps.toLong(),
        restSeconds = item.restSeconds.toLong(),
        notes = item.notes,
    )
}

/** Returns this content with fresh ids for every block and member, owned by [workoutId]. */
private fun WorkoutContent.copyFor(workoutId: String): WorkoutContent {
    val newItemIds = items.associate { it.id to newId() }
    return WorkoutContent(
        items = items.map { it.copy(id = newItemIds.getValue(it.id), workoutId = workoutId) },
        members = members.entries.associate { (itemId, itemMembers) ->
            val newItemId = newItemIds.getValue(itemId)
            newItemId to itemMembers.map { it.copy(id = newId(), itemId = newItemId) }
        },
    )
}
