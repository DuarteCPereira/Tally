package com.tally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.tally.installTallyContext
import com.tally.AndroidImagePickerHost
import android.net.Uri
import android.os.Build
import android.Manifest
import java.io.File

class MainActivity : ComponentActivity() {
    private var cameraOutput: Uri? = null
    private val galleryPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        AndroidImagePickerHost.pendingCameraUri = uri?.toString()
        pendingGalleryCallback?.invoke(uri?.toString())
        pendingGalleryCallback = null
    }
    private var pendingGalleryCallback: ((String?) -> Unit)? = null
    private var pendingCameraCallback: ((Boolean) -> Unit)? = null
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    private val cameraCapture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        pendingCameraCallback?.invoke(success)
        pendingCameraCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        installTallyContext(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        AndroidImagePickerHost.install(
            gallery = { callback ->
                pendingGalleryCallback = callback
                galleryPicker.launch(
                    androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            camera = { callback ->
                val file = File(cacheDir, "picked-${System.currentTimeMillis()}.jpg")
                cameraOutput = androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
                AndroidImagePickerHost.pendingCameraUri = cameraOutput.toString()
                pendingCameraCallback = callback
                cameraCapture.launch(cameraOutput!!)
            },
        )

        setContent {
            App()
        }
    }

    companion object {
        private const val REQUEST_IMAGE = 1
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
