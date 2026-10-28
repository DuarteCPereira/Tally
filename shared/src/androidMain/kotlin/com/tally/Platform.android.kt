package com.tally

import android.os.Build
import android.content.Context
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.app.NotificationCompat
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.tally.database.WorkoutDatabase
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun createDatabaseDriver(): SqlDriver = AndroidSqliteDriver(WorkoutDatabase.Schema, appContext, "workout.db")

// The host app can install its Context before constructing the container.
private lateinit var appContext: Context
fun installTallyContext(context: Context) { appContext = context.applicationContext }


/** Activity-result launchers must be registered by the host Activity. */
object AndroidImagePickerHost {
    private var galleryLauncher: (((String?) -> Unit) -> Unit)? = null
    private var cameraLauncher: (((Boolean) -> Unit) -> Unit)? = null

    fun install(
        gallery: (((String?) -> Unit) -> Unit),
        camera: (((Boolean) -> Unit) -> Unit),
    ) {
        galleryLauncher = gallery
        cameraLauncher = camera
    }

    suspend fun pickGallery(): String? = suspendCancellableCoroutine { continuation ->
        val launcher = galleryLauncher
        if (launcher == null) continuation.resume(null)
        else launcher { if (continuation.isActive) continuation.resume(it) }
    }

    suspend fun takePhoto(): String? = suspendCancellableCoroutine { continuation ->
        val launcher = cameraLauncher
        if (launcher == null) continuation.resume(null)
        else launcher { if (continuation.isActive) continuation.resume(if (it) pendingCameraUri else null) }
    }

    @Volatile var pendingCameraUri: String? = null
}

private class AndroidImageStorage : ImageStorage {
    override suspend fun copyPickedImage(sourcePath: String): String {
        val uri = Uri.parse(sourcePath)
        val input = (if (uri.scheme == "content") appContext.contentResolver.openInputStream(uri)
        else File(uri.path ?: sourcePath).inputStream())
            ?: throw IllegalArgumentException("Unable to open image: $sourcePath")
        val directory = File(appContext.filesDir, "images").apply { mkdirs() }
        val destination = File(directory, "${System.currentTimeMillis()}-${(uri.lastPathSegment ?: "image").substringAfterLast('/')}")
        input.use { source -> FileOutputStream(destination).use { output -> source.copyTo(output) } }
        return destination.absolutePath
    }
}

private class AndroidTimerNotifier : TimerNotifier {
    override fun schedule(endTimeEpochMillis: Long) {
        val context = appContext
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = timerPendingIntent(context)
        alarm.cancel(pending)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeEpochMillis, pending)
    }

    override fun cancel() {
        val context = appContext
        (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(timerPendingIntent(context))
    }
}

private fun timerPendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
    context, 1732, Intent(context, WorkoutTimerReceiver::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

class WorkoutTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Workout timer", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Workout complete")
            .setContentText("Your workout timer has ended.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_ID = "workout_timer"
        private const val NOTIFICATION_ID = 1732
    }
}

actual fun createPlatformServices() = PlatformServices(
    imageStorage = AndroidImageStorage(),
    imagePicker = object : ImagePicker {
        override suspend fun pickFromGallery(): String? = AndroidImagePickerHost.pickGallery()
        override suspend fun takePhoto(): String? = AndroidImagePickerHost.takePhoto()
    },
    feedback = object : Feedback {
        override fun vibrate() {
            val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            else @Suppress("DEPRECATION") vibrator.vibrate(80)
        }
        override fun playSound() { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80).apply { startTone(ToneGenerator.TONE_PROP_BEEP, 150); release() } }
    },
    timerNotifier = AndroidTimerNotifier(),
)
