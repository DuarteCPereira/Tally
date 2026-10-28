package com.tally

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.tally.database.WorkoutDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.PhotosUI.*
import platform.UIKit.*
import platform.UserNotifications.*
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.UIKit.UIDevice
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_async
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun createDatabaseDriver(): SqlDriver = NativeSqliteDriver(WorkoutDatabase.Schema, "workout.db")

private var imagePickerContinuation: ((String?) -> Unit)? = null
private var activeImagePicker: UIViewController? = null

@OptIn(ExperimentalForeignApi::class)
private class NativeImageDelegate : NSObject(), PHPickerViewControllerDelegateProtocol,
    UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        val provider = result?.itemProvider
        if (provider == null || !provider.hasItemConformingToTypeIdentifier("public.image")) {
            finishImagePick(null, picker)
            return
        }
        provider.loadFileRepresentationForTypeIdentifier("public.image") { url, _ ->
            val output = url?.path?.let(::copyImageToDocuments)
            finishImagePick(output, picker)
        }
    }

    override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val path = image?.let {
            val directory = documentsImageDirectory()
            val file = directory.URLByAppendingPathComponent("camera-${NSDate().timeIntervalSince1970}.jpg")
            UIImageJPEGRepresentation(it, 0.9)?.writeToURL(file!!, true)
            file?.path
        }
        finishImagePick(path, picker)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) { finishImagePick(null, picker) }
}

private val nativeImageDelegate = NativeImageDelegate()

private fun finishImagePick(path: String?, picker: UIViewController) {
    dispatch_async(dispatch_get_main_queue()) {
        picker.dismissViewControllerAnimated(true, completion = null)
        imagePickerContinuation?.invoke(path)
        imagePickerContinuation = null
        activeImagePicker = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentsImageDirectory(): NSURL {
    val documents = NSFileManager.defaultManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
    val directory = documents.URLByAppendingPathComponent("images", true)!!
    NSFileManager.defaultManager.createDirectoryAtURL(directory, true, null, null)
    return directory
}

@OptIn(ExperimentalForeignApi::class)
private fun copyImageToDocuments(path: String): String? {
    val source = NSURL.fileURLWithPath(path)
    val destination = documentsImageDirectory().URLByAppendingPathComponent("picked-${NSDate().timeIntervalSince1970}.jpg")!!
    return if (NSFileManager.defaultManager.copyItemAtURL(source, destination, null)) destination.path else null
}

private class IOSImagePicker : ImagePicker {
    override suspend fun pickFromGallery(): String? = showPicker(camera = false)
    override suspend fun takePhoto(): String? = showPicker(camera = true)

    private suspend fun showPicker(camera: Boolean): String? = suspendCancellableCoroutine { continuation ->
        dispatch_async(dispatch_get_main_queue()) {
            val root = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (root == null) { continuation.resume(null); return@dispatch_async }
            imagePickerContinuation = { if (continuation.isActive) continuation.resume(it) }
            if (camera) {
                if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                    imagePickerContinuation = null
                    continuation.resume(null)
                    return@dispatch_async
                }
                val picker = UIImagePickerController().apply {
                    sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                    delegate = nativeImageDelegate
                }
                activeImagePicker = picker
                root.presentViewController(picker, true, null)
            } else {
                val config = PHPickerConfiguration().apply { filter = PHPickerFilter.imagesFilter; selectionLimit = 1 }
                val picker = PHPickerViewController(config).apply { delegate = nativeImageDelegate }
                activeImagePicker = picker
                root.presentViewController(picker, true, null)
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IOSImageStorage : ImageStorage {
    override suspend fun copyPickedImage(sourcePath: String): String {
        val source = NSURL.fileURLWithPath(sourcePath)
        val destination = documentsImageDirectory().URLByAppendingPathComponent("image-${NSDate().timeIntervalSince1970}.jpg")!!
        if (!NSFileManager.defaultManager.copyItemAtURL(source, destination, null)) {
            throw IllegalStateException("Could not copy image to Documents")
        }
        return destination.path!!
    }
}

private class IOSTimerNotifier : TimerNotifier {
    private val center = UNUserNotificationCenter.currentNotificationCenter()
    override fun schedule(endTimeEpochMillis: Long) {
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { _, _ -> }
        center.removePendingNotificationRequestsWithIdentifiers(listOf("workout-timer"))
        val content = UNMutableNotificationContent()
        content.setTitle("Workout complete")
        content.setBody("Your workout timer has ended.")
        content.setSound(UNNotificationSound.defaultSound)
        val seconds = ((endTimeEpochMillis - (NSDate().timeIntervalSince1970 * 1000).toLong()).coerceAtLeast(1000) / 1000.0)
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(seconds, false)
        val request = UNNotificationRequest.requestWithIdentifier("workout-timer", content, trigger)
        center.addNotificationRequest(request) { _ -> }
    }
    override fun cancel() { center.removePendingNotificationRequestsWithIdentifiers(listOf("workout-timer")) }
}

actual fun createPlatformServices() = PlatformServices(
    imageStorage = IOSImageStorage(),
    imagePicker = IOSImagePicker(),
    feedback = object : Feedback {
        override fun vibrate() { AudioServicesPlaySystemSound(kSystemSoundID_Vibrate) }
        override fun playSound() {
            val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
            generator.prepare()
            generator.impactOccurred()
        }
    },
    timerNotifier = IOSTimerNotifier(),
)
