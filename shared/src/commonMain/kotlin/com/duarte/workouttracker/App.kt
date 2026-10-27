package com.duarte.workouttracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.duarte.dui.components.DuiBottomBar
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiScaffold
import com.duarte.dui.components.DuiTab
import com.duarte.dui.theme.DuiTheme
import com.duarte.workouttracker.domain.ThemeMode
import com.duarte.workouttracker.ui.home.HomeScreen
import com.duarte.workouttracker.ui.editor.WorkoutEditorScreen

private enum class Destination(val label: String, val icon: ImageVector) {
    HOME("Home", DuiIcons.Home),
    HISTORY("History", DuiIcons.History),
    SETTINGS("Settings", DuiIcons.Settings),
}

@Composable
fun App(container: AppContainer = remember { AppContainer() }) {
    val settings by container.settings.settings.collectAsState(initial = com.duarte.workouttracker.domain.WorkoutSettings())
    val dark = when (settings.theme) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    DuiTheme(darkTheme = dark) {
        AppContent(container)
    }
}

@Composable
private fun AppContent(container: AppContainer) {
    val backStack = remember { mutableStateListOf(Destination.HOME) }
    var editingWorkoutId by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var creatingWorkout by remember { androidx.compose.runtime.mutableStateOf(false) }
    var runningWorkoutId by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val current = backStack.last()
    if (runningWorkoutId != null) {
        com.duarte.workouttracker.ui.workout.WorkoutRunScreen(runningWorkoutId!!, container, onExit = { runningWorkoutId = null })
        return
    }
    DuiScaffold(
        bottomBar = {
            val selected = if (!creatingWorkout && editingWorkoutId == null) Destination.entries.indexOf(current) else -1
            DuiBottomBar(
                items = Destination.entries.map { DuiTab(it.label, it.icon) },
                selectedIndex = selected,
                onSelect = { index ->
                    val destination = Destination.entries[index]
                    creatingWorkout = false
                    editingWorkoutId = null
                    if (current != destination) { backStack.clear(); backStack.add(destination) }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when {
                current == Destination.HOME && (creatingWorkout || editingWorkoutId != null) -> WorkoutEditorScreen(
                    container = container,
                    workoutId = editingWorkoutId,
                    onBack = { creatingWorkout = false; editingWorkoutId = null },
                )
                current == Destination.HOME -> HomeScreen(
                    container = container,
                    onCreate = { creatingWorkout = true },
                    onEdit = { editingWorkoutId = it },
                    onStart = { runningWorkoutId = it },
                )
                current == Destination.HISTORY -> com.duarte.workouttracker.ui.history.HistoryScreen(container)
                current == Destination.SETTINGS -> com.duarte.workouttracker.ui.settings.SettingsScreen(container)
            }
        }
    }
}
