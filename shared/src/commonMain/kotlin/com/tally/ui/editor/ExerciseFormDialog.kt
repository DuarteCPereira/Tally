package com.tally.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiImageCard
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextField
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tally.domain.Exercise
import com.tally.ui.common.ViewModelStoreScope
import com.tally.ui.common.rememberImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Adds a new exercise, or edits [initial]. Its draft, validation and photo picking live in an
 * [ExerciseFormViewModel] scoped to the dialog, so reopening it always starts from [initial].
 */
@Composable
internal fun ExerciseFormDialog(
    initial: Exercise?,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
) {
    ViewModelStoreScope(key = initial?.id) {
        val form = koinViewModel<ExerciseFormViewModel> { parametersOf(initial) }
        ExerciseFormContent(initial == null, form, onDismiss, onSave)
    }
}

@Composable
private fun ExerciseFormContent(
    isNew: Boolean,
    viewModel: ExerciseFormViewModel,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val form = state.form
    val spacing = DuiTheme.spacing

    DuiDialog(
        onDismissRequest = onDismiss,
        title = if (isNew) "Add exercise" else "Edit exercise",
        confirmButton = { DuiButton("Save exercise", onClick = { viewModel.save()?.let(onSave) }) },
        dismissButton = { DuiButton("Cancel", onClick = onDismiss, style = DuiButtonStyle.Ghost) },
    ) {
        DuiTextField(form.name, { viewModel.edit { copy(name = it) } }, Modifier.fillMaxWidth(), label = "Exercise name")
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            NumberField("Sets", form.sets, { viewModel.edit { copy(sets = it) } }, Modifier.weight(1f))
            NumberField("Reps", form.reps, { viewModel.edit { copy(reps = it) } }, Modifier.weight(1f))
        }
        NumberField("Rest (seconds)", form.rest, { viewModel.edit { copy(rest = it) } }, Modifier.fillMaxWidth())
        DuiTextField(
            form.notes,
            { viewModel.edit { copy(notes = it) } },
            Modifier.fillMaxWidth(),
            label = "Notes (optional)",
            singleLine = false,
            minLines = 2,
        )
        form.imagePath?.let { path ->
            rememberImageBitmap(path)?.let { DuiImageCard(it, "Exercise photo") }
            DuiButton(
                "Remove photo",
                onClick = { viewModel.edit { copy(imagePath = null) } },
                style = DuiButtonStyle.Ghost,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            PhotoSource.entries.forEach { source ->
                DuiButton(
                    source.label,
                    onClick = { viewModel.pickPhoto(source) },
                    modifier = Modifier.weight(1f),
                    style = DuiButtonStyle.Secondary,
                    enabled = !state.isPickingPhoto,
                )
            }
        }
        state.message?.let { DuiText(it, style = DuiTextStyle.Caption, color = DuiTheme.colors.negative) }
    }
}

/** A text field that only accepts digits and opens the numeric keyboard. */
@Composable
internal fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    DuiTextField(
        value,
        { onChange(it.filter(Char::isDigit)) },
        modifier,
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

private val PhotoSource.label: String
    get() = when (this) {
        PhotoSource.GALLERY -> "Choose photo"
        PhotoSource.CAMERA -> "Take photo"
    }
