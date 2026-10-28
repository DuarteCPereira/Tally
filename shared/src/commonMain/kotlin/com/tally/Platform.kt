package com.tally

import app.cash.sqldelight.db.SqlDriver

interface Platform { val name: String }
expect fun getPlatform(): Platform
expect fun createDatabaseDriver(): SqlDriver
expect fun createPlatformServices(): PlatformServices

interface ImageStorage { suspend fun copyPickedImage(sourcePath: String): String }
interface ImagePicker { suspend fun pickFromGallery(): String?; suspend fun takePhoto(): String? }
interface Feedback { fun vibrate(); fun playSound() }
interface TimerNotifier { fun schedule(endTimeEpochMillis: Long); fun cancel() }
data class PlatformServices(val imageStorage: ImageStorage, val imagePicker: ImagePicker, val feedback: Feedback, val timerNotifier: TimerNotifier)
