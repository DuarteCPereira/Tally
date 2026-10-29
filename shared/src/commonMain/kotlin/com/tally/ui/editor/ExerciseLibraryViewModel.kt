package com.tally.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.data.ExerciseRepository
import com.tally.domain.Exercise
import com.tally.ktx.runCatchingNonCancellation
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The exercise library used by the editor's pickers. Changes apply immediately. */
class ExerciseLibraryViewModel(
    private val exercises: ExerciseRepository,
    internal val photos: ExercisePhotos,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    val library: StateFlow<List<Exercise>> = exercises.exercises
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = emptyList())

    fun add(exercise: Exercise) {
        viewModelScope.launch { exercises.save(exercise) }
    }

    /** Removes the exercise from the library. Workouts that already use it keep their copy. */
    fun remove(exercise: Exercise) {
        viewModelScope.launch { runCatchingNonCancellation { exercises.delete(exercise.id) } }
    }
}
