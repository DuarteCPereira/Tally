package com.duarte.workouttracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.duarte.dui.components.DuiAvatar
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiEmptyState
import com.duarte.dui.components.DuiIconButton
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiListRow
import com.duarte.dui.components.DuiMenu
import com.duarte.dui.components.DuiMenuItem
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.duarte.workouttracker.AppContainer
import com.duarte.workouttracker.domain.Workout
import kotlinx.coroutines.launch
import com.duarte.workouttracker.ui.editor.duplicateWorkoutWithItems

@Composable
fun HomeScreen(container: AppContainer, onCreate: () -> Unit, onEdit: (String) -> Unit, onStart: (String) -> Unit) {
    val workouts by container.workouts.workouts.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<Workout?>(null) }
    val spacing = DuiTheme.spacing
    Column(Modifier.fillMaxSize().padding(horizontal = spacing.space4)) {
        Column(Modifier.padding(top = spacing.space4, bottom = spacing.space3)) {
            DuiText("Workouts", style = DuiTextStyle.Title)
            DuiText("Build a routine, then show up for it.", style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
        }
        if (workouts.isEmpty()) {
            DuiEmptyState(
                title = "Start with a simple plan",
                body = "Create your first workout and shape it around the way you like to train.",
                icon = DuiIcons.Spark,
                actionText = "Create your first workout",
                onAction = onCreate,
            )
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = spacing.space3)) {
                itemsIndexed(workouts.sortedBy { it.position }, key = { _, workout -> workout.id }) { index, workout ->
                    if (index > 0) DuiDivider()
                    WorkoutRow(workout, onEdit = { onEdit(workout.id) }, onStart = { onStart(workout.id) }, onDuplicate = { scope.launch { duplicateWorkoutWithItems(container, workout.id) } }, onDelete = { deleteTarget = workout })
                }
            }
            DuiButton("New workout", onCreate, Modifier.padding(bottom = spacing.space4), style = DuiButtonStyle.Primary)
        }
    }
    deleteTarget?.let { workout ->
        DuiDialog(
            onDismissRequest = { deleteTarget = null },
            title = "Delete workout?",
            confirmButton = { DuiButton("Delete", onClick = { scope.launch { container.workouts.delete(workout.id); deleteTarget = null } }) },
            dismissButton = { DuiButton("Keep workout", onClick = { deleteTarget = null }, style = DuiButtonStyle.Ghost) },
        ) {
            DuiText("“${workout.name}” will be removed from your list. This can’t be undone.", color = DuiTheme.colors.textSecondary)
        }
    }
}

@Composable
private fun WorkoutRow(workout: Workout, onEdit: () -> Unit, onStart: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        DuiListRow(
            title = workout.name,
            modifier = Modifier.weight(1f),
            subtitle = "Ready when you are",
            onClick = onStart,
            leading = { DuiAvatar(workout.name.take(1).uppercase()) },
        )
        DuiMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = listOf(
                DuiMenuItem("Edit", onEdit),
                DuiMenuItem("Make a copy", onDuplicate),
                DuiMenuItem("Delete", onDelete),
            ),
        ) {
            DuiIconButton(DuiIcons.More, "More options for ${workout.name}", onClick = { expanded = true })
        }
    }
}
