package com.tally.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.data.ExerciseRepository
import com.tally.data.WorkoutRepository
import com.tally.domain.Exercise
import com.tally.domain.Workout
import com.tally.domain.newId
import com.tally.ktx.runCatchingNonCancellation
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

sealed interface EditorUiState {
    data object Loading : EditorUiState

    /**
     * The draft being edited. Once [isSaved] the screen should close; the content stays in place
     * until it does, so leaving never shows an empty frame.
     */
    data class Content(
        val isNew: Boolean,
        val nameError: String?,
        val blocks: List<DraftBlock>,
        val isSaving: Boolean,
        val isSaved: Boolean,
    ) : EditorUiState
}

/**
 * Edits the workout identified by [workoutId], or a new one when it is `null`. Nothing is written
 * until [save].
 */
class WorkoutEditorViewModel(
    workoutId: String?,
    private val workouts: WorkoutRepository,
    private val exercises: ExerciseRepository,
    private val clock: Clock = Clock.System,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    private data class Draft(
        val workout: Workout?,
        val blocks: List<DraftBlock>,
        val nameError: String? = null,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false,
    )

    /** `null` while an existing workout is still loading. */
    private val draft = MutableStateFlow(if (workoutId == null) Draft(workout = null, emptyList()) else null)

    /**
     * The workout name. Compose state rather than a flow: a text field has to see each edit
     * synchronously, otherwise the cursor and IME composition jump while typing.
     */
    var name by mutableStateOf("")
        private set

    val state: StateFlow<EditorUiState> = draft
        .map { it.toUiState() }
        // A new workout has nothing to load, so it starts as content and never shows a loading frame
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = draft.value.toUiState())

    init {
        workoutId?.let(::load)
    }

    fun rename(value: String) {
        name = value
        draft.update { it?.copy(nameError = null) }
    }

    fun addExercise(exercise: Exercise) = editBlocks { it + DraftBlock.Single(newId(), exercise) }

    fun replaceExercise(key: String, exercise: Exercise) = editBlocks { blocks ->
        blocks.map { if (it.key == key) DraftBlock.Single(key, exercise) else it }
    }

    /** Replaces the superset with the same key, or appends it as a new block. */
    fun saveSuperset(superset: DraftBlock.Superset) = editBlocks { blocks ->
        if (blocks.any { it.key == superset.key }) {
            blocks.map { if (it.key == superset.key) superset else it }
        } else {
            blocks + superset
        }
    }

    /** Moves the block [offset] places up (negative) or down (positive), if there is room. */
    fun move(key: String, offset: Int) = editBlocks { blocks ->
        val from = blocks.indexOfFirst { it.key == key }
        val to = from + offset
        if (from < 0 || to !in blocks.indices) {
            blocks
        } else {
            blocks.toMutableList().apply { add(to, removeAt(from)) }
        }
    }

    fun remove(key: String) = editBlocks { blocks -> blocks.filterNot { it.key == key } }

    /** Validates and saves the workout and its exercises, then reports it as saved. */
    fun save() {
        val current = draft.value?.takeUnless { it.isSaving } ?: return
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            draft.update { it?.copy(nameError = "Give your workout a name first.") }
            return
        }

        draft.update { it?.copy(isSaving = true) }
        viewModelScope.launch {
            // A new workout's position is assigned by the repository when it is appended
            val workout = current.workout?.copy(name = trimmed)
                ?: Workout(id = newId(), name = trimmed, position = 0, createdAt = clock.now())
            runCatchingNonCancellation {
                // Regular blocks carry their exercise's edited defaults back into the library
                current.blocks
                    .filterIsInstance<DraftBlock.Single>()
                    .forEach { exercises.save(it.exercise) }
                workouts.save(workout, current.blocks.toContent(workout.id, ::newId))
            }.onSuccess {
                draft.update { it?.copy(isSaved = true) }
            }.onFailure {
                draft.update { it?.copy(isSaving = false, nameError = SAVE_FAILED) }
            }
        }
    }

    private fun load(id: String) {
        viewModelScope.launch {
            val workout = workouts.workout(id)
            val blocks = workouts.content(id).toDraftBlocks(exercises.exercises.first(), ::newId)
            name = workout?.name.orEmpty()
            draft.value = Draft(workout, blocks)
        }
    }

    private fun editBlocks(transform: (List<DraftBlock>) -> List<DraftBlock>) {
        draft.update { it?.copy(blocks = transform(it.blocks)) }
    }

    private fun Draft?.toUiState(): EditorUiState = this?.let {
        EditorUiState.Content(
            isNew = workout == null,
            nameError = nameError,
            blocks = blocks,
            isSaving = isSaving,
            isSaved = isSaved,
        )
    } ?: EditorUiState.Loading

    private companion object {
        const val SAVE_FAILED = "Couldn’t save this workout. Try again."
    }
}
