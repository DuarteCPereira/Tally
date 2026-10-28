package com.tally.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.tally.domain.Workout
import com.tally.ui.common.DelayedLoadingIndicator
import kotlin.time.Instant

/** Navigation out of the home screen, each taking the id of the workout it acts on. */
data class HomeNavigation(
    val onCreate: () -> Unit,
    val onEdit: (workoutId: String) -> Unit,
    val onStart: (workoutId: String) -> Unit,
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigation: HomeNavigation,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Workout?>(null) }

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = DuiTheme.spacing.space4),
    ) {
        Header()
        when (val current = state) {
            HomeUiState.Loading -> DelayedLoadingIndicator()
            is HomeUiState.Content -> WorkoutList(
                workouts = current.workouts,
                navigation = navigation,
                onDuplicate = viewModel::duplicate,
                onDelete = { deleteTarget = it },
            )
        }
    }

    deleteTarget?.let { workout ->
        DeleteWorkoutDialog(
            workout = workout,
            onConfirm = {
                viewModel.delete(workout)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun Header() {
    val spacing = DuiTheme.spacing
    Column(Modifier.padding(top = spacing.space4, bottom = spacing.space3)) {
        DuiText("Workouts", style = DuiTextStyle.Title)
        DuiText(
            "Build a routine, then show up for it.",
            style = DuiTextStyle.Body,
            color = DuiTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun ColumnScope.WorkoutList(
    workouts: List<Workout>,
    navigation: HomeNavigation,
    onDuplicate: (Workout) -> Unit,
    onDelete: (Workout) -> Unit,
) {
    if (workouts.isEmpty()) {
        DuiEmptyState(
            title = "Start with a simple plan",
            body = "Create your first workout and shape it around the way you like to train.",
            icon = DuiIcons.Spark,
            actionText = "Create your first workout",
            onAction = navigation.onCreate,
        )
        return
    }

    val spacing = DuiTheme.spacing
    LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = spacing.space3)) {
        itemsIndexed(workouts, key = { _, workout -> workout.id }) { index, workout ->
            if (index > 0) DuiDivider()
            WorkoutRow(
                workout = workout,
                onStart = { navigation.onStart(workout.id) },
                onEdit = { navigation.onEdit(workout.id) },
                onDuplicate = { onDuplicate(workout) },
                onDelete = { onDelete(workout) },
            )
        }
    }
    DuiButton(
        "New workout",
        navigation.onCreate,
        Modifier.padding(bottom = spacing.space4),
        style = DuiButtonStyle.Primary,
    )
}

@Composable
private fun WorkoutRow(
    workout: Workout,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
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
            DuiIconButton(
                DuiIcons.More,
                "More options for ${workout.name}",
                onClick = { expanded = true },
            )
        }
    }
}

@Composable
private fun DeleteWorkoutDialog(workout: Workout, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    DuiDialog(
        onDismissRequest = onDismiss,
        title = "Delete workout?",
        confirmButton = { DuiButton("Delete", onClick = onConfirm) },
        dismissButton = {
            DuiButton("Keep workout", onClick = onDismiss, style = DuiButtonStyle.Ghost)
        },
    ) {
        DuiText(
            "“${workout.name}” will be removed from your list. This can’t be undone.",
            color = DuiTheme.colors.textSecondary,
        )
    }
}

@Preview
@Composable
private fun WorkoutListPreview() {
    val workouts = listOf("Upper body A", "Lower body", "Full body circuit").mapIndexed { index, name ->
        Workout(id = "$index", name = name, position = index, createdAt = Instant.fromEpochSeconds(0))
    }
    DuiTheme(darkTheme = false) {
        Column {
            WorkoutList(
                workouts = workouts,
                navigation = HomeNavigation(onCreate = {}, onEdit = {}, onStart = {}),
                onDuplicate = {},
                onDelete = {},
            )
        }
    }
}
