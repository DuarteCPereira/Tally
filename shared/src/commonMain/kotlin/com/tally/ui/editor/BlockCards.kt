package com.tally.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.duarte.dui.components.DuiBadge
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiCardTone
import com.duarte.dui.components.DuiIconButton
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiMenu
import com.duarte.dui.components.DuiMenuItem
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.tally.domain.Exercise

/** Where a block sits in the list and what can be done to it. */
internal class BlockActions(
    val index: Int,
    val count: Int,
    val onMove: (offset: Int) -> Unit,
    val onEdit: () -> Unit,
    val onRemove: () -> Unit,
)

@Composable
internal fun BlockCard(block: DraftBlock, actions: BlockActions) {
    when (block) {
        is DraftBlock.Single -> ExerciseCard(block.exercise, actions)
        is DraftBlock.Superset -> SupersetCard(block, actions)
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise, actions: BlockActions) {
    DuiCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OrderButtons(actions)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = DuiTheme.spacing.space3),
            ) {
                DuiText(exercise.name, style = DuiTextStyle.Label)
                DuiText(
                    "${exercise.defaultSets} sets  ·  ${exercise.defaultReps} reps  ·  " +
                        "${exercise.defaultRestSeconds}s rest",
                    style = DuiTextStyle.Caption,
                    color = DuiTheme.colors.textSecondary,
                )
            }
            BlockMenu(actions)
        }
    }
}

@Composable
private fun SupersetCard(superset: DraftBlock.Superset, actions: BlockActions) {
    val spacing = DuiTheme.spacing
    DuiCard(tone = DuiCardTone.Accent) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OrderButtons(actions)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.space3),
                verticalArrangement = Arrangement.spacedBy(spacing.space1),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                ) {
                    DuiBadge("Superset")
                    DuiText(
                        "${superset.rounds} rounds",
                        style = DuiTextStyle.Caption,
                        color = DuiTheme.colors.textSecondary,
                    )
                }
                superset.members.forEachIndexed { index, member ->
                    DuiText("${index + 1}. ${member.name}  ·  ${superset.reps} reps", style = DuiTextStyle.Label)
                }
                DuiText(
                    "${superset.restSeconds}s rest between rounds",
                    style = DuiTextStyle.Caption,
                    color = DuiTheme.colors.textSecondary,
                )
            }
            BlockMenu(actions)
        }
    }
}

@Composable
private fun OrderButtons(actions: BlockActions) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DuiIconButton(
            DuiIcons.ArrowUp,
            "Move up",
            onClick = { actions.onMove(-1) },
            enabled = actions.index > 0,
        )
        DuiText("${actions.index + 1}", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
        DuiIconButton(
            DuiIcons.ArrowDown,
            "Move down",
            onClick = { actions.onMove(1) },
            enabled = actions.index < actions.count - 1,
        )
    }
}

@Composable
private fun BlockMenu(actions: BlockActions) {
    var expanded by remember { mutableStateOf(false) }
    DuiMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        items = listOf(DuiMenuItem("Edit", actions.onEdit), DuiMenuItem("Remove", actions.onRemove)),
    ) {
        DuiIconButton(DuiIcons.More, "More options", onClick = { expanded = true })
    }
}

@Preview
@Composable
private fun BlockCardsPreview() {
    val squat = Exercise(id = "squat", name = "Back squat", defaultSets = 4, defaultReps = 6)
    val lunge = Exercise(id = "lunge", name = "Walking lunge")
    val actions = BlockActions(index = 0, count = 2, onMove = {}, onEdit = {}, onRemove = {})
    DuiTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space3)) {
            BlockCard(DraftBlock.Single(key = "a", squat), actions)
            BlockCard(
                DraftBlock.Superset(key = "b", listOf(squat, lunge), rounds = 3, reps = 10, restSeconds = 90),
                actions,
            )
        }
    }
}
