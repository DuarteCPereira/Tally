package com.tally

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.data.SettingsRepository
import com.tally.domain.ThemeMode
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

sealed interface AppUiState {
    /** The saved theme isn't known yet, so nothing is drawn rather than drawing the wrong theme. */
    data object Loading : AppUiState

    data class Ready(val theme: ThemeMode) : AppUiState
}

@KoinViewModel
class AppViewModel(
    settings: SettingsRepository,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    val state: StateFlow<AppUiState> = settings.settings
        .map { AppUiState.Ready(it.theme) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = AppUiState.Loading)
}
