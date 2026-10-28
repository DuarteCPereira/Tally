package com.tally.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.tally.domain.Exercise
import com.tally.domain.newId
import com.tally.ui.common.rememberImageBitmap
import kotlinx.coroutines.launch

/**
 * Adds a new exercise, or edits [initial]. The photo buttons call [onTakePhoto], which returns the
 * stored photo's path or `null` if the user cancelled.
 */
@Composable
internal fun ExerciseFormDialog(
    initial: Exercise?,
    onTakePhoto: suspend (PhotoSource) -> String?,
    onDismiss: () -> Unit,
    onSave: (Exercise) -> Unit,
) {
    var form by remember(initial) { mutableStateOf(initial?.let(ExerciseForm::of) ?: ExerciseForm()) }
    var validation by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val spacing = DuiTheme.spacing

    DuiDialog(
        onDismissRequest = onDismiss,
        title = if (initial == null) "Add exercise" else "Edit exercise",
        confirmButton = {
            DuiButton("Save exercise", onClick = {
                when (val result = form.validate(id = initial?.id ?: newId())) {
                    is FormResult.Valid -> onSave(result.value)
                    is FormResult.Invalid -> validation = result.message
                }
            })
        },
        dismissButton = { DuiButton("Cancel", onClick = onDismiss, style = DuiButtonStyle.Ghost) },
    ) {
        DuiTextField(
            form.name,
            {
                form = form.copy(name = it)
                validation = null
            },
            Modifier.fillMaxWidth(),
            label = "Exercise name",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            NumberField("Sets", form.sets, { form = form.copy(sets = it) }, Modifier.weight(1f))
            NumberField("Reps", form.reps, { form = form.copy(reps = it) }, Modifier.weight(1f))
        }
        NumberField("Rest (seconds)", form.rest, { form = form.copy(rest = it) }, Modifier.fillMaxWidth())
        DuiTextField(
            form.notes,
            { form = form.copy(notes = it) },
            Modifier.fillMaxWidth(),
            label = "Notes (optional)",
            singleLine = false,
            minLines = 2,
        )
        form.imagePath?.let { path ->
            rememberImageBitmap(path)?.let { DuiImageCard(it, "Exercise photo") }
            DuiButton(
                "Remove photo",
                onClick = { form = form.copy(imagePath = null) },
                style = DuiButtonStyle.Ghost,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            PhotoSource.entries.forEach { source ->
                DuiButton(
                    source.label,
                    onClick = {
                        scope.launch { onTakePhoto(source)?.let { form = form.copy(imagePath = it) } }
                    },
                    modifier = Modifier.weight(1f),
                    style = DuiButtonStyle.Secondary,
                )
            }
        }
        validation?.let { DuiText(it, style = DuiTextStyle.Caption, color = DuiTheme.colors.negative) }
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
