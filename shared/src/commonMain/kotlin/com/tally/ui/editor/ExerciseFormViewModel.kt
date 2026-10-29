package com.tally.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.domain.Exercise
import com.tally.domain.newId
import com.tally.ktx.runCatchingNonCancellation
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** What the exercise dialog shows: the raw [form], a validation or photo [message], and whether a picker is open. */
data class ExerciseFormUiState(
    val form: ExerciseForm,
    val message: String? = null,
    val isPickingPhoto: Boolean = false,
)

/** Owns the exercise dialog's draft, its validation and the photo picking, so the dialog only renders. */
class ExerciseFormViewModel(
    initial: Exercise?,
    private val photos: ExercisePhotos,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    private val id = initial?.id ?: newId()
    private val _state = MutableStateFlow(ExerciseFormUiState(initial?.let(ExerciseForm::of) ?: ExerciseForm()))
    val state: StateFlow<ExerciseFormUiState> = _state.asStateFlow()

    /** Applies [transform] to the draft and clears any stale message. */
    fun edit(transform: ExerciseForm.() -> ExerciseForm) {
        _state.update { it.copy(form = it.form.transform(), message = null) }
    }

    /** Opens the picker for [source]. Ignored while a picker is already open, so taps cannot stack pickers. */
    fun pickPhoto(source: PhotoSource) {
        if (!_state.compareAndSetPicking()) return
        viewModelScope.launch {
            runCatchingNonCancellation { photos.take(source) }
                .onSuccess { path -> finishPicking { if (path != null) copy(form = form.copy(imagePath = path)) else this } }
                .onFailure { finishPicking { copy(message = PHOTO_FAILED) } }
        }
    }

    /** Returns the validated exercise, or `null` after publishing why the draft is not valid. */
    fun save(): Exercise? = when (val result = _state.value.form.validate(id)) {
        is FormResult.Valid -> result.value
        is FormResult.Invalid -> {
            _state.update { it.copy(message = result.message) }
            null
        }
    }

    private fun MutableStateFlow<ExerciseFormUiState>.compareAndSetPicking(): Boolean {
        while (true) {
            val current = value
            if (current.isPickingPhoto) return false
            if (compareAndSet(current, current.copy(isPickingPhoto = true, message = null))) return true
        }
    }

    private fun finishPicking(transform: ExerciseFormUiState.() -> ExerciseFormUiState) {
        _state.update { it.transform().copy(isPickingPhoto = false) }
    }

    private companion object {
        const val PHOTO_FAILED = "Could not save the photo. Try again."
    }
}
