package com.tally.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duarte.dui.components.DuiBadge
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiImageCard
import com.duarte.dui.components.DuiProgressBar
import com.duarte.dui.components.DuiScaffold
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.components.DuiTopBar
import com.duarte.dui.theme.DuiTheme
import com.tally.ui.common.DelayedLoadingIndicator
import com.tally.ui.common.rememberImageBitmap

@Composable
fun WorkoutRunScreen(viewModel: WorkoutRunViewModel, onExit: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        RunUiState.Loading -> DuiScaffold { DelayedLoadingIndicator() }
        RunUiState.Unavailable -> RunUnavailable(onExit)
        is RunUiState.Active -> ActiveRun(current, viewModel::onAction, onExit)
        is RunUiState.Finished -> {
            LaunchedEffect(current.save) {
                if (current.save == SaveStatus.SAVED) onExit()
            }
            FinishSummary(current, onSave = viewModel::save, onExit = onExit)
        }
    }
}

@Composable
private fun ActiveRun(state: RunUiState.Active, onAction: (RunAction) -> Unit, onExit: () -> Unit) {
    val progress = state.progress
    DuiScaffold(
        topBar = {
            DuiTopBar(
                onBack = onExit,
                backLabel = "Exit",
                trailing = "${progress.completedCount} / ${progress.total} done",
            )
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            DuiProgressBar(progress.progress, Modifier.padding(horizontal = DuiTheme.spacing.space4))
            StepDetails(state, onAction, Modifier.weight(1f))
            StepControls(canGoBack = progress.canGoBack, onAction)
        }
    }
    state.rest?.let { RestOverlay(it, onAction) }
}

@Composable
private fun StepDetails(state: RunUiState.Active, onAction: (RunAction) -> Unit, modifier: Modifier) {
    val spacing = DuiTheme.spacing
    val image = rememberImageBitmap(state.exercise?.imagePath)
    LazyColumn(
        modifier.fillMaxWidth(),
        contentPadding = PaddingValues(spacing.space4),
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        item { StepHeader(state) }
        if (image != null) {
            item {
                DuiImageCard(image, "${state.exercise?.name ?: state.workoutName} exercise photo")
            }
        }
        item { ExerciseHeader(state) }
        state.circuit?.let { circuit ->
            item { SupersetPanel(circuit) }
        }
        item {
            RepAdjuster(
                target = state.progress.targetReps ?: 0,
                actual = state.actualReps,
                onAdjust = { onAction(RunAction.AdjustReps(it)) },
            )
        }
    }
}

@Composable
private fun StepHeader(state: RunUiState.Active) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            DuiText(state.stepTitle, style = DuiTextStyle.Title)
            DuiText(
                state.workoutName,
                style = DuiTextStyle.Caption,
                color = DuiTheme.colors.textSecondary,
                maxLines = 1,
            )
        }
        DuiBadge(if (state.progress.isSuperset) "Superset" else "Regular")
    }
}

@Composable
private fun ExerciseHeader(state: RunUiState.Active) {
    Column {
        DuiText(state.exercise?.name ?: "Exercise", style = DuiTextStyle.Title)
        state.progress.currentItem?.notes?.takeIf { it.isNotBlank() }?.let {
            DuiText(it, style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
        }
    }
}

@Composable
private fun StepControls(canGoBack: Boolean, onAction: (RunAction) -> Unit) {
    val spacing = DuiTheme.spacing
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = spacing.space4, end = spacing.space4, bottom = spacing.space4, top = spacing.space2),
        verticalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        DuiButton("Complete set", onClick = { onAction(RunAction.CompleteSet) })
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            DuiButton(
                "Previous",
                onClick = { onAction(RunAction.Previous) },
                modifier = Modifier.weight(1f),
                style = DuiButtonStyle.Secondary,
                enabled = canGoBack,
            )
            DuiButton(
                "Skip",
                onClick = { onAction(RunAction.Skip) },
                modifier = Modifier.weight(1f),
                style = DuiButtonStyle.Secondary,
            )
        }
    }
}
