package com.tally.ui.editor

import androidx.lifecycle.ViewModel
import com.tally.domain.Exercise
import com.tally.domain.newId
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** What the superset dialog shows: the raw [form] and a validation [message]. */
data class SupersetFormUiState(val form: SupersetForm, val message: String? = null)

/** Owns the superset dialog's draft, member ordering and validation, so the dialog only renders. */
class SupersetFormViewModel(
    private val initial: DraftBlock.Superset?,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    private val key = initial?.key ?: newId()
    private val _state = MutableStateFlow(SupersetFormUiState(initial?.let(SupersetForm::of) ?: SupersetForm()))
    val state: StateFlow<SupersetFormUiState> = _state.asStateFlow()

    /** Applies [transform] to the draft and clears any stale message. */
    fun edit(transform: SupersetForm.() -> SupersetForm) {
        _state.update { it.copy(form = it.form.transform(), message = null) }
    }

    fun addMember(exercise: Exercise) = edit { copy(members = members + exercise) }

    fun removeMember(index: Int) = edit { copy(members = members.filterIndexed { i, _ -> i != index }) }

    /** Moves the member at [from] to [to]; out-of-range moves leave the order unchanged. */
    fun moveMember(from: Int, to: Int) = edit { copy(members = members.swap(from, to)) }

    /** Returns the validated superset, or `null` after publishing why the draft is not valid. */
    fun save(): DraftBlock.Superset? = when (val result = _state.value.form.validate(key)) {
        is FormResult.Valid -> result.value
        is FormResult.Invalid -> {
            _state.update { it.copy(message = result.message) }
            null
        }
    }
}

/** Returns a copy with the elements at [from] and [to] swapped, or this list if either is out of range. */
internal fun <T> List<T>.swap(from: Int, to: Int): List<T> {
    if (from !in indices || to !in indices) return this
    return toMutableList().apply {
        this[from] = this@swap[to]
        this[to] = this@swap[from]
    }
}
