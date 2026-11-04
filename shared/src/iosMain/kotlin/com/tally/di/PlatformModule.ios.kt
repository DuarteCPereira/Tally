package com.tally.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.tally.Feedback
import com.tally.IOSFeedback
import com.tally.IOSImagePicker
import com.tally.IOSImageStorage
import com.tally.IOSTimerNotifier
import com.tally.ImagePicker
import com.tally.ImageStorage
import com.tally.TimerNotifier
import com.tally.database.WorkoutDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

private const val DATABASE_NAME = "workout.db"

@Module
class IosPlatformModule {
    @Single
    fun driver(): SqlDriver = NativeSqliteDriver(WorkoutDatabase.Schema, DATABASE_NAME)

    @Single
    fun imageStorage(): ImageStorage = IOSImageStorage()

    @Single
    fun imagePicker(): ImagePicker = IOSImagePicker()

    @Single
    fun feedback(): Feedback = IOSFeedback()

    @Single
    fun timerNotifier(): TimerNotifier = IOSTimerNotifier()
}

actual val platformModule = IosPlatformModule().module()
