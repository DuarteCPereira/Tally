package com.tally

import app.cash.sqldelight.db.SqlDriver

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun createDatabaseDriver(): SqlDriver

expect fun createPlatformServices(): PlatformServices

interface ImageStorage {
    /** Copies a picked image into app storage and returns the copy's path. */
    suspend fun copyPickedImage(sourcePath: String): String
}

interface ImagePicker {
    /** Returns the picked image's path, or `null` if the user cancelled. */
    suspend fun pickFromGallery(): String?

    /** Returns the captured photo's path, or `null` if the user cancelled. */
    suspend fun takePhoto(): String?
}

interface Feedback {
    fun vibrate()
    fun playSound()
}

/** Schedules a system notification for when a rest ends, so it fires even in the background. */
interface TimerNotifier {
    fun schedule(endTimeEpochMillis: Long)
    fun cancel()
}

data class PlatformServices(
    val imageStorage: ImageStorage,
    val imagePicker: ImagePicker,
    val feedback: Feedback,
    val timerNotifier: TimerNotifier,
)
