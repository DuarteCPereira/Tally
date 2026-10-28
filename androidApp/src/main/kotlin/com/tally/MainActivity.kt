package com.tally

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.tally.domain.ThemeMode
import java.io.File

class MainActivity : ComponentActivity() {
    private var pendingGalleryCallback: ((String?) -> Unit)? = null
    private var pendingCameraCallback: ((Boolean) -> Unit)? = null
    private val galleryPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        AndroidImagePickerHost.pendingCameraUri = uri?.toString()
        pendingGalleryCallback?.invoke(uri?.toString())
        pendingGalleryCallback = null
    }
    private val cameraCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        pendingCameraCallback?.invoke(success)
        pendingCameraCallback = null
    }
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    /** Set once the app has drawn its first real frame; until then the system splash stays up. */
    private var isContentReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        installTallyContext(this)
        requestNotificationPermission()
        installImagePickers()
        keepSplashUntilContentReady()

        setContent {
            App(
                onReady = { isContentReady = true },
                onThemeChange = { splashScreen.setSplashScreenTheme(it.splashTheme) },
            )
        }
    }

    private fun requestNotificationPermission() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(permission)
        }
    }

    private fun installImagePickers() {
        AndroidImagePickerHost.install(
            gallery = { callback ->
                pendingGalleryCallback = callback
                galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            camera = { callback ->
                val file = File(cacheDir, "picked-${System.currentTimeMillis()}.jpg")
                val output = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                AndroidImagePickerHost.pendingCameraUri = output.toString()
                pendingCameraCallback = callback
                cameraCapture.launch(output)
            },
        )
    }

    /**
     * Holds the system splash screen by refusing to draw until [isContentReady], so the first frame
     * the user sees already has the saved theme and data instead of flashing defaults.
     */
    private fun keepSplashUntilContentReady() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (isContentReady) content.viewTreeObserver.removeOnPreDrawListener(this)
                    return isContentReady
                }
            },
        )
    }

    /** The splash theme matching an app theme, so a forced theme isn't preceded by the system's. */
    private val ThemeMode.splashTheme: Int
        get() = when (this) {
            ThemeMode.SYSTEM -> R.style.Theme_Tally
            ThemeMode.LIGHT -> R.style.Theme_Tally_Light
            ThemeMode.DARK -> R.style.Theme_Tally_Dark
        }
}
