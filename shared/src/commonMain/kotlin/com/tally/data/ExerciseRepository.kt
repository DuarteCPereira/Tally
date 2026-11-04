package com.tally.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tally.database.WorkoutDatabase
import com.tally.domain.Exercise
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

interface ExerciseRepository {
    /** The exercise library, ordered by name. */
    val exercises: Flow<List<Exercise>>

    /** Inserts the exercise, or replaces the one with the same id. */
    suspend fun save(exercise: Exercise)

    suspend fun delete(id: String)
}

@Single(binds = [ExerciseRepository::class])
class SqlDelightExerciseRepository(
    database: WorkoutDatabase,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : ExerciseRepository {
    private val queries = database.exerciseQueries

    override val exercises: Flow<List<Exercise>> = queries.selectAllExercises()
        .asFlow()
        .mapToList(io)
        .map { rows -> rows.map { it.toExercise() } }

    override suspend fun save(exercise: Exercise) {
        withContext(io) { upsert(exercise) }
    }

    override suspend fun delete(id: String) {
        withContext(io) { queries.deleteExercise(id) }
    }

    private fun upsert(exercise: Exercise) {
        queries.upsertExercise(
            id = exercise.id,
            name = exercise.name,
            defaultSets = exercise.defaultSets.toLong(),
            defaultReps = exercise.defaultReps.toLong(),
            defaultRestSeconds = exercise.defaultRestSeconds.toLong(),
            notes = exercise.notes,
            imagePath = exercise.imagePath,
        )
    }
}
