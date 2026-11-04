package com.tally.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiEmptyState
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextField
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.components.DuiTopBar
import com.duarte.dui.theme.DuiTheme
import com.tally.ui.common.DelayedLoadingIndicator

/** The dialog currently open over the editor. A `null` block means "add" rather than "edit". */
private sealed interface EditorDialog {
    data class EditExercise(val block: DraftBlock.Single?) : EditorDialog
    data class EditSuperset(val block: DraftBlock.Superset?) : EditorDialog
    data object Library : EditorDialog
}

@Composable
fun WorkoutEditorScreen(
    editor: WorkoutEditorViewModel,
    library: ExerciseLibraryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by editor.state.collectAsStateWithLifecycle()
    Column(modifier.fillMaxSize()) {
        when (val current = state) {
            EditorUiState.Loading -> {
                DuiTopBar(onBack = onBack)
                DelayedLoadingIndicator()
            }

            is EditorUiState.Content -> EditorContent(current, editor, library, onBack)
        }
    }
}

@Composable
private fun EditorContent(
    state: EditorUiState.Content,
    editor: WorkoutEditorViewModel,
    library: ExerciseLibraryViewModel,
    onBack: () -> Unit,
) {
    var dialog by remember { mutableStateOf<EditorDialog?>(null) }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onBack()
    }

    DuiTopBar(onBack = onBack, actionText = "Save", onAction = editor::save)
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = DuiTheme.spacing.space4,
            end = DuiTheme.spacing.space4,
            bottom = DuiTheme.spacing.space6,
        ),
        verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space3),
    ) {
        item {
            EditorHeader(state, name = editor.name, onRename = editor::rename)
        }
        if (state.blocks.isEmpty()) {
            item {
                DuiEmptyState(
                    title = "Your workout starts here",
                    body = "Add an exercise or group a few into a superset.",
                )
            }
        }
        itemsIndexed(state.blocks, key = { _, block -> block.key }) { index, block ->
            BlockCard(
                block = block,
                actions = BlockActions(
                    index = index,
                    count = state.blocks.size,
                    onMove = { offset -> editor.move(block.key, offset) },
                    onEdit = { dialog = block.toEditDialog() },
                    onRemove = { editor.remove(block.key) },
                ),
            )
        }
        item {
            AddBlockButtons(onOpen = { dialog = it })
        }
    }

    dialog?.let { open ->
        EditorDialogHost(open, editor, library, onClose = { dialog = null })
    }
}

@Composable
private fun EditorHeader(state: EditorUiState.Content, name: String, onRename: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space4)) {
        DuiText(if (state.isNew) "New workout" else "Edit workout", style = DuiTextStyle.Title)
        DuiTextField(
            value = name,
            onValueChange = onRename,
            modifier = Modifier.fillMaxWidth(),
            label = "Workout name",
            placeholder = "e.g. Upper body A",
            error = state.nameError,
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val count = state.blocks.size
            DuiText("Training flow", style = DuiTextStyle.Title)
            DuiText(
                "$count ${if (count == 1) "block" else "blocks"}",
                style = DuiTextStyle.Caption,
                color = DuiTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun AddBlockButtons(onOpen: (EditorDialog) -> Unit) {
    val spacing = DuiTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            DuiButton(
                "Add exercise",
                onClick = { onOpen(EditorDialog.EditExercise(block = null)) },
                modifier = Modifier.weight(1f),
                style = DuiButtonStyle.Secondary,
            )
            DuiButton(
                "Add superset",
                onClick = { onOpen(EditorDialog.EditSuperset(block = null)) },
                modifier = Modifier.weight(1f),
                style = DuiButtonStyle.Secondary,
            )
        }
        DuiButton(
            "Browse exercise library",
            onClick = { onOpen(EditorDialog.Library) },
            style = DuiButtonStyle.Ghost,
        )
    }
}

@Composable
private fun EditorDialogHost(
    dialog: EditorDialog,
    editor: WorkoutEditorViewModel,
    library: ExerciseLibraryViewModel,
    onClose: () -> Unit,
) {
    when (dialog) {
        is EditorDialog.EditExercise -> ExerciseFormDialog(
            initial = dialog.block?.exercise,
            onDismiss = onClose,
            onSave = { exercise ->
                dialog.block
                    ?.let { editor.replaceExercise(it.key, exercise) }
                    ?: editor.addExercise(exercise)
                onClose()
            },
        )

        is EditorDialog.EditSuperset -> SupersetFormDialog(
            initial = dialog.block,
            library = library,
            onDismiss = onClose,
            onSave = { superset ->
                editor.saveSuperset(superset)
                onClose()
            },
        )

        EditorDialog.Library -> LibraryPickerDialog(
            title = "Exercise library",
            emptyText = "Your library is empty. Add your first exercise.",
            library = library,
            onPick = { exercise ->
                editor.addExercise(exercise)
                onClose()
            },
            // New exercises only join the library here; picking one adds it to the workout
            onCreated = {},
            onDismiss = onClose,
        )
    }
}

private fun DraftBlock.toEditDialog(): EditorDialog = when (this) {
    is DraftBlock.Single -> EditorDialog.EditExercise(block = this)
    is DraftBlock.Superset -> EditorDialog.EditSuperset(block = this)
}
