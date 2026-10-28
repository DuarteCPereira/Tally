package com.tally.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiCardTone
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiIconButton
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.tally.domain.Exercise
import com.tally.domain.newId

/** Builds a new superset, or edits [initial], choosing members from the [library]. */
@Composable
internal fun SupersetFormDialog(
    initial: DraftBlock.Superset?,
    library: ExerciseLibraryViewModel,
    onDismiss: () -> Unit,
    onSave: (DraftBlock.Superset) -> Unit,
) {
    var form by remember(initial) { mutableStateOf(initial?.let(SupersetForm::of) ?: SupersetForm()) }
    var error by remember { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf(false) }
    val spacing = DuiTheme.spacing

    DuiDialog(
        onDismissRequest = onDismiss,
        title = "Build a superset",
        confirmButton = {
            DuiButton("Save superset", onClick = {
                when (val result = form.validate(key = initial?.key ?: newId())) {
                    is FormResult.Valid -> onSave(result.value)
                    is FormResult.Invalid -> error = result.message
                }
            })
        },
        dismissButton = { DuiButton("Cancel", onClick = onDismiss, style = DuiButtonStyle.Ghost) },
    ) {
        DuiText(
            "Move from one exercise to the next, then repeat for each round.",
            style = DuiTextStyle.Body,
            color = DuiTheme.colors.textSecondary,
        )
        form.members.forEachIndexed { index, exercise ->
            MemberCard(
                exercise = exercise,
                index = index,
                count = form.members.size,
                onMembersChange = { form = form.copy(members = it(form.members)) },
            )
        }
        DuiButton("Add member", onClick = { picking = true }, style = DuiButtonStyle.Secondary)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            NumberField("Rounds", form.rounds, { form = form.copy(rounds = it) }, Modifier.weight(1f))
            NumberField("Reps", form.reps, { form = form.copy(reps = it) }, Modifier.weight(1f))
        }
        NumberField(
            "Rest between rounds (seconds)",
            form.rest,
            { form = form.copy(rest = it) },
            Modifier.fillMaxWidth(),
        )
        error?.let { DuiText(it, style = DuiTextStyle.Caption, color = DuiTheme.colors.negative) }
    }

    if (picking) {
        val addMember = { exercise: Exercise ->
            form = form.copy(members = form.members + exercise)
            picking = false
        }
        LibraryPickerDialog(
            title = "Choose an exercise",
            emptyText = "Your exercise library is empty. Add a new exercise to start.",
            library = library,
            onPick = addMember,
            onCreated = addMember,
            onDismiss = { picking = false },
        )
    }
}

/** One member of the superset being built, with controls to reorder or drop it. */
@Composable
private fun MemberCard(
    exercise: Exercise,
    index: Int,
    count: Int,
    onMembersChange: ((List<Exercise>) -> List<Exercise>) -> Unit,
) {
    DuiCard(tone = DuiCardTone.Accent) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DuiText("${index + 1}. ${exercise.name}", Modifier.weight(1f), style = DuiTextStyle.Label)
            DuiIconButton(
                DuiIcons.ArrowUp,
                "Move up",
                onClick = { onMembersChange { it.swap(index, index - 1) } },
                enabled = index > 0,
            )
            DuiIconButton(
                DuiIcons.ArrowDown,
                "Move down",
                onClick = { onMembersChange { it.swap(index, index + 1) } },
                enabled = index < count - 1,
            )
            DuiIconButton(
                DuiIcons.Close,
                "Remove ${exercise.name}",
                onClick = { onMembersChange { members -> members.filterIndexed { i, _ -> i != index } } },
            )
        }
    }
}

/** Returns a copy with the elements at [from] and [to] swapped, or this list if [to] is out of range. */
private fun <T> List<T>.swap(from: Int, to: Int): List<T> {
    if (to !in indices) return this
    return toMutableList().apply {
        this[from] = this@swap[to]
        this[to] = this@swap[from]
    }
}
