package com.tally.ui.editor

import com.tally.ImagePicker
import com.tally.ImageStorage

enum class PhotoSource { GALLERY, CAMERA }

/** Gets a photo for an exercise from the user and keeps a copy in app storage. */
class ExercisePhotos(private val picker: ImagePicker, private val storage: ImageStorage) {
    /** Returns the stored copy's path, or `null` if the user cancelled. */
    suspend fun take(source: PhotoSource): String? {
        val picked = when (source) {
            PhotoSource.GALLERY -> picker.pickFromGallery()
            PhotoSource.CAMERA -> picker.takePhoto()
        } ?: return null
        return storage.copyPickedImage(picked)
    }
}
