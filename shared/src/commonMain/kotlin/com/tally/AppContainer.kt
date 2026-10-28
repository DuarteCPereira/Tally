package com.tally

import com.tally.data.ExerciseRepository
import com.tally.data.HistoryRepository
import com.tally.data.SettingsRepository
import com.tally.data.SqlDelightExerciseRepository
import com.tally.data.SqlDelightHistoryRepository
import com.tally.data.SqlDelightSettingsRepository
import com.tally.data.SqlDelightWorkoutRepository
import com.tally.data.WorkoutRepository
import com.tally.database.WorkoutDatabase

/**
 * The composition root: builds the app's dependencies once. Only [App] reads it, to construct
 * ViewModels; screens never see it.
 */
class AppContainer(
    database: WorkoutDatabase = WorkoutDatabase(createDatabaseDriver()),
    val workouts: WorkoutRepository = SqlDelightWorkoutRepository(database),
    val exercises: ExerciseRepository = SqlDelightExerciseRepository(database),
    val settings: SettingsRepository = SqlDelightSettingsRepository(database),
    val history: HistoryRepository = SqlDelightHistoryRepository(database),
    val platform: PlatformServices = createPlatformServices(),
)
