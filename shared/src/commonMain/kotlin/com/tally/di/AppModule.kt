package com.tally.di

import app.cash.sqldelight.db.SqlDriver
import com.tally.database.WorkoutDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Collects every `@Single`, `@Factory` and `@KoinViewModel` under `com.tally`, plus the database,
 * which has to be built from the platform's [SqlDriver].
 *
 * ViewModels that need a value from the screen that opens them mark it `@InjectedParam` and get it
 * from `koinViewModel(parameters = { parametersOf(value) })`.
 */
@Module
@ComponentScan("com.tally")
class AppModule {
    @Single
    fun database(driver: SqlDriver) = WorkoutDatabase(driver)
}
