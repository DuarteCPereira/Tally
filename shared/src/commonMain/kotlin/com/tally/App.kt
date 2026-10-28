package com.tally

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duarte.dui.components.DuiBottomBar
import com.duarte.dui.components.DuiScaffold
import com.duarte.dui.components.DuiTab
import com.duarte.dui.theme.DuiTheme
import com.tally.domain.RestTimer
import com.tally.domain.ThemeMode
import com.tally.domain.isDark
import com.tally.ui.common.ViewModelStoreScope
import com.tally.ui.editor.ExerciseLibraryViewModel
import com.tally.ui.editor.ExercisePhotos
import com.tally.ui.editor.WorkoutEditorScreen
import com.tally.ui.editor.WorkoutEditorViewModel
import com.tally.ui.history.HistoryScreen
import com.tally.ui.history.HistoryViewModel
import com.tally.ui.home.HomeNavigation
import com.tally.ui.home.HomeScreen
import com.tally.ui.home.HomeViewModel
import com.tally.ui.settings.SettingsScreen
import com.tally.ui.settings.SettingsViewModel
import com.tally.ui.workout.RunRepositories
import com.tally.ui.workout.WorkoutRunScreen
import com.tally.ui.workout.WorkoutRunViewModel

/** The ViewModels behind the bottom-bar tabs, which live as long as the app. */
private class TabViewModels(
    val home: HomeViewModel,
    val history: HistoryViewModel,
    val settings: SettingsViewModel,
)

/**
 * The app's root. Nothing is drawn until the saved theme is known, then [onReady] is called once
 * so the platform can dismiss its launch screen without ever showing the wrong theme.
 * [onThemeChange] reports the saved theme whenever it changes, so the platform can match its
 * next launch screen to it.
 */
@Composable
fun App(
    container: AppContainer = remember { AppContainer() },
    onReady: () -> Unit = {},
    onThemeChange: (ThemeMode) -> Unit = {},
) {
    val app = viewModel { AppViewModel(container.settings) }
    // Created up front so every tab's data loads during start-up, not on its first visit
    val tabs = TabViewModels(
        home = viewModel { HomeViewModel(container.workouts) },
        history = viewModel { HistoryViewModel(container.history) },
        settings = viewModel { SettingsViewModel(container.settings) },
    )
    val state by app.state.collectAsStateWithLifecycle()
    val theme = (state as? AppUiState.Ready)?.theme ?: return

    LaunchedEffect(Unit) { onReady() }
    LaunchedEffect(theme) { onThemeChange(theme) }
    DuiTheme(darkTheme = theme.isDark(systemIsDark = isSystemInDarkTheme())) {
        AppContent(container, tabs)
    }
}

@Composable
private fun AppContent(container: AppContainer, tabs: TabViewModels) {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    when (val current = route) {
        is Route.Run -> ViewModelStoreScope(key = current) {
            RunScreen(container, current.workoutId, onExit = { route = Route.Home })
        }

        is Route.Scaffolded -> ScaffoldedContent(current, container, tabs, navigate = { route = it })
    }
}

@Composable
private fun ScaffoldedContent(
    route: Route.Scaffolded,
    container: AppContainer,
    tabs: TabViewModels,
    navigate: (Route) -> Unit,
) {
    DuiScaffold(
        bottomBar = {
            DuiBottomBar(
                items = AppTab.entries.map { DuiTab(it.label, it.icon) },
                selectedIndex = (route as? Route.Tab)?.tab?.ordinal ?: -1,
                onSelect = { index -> navigate(Route.Tab(AppTab.entries[index])) },
            )
        },
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when (route) {
            is Route.Tab -> TabScreen(route.tab, tabs, navigate, modifier)
            is Route.Editor -> ViewModelStoreScope(key = route) {
                EditorScreen(container, route.workoutId, onBack = { navigate(Route.Home) }, modifier)
            }
        }
    }
}

@Composable
private fun TabScreen(tab: AppTab, tabs: TabViewModels, navigate: (Route) -> Unit, modifier: Modifier) {
    when (tab) {
        AppTab.HOME -> HomeScreen(
            viewModel = tabs.home,
            navigation = HomeNavigation(
                onCreate = { navigate(Route.Editor(workoutId = null)) },
                onEdit = { navigate(Route.Editor(it)) },
                onStart = { navigate(Route.Run(it)) },
            ),
            modifier = modifier,
        )

        AppTab.HISTORY -> HistoryScreen(tabs.history, modifier)
        AppTab.SETTINGS -> SettingsScreen(tabs.settings, modifier)
    }
}

@Composable
private fun EditorScreen(container: AppContainer, workoutId: String?, onBack: () -> Unit, modifier: Modifier) {
    WorkoutEditorScreen(
        editor = viewModel { WorkoutEditorViewModel(workoutId, container.workouts, container.exercises) },
        library = viewModel {
            ExerciseLibraryViewModel(
                exercises = container.exercises,
                photos = ExercisePhotos(container.platform.imagePicker, container.platform.imageStorage),
            )
        },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun RunScreen(container: AppContainer, workoutId: String, onExit: () -> Unit) {
    val viewModel = viewModel {
        WorkoutRunViewModel(
            workoutId = workoutId,
            repositories = RunRepositories(
                workouts = container.workouts,
                exercises = container.exercises,
                settings = container.settings,
                history = container.history,
            ),
            createTimer = { settings ->
                RestTimer(
                    timerNotifier = container.platform.timerNotifier,
                    feedback = container.platform.feedback,
                    soundEnabled = settings.sound,
                    vibrationEnabled = settings.vibration,
                )
            },
        )
    }
    WorkoutRunScreen(viewModel, onExit)
}
