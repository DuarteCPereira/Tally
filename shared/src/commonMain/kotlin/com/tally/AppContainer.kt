package com.tally

import com.tally.data.*
import com.tally.database.WorkoutDatabase

class AppContainer(
    val database: WorkoutDatabase = WorkoutDatabase(createDatabaseDriver()),
    val workouts: WorkoutRepository = SqlDelightWorkoutRepository(database),
    val exercises: ExerciseRepository = SqlDelightExerciseRepository(database),
    val settings: SettingsRepository = SqlDelightSettingsRepository(database),
    val history: HistoryRepository = SqlDelightHistoryRepository(database),
    val platform: PlatformServices = createPlatformServices(),
)
