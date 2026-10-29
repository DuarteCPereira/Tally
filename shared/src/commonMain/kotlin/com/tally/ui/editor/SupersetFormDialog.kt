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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.tally.ui.common.ViewModelStoreScope

/**
 * Builds a new superset, or edits [initial], choosing members from the [library]. The draft and its
 * validation live in a [SupersetFormViewModel] scoped to the dialog.
 */
@Composable
internal fun SupersetFormDialog(
    initial: DraftBlock.Superset?,
    library: ExerciseLibraryViewModel,
    onDismiss: () -> Unit,
    onSave: (DraftBlock.Superset) -> Unit,
) {
    ViewModelStoreScope(key = initial?.key) {
        val form = viewModel { SupersetFormViewModel(initial) }
        SupersetFormContent(form, library, onDismiss, onSave)
    }
}

@Composable
private fun SupersetFormContent(
    viewModel: SupersetFormViewModel,
    library: ExerciseLibraryViewModel,
    onDismiss: () -> Unit,
    onSave: (DraftBlock.Superset) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val form = state.form
    var picking by remember { mutableStateOf(false) }
    val spacing = DuiTheme.spacing

    DuiDialog(
        onDismissRequest = onDismiss,
        title = "Build a superset",
        confirmButton = { DuiButton("Save superset", onClick = { viewModel.save()?.let(onSave) }) },
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
                onMove = viewModel::moveMember,
                onRemove = viewModel::removeMember,
            )
        }
        DuiButton("Add member", onClick = { picking = true }, style = DuiButtonStyle.Secondary)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            NumberField("Rounds", form.rounds, { viewModel.edit { copy(rounds = it) } }, Modifier.weight(1f))
            NumberField("Reps", form.reps, { viewModel.edit { copy(reps = it) } }, Modifier.weight(1f))
        }
        NumberField(
            "Rest between rounds (seconds)",
            form.rest,
            { viewModel.edit { copy(rest = it) } },
            Modifier.fillMaxWidth(),
        )
        state.message?.let { DuiText(it, style = DuiTextStyle.Caption, color = DuiTheme.colors.negative) }
    }

    if (picking) {
        val addMember = { exercise: Exercise ->
            viewModel.addMember(exercise)
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
    onMove: (from: Int, to: Int) -> Unit,
    onRemove: (Int) -> Unit,
) {
    DuiCard(tone = DuiCardTone.Accent) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DuiText("${index + 1}. ${exercise.name}", Modifier.weight(1f), style = DuiTextStyle.Label)
            DuiIconButton(
                DuiIcons.ArrowUp,
                "Move up",
                onClick = { onMove(index, index - 1) },
                enabled = index > 0,
            )
            DuiIconButton(
                DuiIcons.ArrowDown,
                "Move down",
                onClick = { onMove(index, index + 1) },
                enabled = index < count - 1,
            )
            DuiIconButton(
                DuiIcons.Close,
                "Remove ${exercise.name}",
                onClick = { onRemove(index) },
            )
        }
    }
}
