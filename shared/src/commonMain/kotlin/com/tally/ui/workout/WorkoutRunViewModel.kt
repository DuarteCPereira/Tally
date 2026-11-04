package com.tally.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tally.data.ExerciseRepository
import com.tally.data.HistoryRepository
import com.tally.data.SettingsRepository
import com.tally.data.WorkoutRepository
import com.tally.domain.RestTimerFactory
import com.tally.domain.WorkoutEngine
import com.tally.domain.WorkoutSettings
import com.tally.ktx.runCatchingNonCancellation
import com.tally.ui.common.defaultViewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

/** The repositories a run reads from when it starts, and writes the finished session to. */
@Factory
class RunRepositories(
    val workouts: WorkoutRepository,
    val exercises: ExerciseRepository,
    val settings: SettingsRepository,
    val history: HistoryRepository,
)

/**
 * Loads the workout identified by [workoutId] into a [WorkoutRun], forwards the user's
 * [RunAction]s to it, and [save]s the finished session. [timerFactory] builds the rest timer once the
 * user's sound and vibration settings are known.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class WorkoutRunViewModel(
    @InjectedParam private val workoutId: String,
    private val repositories: RunRepositories,
    private val timerFactory: RestTimerFactory,
    scope: CoroutineScope = defaultViewModelScope(),
) : ViewModel(scope) {
    private sealed interface Load {
        data object Pending : Load
        data object Failed : Load
        data class Ready(val run: WorkoutRun) : Load
    }

    private val load = MutableStateFlow<Load>(Load.Pending)
    private val saveStatus = MutableStateFlow(SaveStatus.IDLE)
    private val run: WorkoutRun? get() = (load.value as? Load.Ready)?.run

    val state: StateFlow<RunUiState> = load
        .flatMapLatest { load ->
            when (load) {
                Load.Pending -> flowOf(RunUiState.Loading)
                Load.Failed -> flowOf(RunUiState.Unavailable)
                is Load.Ready -> load.run.uiState(saveStatus)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = RunUiState.Loading)

    init {
        viewModelScope.launch { load.value = start() }
    }

    fun onAction(action: RunAction) {
        run?.handle(action)
    }

    override fun onCleared() {
        run?.dispose()
    }

    /** Saves the finished session once; [RunUiState.Finished.save] reports progress and failure. */
    fun save() {
        val finished = run?.finished ?: return
        if (saveStatus.value == SaveStatus.SAVING || saveStatus.value == SaveStatus.SAVED) return
        saveStatus.value = SaveStatus.SAVING
        viewModelScope.launch {
            saveStatus.value = runCatchingNonCancellation {
                repositories.history.addSession(finished.session, finished.entries)
            }.fold(onSuccess = { SaveStatus.SAVED }, onFailure = { SaveStatus.FAILED })
        }
    }

    private suspend fun start(): Load = runCatchingNonCancellation {
        val workout = repositories.workouts.workout(workoutId) ?: return Load.Failed
        val content = repositories.workouts.content(workoutId)
        val exercises = repositories.exercises.exercises.first().associateBy { it.id }
        val settings = repositories.settings.settings.first()
        val engine = WorkoutEngine(
            workout = workout,
            items = content.items,
            membersByItemId = content.members,
            exerciseNames = exercises.mapValues { (_, exercise) -> exercise.name },
            settings = settings,
        )
        Load.Ready(WorkoutRun(RunPlan(workout, content, exercises), engine, timerFactory.create(settings)))
    }.getOrElse { Load.Failed }
}
