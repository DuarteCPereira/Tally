package com.tally.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.duarte.dui.components.DuiAmountText
import com.duarte.dui.components.DuiAvatar
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiListRow
import com.duarte.dui.components.DuiProgressBar
import com.duarte.dui.components.DuiStepper
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme

private const val REST_STEP_SECONDS = 15
private const val SECONDS_PER_MINUTE = 60

@Composable
internal fun SupersetPanel(circuit: CircuitUi) {
    val spacing = DuiTheme.spacing
    DuiCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DuiText("The circuit", Modifier.weight(1f), style = DuiTextStyle.Label)
            DuiText(
                "Round ${circuit.round} of ${circuit.rounds}",
                style = DuiTextStyle.Caption,
                color = DuiTheme.colors.textSecondary,
            )
        }
        circuit.members.forEachIndexed { index, member ->
            val active = index == circuit.currentIndex
            if (index > 0) DuiDivider()
            DuiListRow(
                title = member.name,
                subtitle = if (active) "Now" else null,
                value = "${member.reps} reps",
                selected = active,
                leading = { DuiAvatar("${index + 1}") },
            )
        }
        DuiProgressBar(
            circuit.round.toFloat() / circuit.rounds.coerceAtLeast(1),
            Modifier.padding(top = spacing.space3),
        )
    }
}

@Composable
internal fun RepAdjuster(target: Int, actual: Int, onAdjust: (delta: Int) -> Unit) {
    DuiCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                DuiText("Reps", style = DuiTextStyle.Label)
                DuiText("Target $target", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            }
            DuiStepper(
                value = "$actual",
                onDecrement = { onAdjust(-1) },
                onIncrement = { onAdjust(1) },
                decrementEnabled = actual > 0,
                decrementLabel = "Decrease reps",
                incrementLabel = "Increase reps",
            )
        }
    }
}

/** The non-dismissible rest dialog: waiting to start (manual mode) or counting down. */
@Composable
internal fun RestOverlay(rest: RestUi, onAction: (RunAction) -> Unit) {
    val skip = { onAction(RunAction.SkipRest) }
    // Manual mode offers skipping next to "Start rest"; a running rest offers it as the main action
    val skipInstead: (@Composable () -> Unit)? = if (rest is RestUi.Ready) {
        { DuiButton("Skip rest", onClick = skip, style = DuiButtonStyle.Ghost) }
    } else {
        null
    }
    DuiDialog(
        onDismissRequest = {},
        title = "Take a breath",
        dismissible = false,
        confirmButton = {
            when (rest) {
                is RestUi.Ready -> DuiButton("Start rest", onClick = { onAction(RunAction.StartRest) })
                is RestUi.Running -> DuiButton("Skip rest", onClick = skip)
            }
        },
        dismissButton = skipInstead,
    ) {
        when (rest) {
            is RestUi.Ready -> DuiAmountText(label = "Ready", amount = formatClock(rest.seconds))
            is RestUi.Running -> RunningRest(rest, onAction)
        }
    }
}

/** The countdown with its controls, laid out directly in the dialog's column. */
@Composable
private fun ColumnScope.RunningRest(rest: RestUi.Running, onAction: (RunAction) -> Unit) {
    val spacing = DuiTheme.spacing
    DuiAmountText(
        label = if (rest.isPaused) "Paused" else "Resting",
        amount = formatClock(rest.remainingSeconds),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
        DuiButton(
            "−$REST_STEP_SECONDS sec",
            onClick = { onAction(RunAction.AdjustRest(-REST_STEP_SECONDS)) },
            modifier = Modifier.weight(1f),
            style = DuiButtonStyle.Secondary,
        )
        DuiButton(
            "+$REST_STEP_SECONDS sec",
            onClick = { onAction(RunAction.AdjustRest(REST_STEP_SECONDS)) },
            modifier = Modifier.weight(1f),
            style = DuiButtonStyle.Secondary,
        )
    }
    DuiButton(
        if (rest.isPaused) "Resume timer" else "Pause timer",
        onClick = { onAction(RunAction.ToggleRestPause) },
        style = DuiButtonStyle.Secondary,
    )
}

/** "01:30" for 90 seconds. */
private fun formatClock(seconds: Int): String {
    val minutes = (seconds / SECONDS_PER_MINUTE).toString().padStart(2, '0')
    val rest = (seconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')
    return "$minutes:$rest"
}

@Preview
@Composable
private fun SupersetPanelPreview() {
    DuiTheme(darkTheme = false) {
        SupersetPanel(
            CircuitUi(
                members = listOf(CircuitMemberUi("Push-up", reps = 12), CircuitMemberUi("Row", reps = 10)),
                currentIndex = 1,
                round = 2,
                rounds = 3,
            ),
        )
    }
}

@Preview
@Composable
private fun RepAdjusterPreview() {
    DuiTheme(darkTheme = true) {
        RepAdjuster(target = 10, actual = 8, onAdjust = {})
    }
}
