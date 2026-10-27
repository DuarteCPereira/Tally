package com.duarte.workouttracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duarte.workouttracker.domain.ThemeMode
import com.duarte.workouttracker.ui.home.HomeScreen
import com.duarte.workouttracker.ui.editor.WorkoutEditorScreen
import kotlinx.coroutines.launch

private enum class Destination(val label: String, val icon: String) { HOME("Home", "⌂"), HISTORY("History", "◷"), SETTINGS("Settings", "⚙") }

@Composable
fun App(container: AppContainer = remember { AppContainer() }) {
    val settings by container.settings.settings.collectAsState(initial = com.duarte.workouttracker.domain.WorkoutSettings())
    val scope = rememberCoroutineScope()
    val dark = when (settings.theme) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    WorkoutTrackerTheme(darkTheme = dark) {
        val backStack = remember { mutableStateListOf(Destination.HOME) }
        var editingWorkoutId by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
        var creatingWorkout by remember { androidx.compose.runtime.mutableStateOf(false) }
        var runningWorkoutId by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
        val current = backStack.last()
        if (runningWorkoutId != null) {
            com.duarte.workouttracker.ui.workout.WorkoutRunScreen(runningWorkoutId!!, container, onExit = { runningWorkoutId = null })
            return@WorkoutTrackerTheme
        }
        Scaffold(
            bottomBar = {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = current == destination && !creatingWorkout && editingWorkoutId == null,
                            onClick = { creatingWorkout = false; editingWorkoutId = null; if (current != destination) { backStack.clear(); backStack.add(destination) } },
                            icon = { Text(destination.icon) }, label = { Text(destination.label) },
                        )
                    }
                }
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
}
