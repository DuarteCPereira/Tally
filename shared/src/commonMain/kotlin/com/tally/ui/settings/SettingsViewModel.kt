package com.tally.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.data.SettingsRepository
import com.tally.domain.WorkoutSettings
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Content(val settings: WorkoutSettings) : SettingsUiState
}

class SettingsViewModel(
    private val repository: SettingsRepository,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    private val writes = Mutex()

    // Eagerly: the tab is created at app start so its state is ready before it is first shown
    val state: StateFlow<SettingsUiState> = repository.settings
        .map(SettingsUiState::Content)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = SettingsUiState.Loading)

    /**
     * Applies [change] to the stored settings. Writes are serialised and each one reads the latest
     * stored value, so quick consecutive changes never overwrite each other with stale settings.
     */
    fun update(change: WorkoutSettings.() -> WorkoutSettings) {
        viewModelScope.launch {
            writes.withLock { repository.update(repository.settings.first().change()) }
        }
    }
}
