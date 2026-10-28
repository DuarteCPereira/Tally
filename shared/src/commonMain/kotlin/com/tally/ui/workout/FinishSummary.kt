package com.tally.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.duarte.dui.components.DuiAmountText
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiEmptyState
import com.duarte.dui.components.DuiScaffold
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme

@Composable
internal fun FinishSummary(state: RunUiState.Finished, onSave: () -> Unit, onExit: () -> Unit) {
    val spacing = DuiTheme.spacing
    val isSaving = state.save == SaveStatus.SAVING || state.save == SaveStatus.SAVED
    DuiScaffold {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.space4, vertical = spacing.space5),
            verticalArrangement = Arrangement.Center,
        ) {
            DuiText("Session complete", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            DuiText("That’s a wrap.", style = DuiTextStyle.Title)
            DuiText(state.workoutName, style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
            DuiCard(Modifier.padding(top = spacing.space5)) {
                DuiAmountText(label = "Sets completed", amount = "${state.completed} / ${state.total}")
                DuiText(
                    if (state.completed == state.total) {
                        "Every set logged. Nice work."
                    } else {
                        "Skipped sets are saved as incomplete."
                    },
                    Modifier.padding(top = spacing.space2),
                    style = DuiTextStyle.Body,
                    color = DuiTheme.colors.textSecondary,
                )
            }
            if (state.save == SaveStatus.FAILED) {
                DuiText(
                    "Couldn’t save this session. Please try again.",
                    Modifier.padding(top = spacing.space4),
                    style = DuiTextStyle.Body,
                    color = DuiTheme.colors.negative,
                )
            }
            Column(
                Modifier.padding(top = spacing.space5),
                verticalArrangement = Arrangement.spacedBy(spacing.space2),
            ) {
                DuiButton(if (isSaving) "Saving…" else "Save session", onClick = onSave, enabled = !isSaving)
                DuiButton("Leave without saving", onClick = onExit, style = DuiButtonStyle.Ghost)
            }
        }
    }
}

@Composable
internal fun RunUnavailable(onExit: () -> Unit) {
    DuiScaffold {
        Column(
            Modifier
                .fillMaxSize()
                .padding(DuiTheme.spacing.space4),
            verticalArrangement = Arrangement.Center,
        ) {
            DuiEmptyState(
                title = "Workout unavailable",
                body = "This workout couldn’t be loaded. It may have been removed.",
                actionText = "Back to workouts",
                onAction = onExit,
            )
        }
    }
}

@Preview
@Composable
private fun FinishSummaryPreview() {
    DuiTheme(darkTheme = false) {
        FinishSummary(
            RunUiState.Finished(workoutName = "Upper body A", completed = 11, total = 12, save = SaveStatus.FAILED),
            onSave = {},
            onExit = {},
        )
    }
}
