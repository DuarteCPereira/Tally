package com.duarte.workouttracker.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberImageBitmap(path: String?): ImageBitmap? = remember(path) {
    path?.let {
        runCatching {
            val data = NSData.dataWithContentsOfFile(it) ?: return@runCatching null
            val image = UIImage(data = data) ?: return@runCatching null
            val png = UIImagePNGRepresentation(image) ?: return@runCatching null
            org.jetbrains.skia.Image.makeFromEncoded(png.bytes?.readBytes(png.length.toInt()) ?: return@runCatching null).toComposeImageBitmap()
        }.getOrNull()
    }
}
