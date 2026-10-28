package com.tally.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.data.WorkoutRepository
import com.tally.domain.Workout
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Content(val workouts: List<Workout>) : HomeUiState
}

class HomeViewModel(
    private val repository: WorkoutRepository,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    // Eagerly: the tab is created at app start so its state is ready before it is first shown
    val state: StateFlow<HomeUiState> = repository.workouts
        .map { workouts -> HomeUiState.Content(workouts.sortedBy { it.position }) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = HomeUiState.Loading)

    fun duplicate(workout: Workout) {
        viewModelScope.launch { repository.duplicate(workout.id) }
    }

    fun delete(workout: Workout) {
        viewModelScope.launch { repository.delete(workout.id) }
    }
}
