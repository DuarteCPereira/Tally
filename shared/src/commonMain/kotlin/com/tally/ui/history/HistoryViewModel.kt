package com.tally.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.data.HistoryRepository
import com.tally.domain.HistoryStats
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HistoryViewModel(
    repository: HistoryRepository,
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    // Eagerly: the tab is created at app start so its state is ready before it is first shown
    val state: StateFlow<HistoryUiState> = combine(repository.sessions, repository.entries) { sessions, entries ->
        val entriesBySession = entries.groupBy { it.sessionId }
        HistoryUiState.Content(
            stats = HistoryStats.of(sessions, entries, now = clock.now()).toStatsUi(),
            sessions = sessions
                .sortedByDescending { it.startedAt }
                .map { it.toSessionUi(entriesBySession[it.id].orEmpty(), timeZone) },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = HistoryUiState.Loading)
}
