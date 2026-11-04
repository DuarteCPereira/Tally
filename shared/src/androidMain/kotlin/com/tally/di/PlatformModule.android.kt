package com.tally.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.tally.AndroidFeedback
import com.tally.AndroidImagePicker
import com.tally.AndroidImageStorage
import com.tally.AndroidTimerNotifier
import com.tally.Feedback
import com.tally.ImagePicker
import com.tally.ImageStorage
import com.tally.TimerNotifier
import com.tally.database.WorkoutDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

private const val DATABASE_NAME = "workout.db"

/** The Android [Context] is provided by `androidContext` when Koin starts. */
@Module
class AndroidPlatformModule {
    @Single
    fun driver(@Provided context: Context): SqlDriver =
        AndroidSqliteDriver(WorkoutDatabase.Schema, context, DATABASE_NAME)

    @Single
    fun imageStorage(@Provided context: Context): ImageStorage = AndroidImageStorage(context)

    @Single
    fun imagePicker(): ImagePicker = AndroidImagePicker()

    @Single
    fun feedback(@Provided context: Context): Feedback = AndroidFeedback(context)

    @Single
    fun timerNotifier(@Provided context: Context): TimerNotifier = AndroidTimerNotifier(context)
}

actual val platformModule = AndroidPlatformModule().module()
