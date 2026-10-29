package com.tally.ui.editor

import com.tally.ImagePicker
import com.tally.ImageStorage
import com.tally.fakes.Exercises
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExerciseFormViewModelTest {
    private val picker = FakePicker()
    private val storage = FakeStorage()

    @Test
    fun `Given a blank name when saving then it publishes a message and returns nothing`() = runTest {
        val sut = form(initial = null)

        assertNull(sut.save())
        assertEquals(expected = "Enter an exercise name.", actual = sut.state.value.message)
    }

    @Test
    fun `Given an invalid draft when editing then the stale message is cleared`() = runTest {
        val sut = form(initial = null)
        sut.save()

        sut.edit { copy(name = "Squat") }

        assertNull(sut.state.value.message)
    }

    @Test
    fun `Given an existing exercise when saving then the id is kept`() = runTest {
        val sut = form(initial = Exercises.squat)

        assertEquals(expected = Exercises.squat.id, actual = sut.save()?.id)
    }

    @Test
    fun `Given a picked photo then its stored copy becomes the image and the picker closes`() = runTest {
        val sut = form(initial = null)
        picker.result.complete("/picked.jpg")

        sut.pickPhoto(PhotoSource.GALLERY)

        assertEquals(expected = "/stored/picked.jpg", actual = sut.state.value.form.imagePath)
        assertFalse(sut.state.value.isPickingPhoto)
    }

    @Test
    fun `Given a cancelled picker then the image is unchanged`() = runTest {
        val sut = form(initial = Exercises.squat.copy(imagePath = "/old.jpg"))
        picker.result.complete(null)

        sut.pickPhoto(PhotoSource.CAMERA)

        assertEquals(expected = "/old.jpg", actual = sut.state.value.form.imagePath)
        assertFalse(sut.state.value.isPickingPhoto)
    }

    @Test
    fun `Given a picker is open when picking again then the second request is ignored`() = runTest {
        val sut = form(initial = null)

        sut.pickPhoto(PhotoSource.GALLERY)
        sut.pickPhoto(PhotoSource.CAMERA)

        assertTrue(sut.state.value.isPickingPhoto)
        assertEquals(expected = 1, actual = picker.calls)
    }

    @Test
    fun `Given storing the photo fails then it publishes a message and the picker closes`() = runTest {
        val sut = form(initial = null)
        storage.fail = true
        picker.result.complete("/picked.jpg")

        sut.pickPhoto(PhotoSource.GALLERY)

        assertNotNull(sut.state.value.message)
        assertNull(sut.state.value.form.imagePath)
        assertFalse(sut.state.value.isPickingPhoto)
    }

    private fun TestScope.form(initial: com.tally.domain.Exercise?) = ExerciseFormViewModel(
        initial = initial,
        photos = ExercisePhotos(picker, storage),
        scope = viewModelTestScope(),
    )

    private class FakePicker : ImagePicker {
        val result = CompletableDeferred<String?>()
        var calls = 0

        override suspend fun pickFromGallery(): String? = next()
        override suspend fun takePhoto(): String? = next()

        private suspend fun next(): String? {
            calls++
            return result.await()
        }
    }

    private class FakeStorage : ImageStorage {
        var fail = false

        override suspend fun copyPickedImage(sourcePath: String): String {
            check(!fail) { "disk full" }
            return "/stored/" + sourcePath.substringAfterLast('/')
        }
    }
}
