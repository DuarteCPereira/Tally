package com.duarte.workouttracker

import com.duarte.workouttracker.data.*
import com.duarte.workouttracker.database.WorkoutDatabase

class AppContainer(
    val database: WorkoutDatabase = WorkoutDatabase(createDatabaseDriver()),
    val workouts: WorkoutRepository = SqlDelightWorkoutRepository(database),
    val exercises: ExerciseRepository = SqlDelightExerciseRepository(database),
    val settings: SettingsRepository = SqlDelightSettingsRepository(database),
    val history: HistoryRepository = SqlDelightHistoryRepository(database),
    val platform: PlatformServices = createPlatformServices(),
)
