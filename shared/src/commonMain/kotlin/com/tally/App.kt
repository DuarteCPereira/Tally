package com.tally

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duarte.dui.theme.DuiTheme
import com.tally.domain.ThemeMode
import com.tally.domain.isDark
import com.tally.ui.editor.ExerciseLibraryViewModel
import com.tally.ui.editor.WorkoutEditorViewModel
import com.tally.ui.history.HistoryViewModel
import com.tally.ui.home.HomeViewModel
import com.tally.ui.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.tally.ui.workout.WorkoutRunViewModel

/** The ViewModels behind the bottom-bar tabs, which live as long as the app. */
internal class TabViewModels(
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
    onReady: () -> Unit = {},
    onThemeChange: (ThemeMode) -> Unit = {},
) {
    val app = koinViewModel<AppViewModel>()
    // Created up front so every tab's data loads during start-up, not on its first visit
    val tabs = TabViewModels(
        home = koinViewModel(),
        history = koinViewModel(),
        settings = koinViewModel(),
    )
    val state by app.state.collectAsStateWithLifecycle()
    val theme = (state as? AppUiState.Ready)?.theme ?: return

    LaunchedEffect(Unit) { onReady() }
    LaunchedEffect(theme) { onThemeChange(theme) }
    DuiTheme(darkTheme = theme.isDark(systemIsDark = isSystemInDarkTheme())) {
        AppNavigation(tabs)
    }
}
