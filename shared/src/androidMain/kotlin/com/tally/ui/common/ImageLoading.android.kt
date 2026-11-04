package com.tally.ui.common

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

@Composable
actual fun rememberImageBitmap(path: String?): ImageBitmap? = remember(path) {
    path?.let {
        runCatching {
            BitmapFactory.decodeFile(File(it).absolutePath)?.asImageBitmap()
        }.getOrNull()
    }
}
