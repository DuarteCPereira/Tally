package com.tally.ui.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiIconButton
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiListRow
import com.duarte.dui.components.DuiText
import com.duarte.dui.theme.DuiTheme
import com.tally.domain.Exercise

/**
 * Lets the user pick an exercise from the [library], add new ones to it, or remove ones from it.
 * A newly created exercise is saved to the library and then passed to [onCreated].
 */
@Composable
internal fun LibraryPickerDialog(
    title: String,
    emptyText: String,
    library: ExerciseLibraryViewModel,
    onPick: (Exercise) -> Unit,
    onCreated: (Exercise) -> Unit,
    onDismiss: () -> Unit,
) {
    val exercises by library.library.collectAsStateWithLifecycle()
    var creating by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<Exercise?>(null) }

    DuiDialog(
        onDismissRequest = onDismiss,
        title = title,
        confirmButton = { DuiButton("Done", onClick = onDismiss) },
    ) {
        DuiButton("New exercise", onClick = { creating = true }, style = DuiButtonStyle.Secondary)
        LibraryList(exercises, emptyText, onPick, onRemove = { removeTarget = it })
    }

    if (creating) {
        ExerciseFormDialog(
            initial = null,
            library = library,
            onDismiss = { creating = false },
            onSave = { exercise ->
                library.add(exercise)
                creating = false
                onCreated(exercise)
            },
        )
    }
    removeTarget?.let { target ->
        RemoveFromLibraryDialog(
            exercise = target,
            onConfirm = {
                library.remove(target)
                removeTarget = null
            },
            onDismiss = { removeTarget = null },
        )
    }
}

@Composable
private fun LibraryList(
    library: List<Exercise>,
    emptyText: String,
    onPick: (Exercise) -> Unit,
    onRemove: (Exercise) -> Unit,
) {
    if (library.isEmpty()) DuiText(emptyText, color = DuiTheme.colors.textSecondary)
    library.forEachIndexed { index, exercise ->
        if (index > 0) DuiDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            DuiListRow(
                title = exercise.name,
                modifier = Modifier.weight(1f),
                subtitle = "${exercise.defaultSets} sets · ${exercise.defaultReps} reps",
                onClick = { onPick(exercise) },
            )
            DuiIconButton(
                DuiIcons.Close,
                "Remove ${exercise.name} from library",
                onClick = { onRemove(exercise) },
            )
        }
    }
}

@Composable
private fun RemoveFromLibraryDialog(exercise: Exercise, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    DuiDialog(
        onDismissRequest = onDismiss,
        title = "Remove from library?",
        confirmButton = { DuiButton("Remove", onClick = onConfirm) },
        dismissButton = {
            DuiButton("Keep exercise", onClick = onDismiss, style = DuiButtonStyle.Ghost)
        },
    ) {
        DuiText(
            "“${exercise.name}” will be removed from your library. Workouts that already use it keep it.",
            color = DuiTheme.colors.textSecondary,
        )
    }
}
