package com.duarte.workouttracker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.duarte.dui.components.DuiBadge
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiCardTone
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiEmptyState
import com.duarte.dui.components.DuiIconButton
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiImageCard
import com.duarte.dui.components.DuiListRow
import com.duarte.dui.components.DuiMenu
import com.duarte.dui.components.DuiMenuItem
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextField
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.components.DuiTopBar
import com.duarte.dui.theme.DuiTheme
import com.duarte.workouttracker.AppContainer
import com.duarte.workouttracker.domain.*
import com.duarte.workouttracker.ui.common.rememberImageBitmap
import kotlinx.coroutines.launch
import kotlin.time.Clock as KotlinClock

internal data class DraftItem(val type: WorkoutItemType, val exercise: Exercise? = null, val members: List<Exercise> = emptyList(), val rounds: Int = 3, val reps: Int = 10, val rest: Int = 60)
internal object DraftStore { val byWorkout = mutableMapOf<String, List<DraftItem>>() }

internal suspend fun duplicateWorkoutWithItems(container: AppContainer, workoutId: String) {
    val duplicate = container.workouts.duplicate(workoutId) ?: return
    DraftStore.byWorkout[workoutId]?.let { DraftStore.byWorkout[duplicate.id] = it.toList() }
}

@Composable
fun WorkoutEditorScreen(container: AppContainer, workoutId: String?, onBack: () -> Unit) {
    val workouts by container.workouts.workouts.collectAsState(initial = emptyList())
    val library by container.exercises.exercises.collectAsState(initial = emptyList())
    val workout = workouts.firstOrNull { it.id == workoutId }
    val scope = rememberCoroutineScope()
    var name by remember(workoutId) { mutableStateOf(workout?.name.orEmpty()) }
    val items = remember(workoutId) { mutableStateListOf<DraftItem>().apply { addAll(workoutId?.let { DraftStore.byWorkout[it] }.orEmpty()) } }
    var exerciseDialog by remember { mutableStateOf(false) }
    var supersetDialog by remember { mutableStateOf(false) }
    var editingSupersetIndex by remember { mutableStateOf<Int?>(null) }
    var libraryDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Pair<Int, Exercise>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var newInLibrary by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<Exercise?>(null) }
    val spacing = DuiTheme.spacing

    Column(Modifier.fillMaxSize()) {
        DuiTopBar(onBack = onBack, actionText = "Save", onAction = {
            val trimmed = name.trim()
            if (trimmed.isBlank()) error = "Give your workout a name first."
            else scope.launch {
                val now = KotlinClock.System.now()
                val savedWorkout = if (workout == null) Workout("workout-${now.toEpochMilliseconds()}", trimmed, workouts.size, now) else workout.copy(name = trimmed)
                if (workout != null) container.workouts.delete(workout.id)
                container.workouts.add(savedWorkout)
                items.filter { it.type == WorkoutItemType.REGULAR }.mapNotNull { it.exercise }.forEach { container.exercises.save(it) }
                items.flatMap { it.members }.forEach { container.exercises.save(it) }
                DraftStore.byWorkout[savedWorkout.id] = items.toList()
                onBack()
            }
        })
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = spacing.space4, end = spacing.space4, bottom = spacing.space6), verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space4)) {
                    DuiText(if (workout == null) "New workout" else "Edit workout", style = DuiTextStyle.Title)
                    DuiTextField(value = name, onValueChange = { name = it; error = null }, modifier = Modifier.fillMaxWidth(), label = "Workout name", placeholder = "e.g. Upper body A", error = error)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        DuiText("Training flow", style = DuiTextStyle.Title)
                        DuiText("${items.size} ${if (items.size == 1) "block" else "blocks"}", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
                    }
                }
            }
            if (items.isEmpty()) item {
                DuiEmptyState(title = "Your workout starts here", body = "Add an exercise or group a few into a superset.")
            }
            items(items.size, key = { index -> "${index}-${items[index].type}" }) { index ->
                val item = items[index]
                if (item.type == WorkoutItemType.SUPERSET) {
                    SupersetCard(item, index, items.size,
                        onUp = { if (index > 0) { val previous = items[index - 1]; items[index - 1] = item; items[index] = previous } },
                        onDown = { if (index < items.lastIndex) { val next = items[index + 1]; items[index + 1] = item; items[index] = next } },
                        onEdit = { editingSupersetIndex = index; supersetDialog = true }, onRemove = { items.removeAt(index) })
                } else {
                    ExerciseCard(item.exercise, index, items.size,
                        onUp = { if (index > 0) { val previous = items[index - 1]; items[index - 1] = item; items[index] = previous } },
                        onDown = { if (index < items.lastIndex) { val next = items[index + 1]; items[index + 1] = item; items[index] = next } },
                        onEdit = { item.exercise?.let { editing = index to it; exerciseDialog = true } }, onRemove = { items.removeAt(index) })
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                        DuiButton("Add exercise", onClick = { exerciseDialog = true; editing = null }, modifier = Modifier.weight(1f), style = DuiButtonStyle.Secondary)
                        DuiButton("Add superset", onClick = { supersetDialog = true }, modifier = Modifier.weight(1f), style = DuiButtonStyle.Secondary)
                    }
                    DuiButton("Browse exercise library", onClick = { libraryDialog = true }, style = DuiButtonStyle.Ghost)
                }
            }
        }
    }

    if (exerciseDialog) {
        ExerciseFormDialog(container, initial = editing?.second, onDismiss = { exerciseDialog = false }, onSave = { exercise ->
            val at = editing?.first
            if (at != null && at in items.indices) items[at] = items[at].copy(exercise = exercise)
            else items.add(DraftItem(WorkoutItemType.REGULAR, exercise = exercise, rounds = exercise.defaultSets, reps = exercise.defaultReps, rest = exercise.defaultRestSeconds))
            exerciseDialog = false
        })
    }
    if (supersetDialog) {
        SupersetFormDialog(container, library, initial = editingSupersetIndex?.let { items.getOrNull(it) }, onDismiss = { supersetDialog = false; editingSupersetIndex = null }, onSave = { item ->
            val existingIndex = editingSupersetIndex
            if (existingIndex != null && existingIndex in items.indices) items[existingIndex] = item else items.add(item)
            editingSupersetIndex = null
            supersetDialog = false
        })
    }
    if (libraryDialog) {
        DuiDialog(onDismissRequest = { libraryDialog = false }, title = "Exercise library",
            confirmButton = { DuiButton("Done", onClick = { libraryDialog = false }) }) {
            DuiButton("New exercise", onClick = { newInLibrary = true }, style = DuiButtonStyle.Secondary)
            LibraryList(
                library = library,
                emptyText = "Your library is empty. Add your first exercise.",
                onPick = { exercise ->
                    items.add(DraftItem(WorkoutItemType.REGULAR, exercise, rounds = exercise.defaultSets, reps = exercise.defaultReps, rest = exercise.defaultRestSeconds))
                    libraryDialog = false
                },
                onRemove = { removeTarget = it },
            )
        }
    }
    if (newInLibrary) {
        ExerciseFormDialog(container, initial = null, onDismiss = { newInLibrary = false }, onSave = { exercise ->
            scope.launch { container.exercises.save(exercise) }
            newInLibrary = false
        })
    }
    removeTarget?.let { target ->
        RemoveFromLibraryDialog(target, onConfirm = {
            scope.launch { runCatching { container.exercises.delete(target.id) } }
            removeTarget = null
        }, onDismiss = { removeTarget = null })
    }
}

@Composable
private fun LibraryList(library: List<Exercise>, emptyText: String, onPick: (Exercise) -> Unit, onRemove: (Exercise) -> Unit) {
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
            DuiIconButton(DuiIcons.Close, "Remove ${exercise.name} from library", onClick = { onRemove(exercise) })
        }
    }
}

@Composable
private fun RemoveFromLibraryDialog(exercise: Exercise, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    DuiDialog(
        onDismissRequest = onDismiss,
        title = "Remove from library?",
        confirmButton = { DuiButton("Remove", onClick = onConfirm) },
        dismissButton = { DuiButton("Keep exercise", onClick = onDismiss, style = DuiButtonStyle.Ghost) },
    ) {
        DuiText("“${exercise.name}” will be removed from your library. Workouts that already use it keep it.", color = DuiTheme.colors.textSecondary)
    }
}

@Composable
private fun BlockMenu(onEdit: () -> Unit, onRemove: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    DuiMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        items = listOf(DuiMenuItem("Edit", onEdit), DuiMenuItem("Remove", onRemove)),
    ) {
        DuiIconButton(DuiIcons.More, "More options", onClick = { expanded = true })
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise?, index: Int, count: Int, onUp: () -> Unit, onDown: () -> Unit, onEdit: () -> Unit, onRemove: () -> Unit) {
    DuiCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OrderButtons(index, count, onUp, onDown)
            Column(Modifier.weight(1f).padding(horizontal = DuiTheme.spacing.space3)) {
                DuiText(exercise?.name ?: "Exercise", style = DuiTextStyle.Label)
                DuiText("${exercise?.defaultSets ?: 3} sets  ·  ${exercise?.defaultReps ?: 10} reps  ·  ${exercise?.defaultRestSeconds ?: 60}s rest", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            }
            BlockMenu(onEdit, onRemove)
        }
    }
}

@Composable
private fun SupersetCard(item: DraftItem, index: Int, count: Int, onUp: () -> Unit, onDown: () -> Unit, onEdit: () -> Unit, onRemove: () -> Unit) {
    val spacing = DuiTheme.spacing
    DuiCard(tone = DuiCardTone.Accent) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OrderButtons(index, count, onUp, onDown)
            Column(Modifier.weight(1f).padding(horizontal = spacing.space3), verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    DuiBadge("Superset")
                    DuiText("${item.rounds} rounds", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
                }
                item.members.forEachIndexed { i, member -> DuiText("${i + 1}. ${member.name}  ·  ${member.defaultReps} reps", style = DuiTextStyle.Label) }
                DuiText("${item.rest}s rest between rounds", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            }
            BlockMenu(onEdit, onRemove)
        }
    }
}

@Composable
private fun OrderButtons(index: Int, count: Int, up: () -> Unit, down: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DuiIconButton(DuiIcons.ArrowUp, "Move up", up, enabled = index > 0)
        DuiText("${index + 1}", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
        DuiIconButton(DuiIcons.ArrowDown, "Move down", down, enabled = index < count - 1)
    }
}

@Composable
private fun ExerciseFormDialog(container: AppContainer, initial: Exercise?, onDismiss: () -> Unit, onSave: (Exercise) -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var sets by remember(initial) { mutableStateOf((initial?.defaultSets ?: 3).toString()) }
    var reps by remember(initial) { mutableStateOf((initial?.defaultReps ?: 10).toString()) }
    var rest by remember(initial) { mutableStateOf((initial?.defaultRestSeconds ?: 60).toString()) }
    var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }
    var imagePath by remember(initial) { mutableStateOf(initial?.imagePath) }
    var validation by remember { mutableStateOf<String?>(null) }
    val spacing = DuiTheme.spacing
    DuiDialog(
        onDismissRequest = onDismiss,
        title = if (initial == null) "Add exercise" else "Edit exercise",
        confirmButton = {
            DuiButton("Save exercise", onClick = {
                val parsedSets = sets.toIntOrNull(); val parsedReps = reps.toIntOrNull(); val parsedRest = rest.toIntOrNull()
                if (name.isBlank()) validation = "Enter an exercise name."
                else if (parsedSets == null || parsedReps == null || parsedRest == null || parsedSets < 1 || parsedReps < 1 || parsedRest < 0) validation = "Use positive sets and reps, and a non-negative rest."
                else onSave(Exercise(initial?.id ?: "exercise-${KotlinClock.System.now().toEpochMilliseconds()}", name.trim(), parsedSets, parsedReps, parsedRest, notes.trim().ifEmpty { null }, imagePath))
            })
        },
        dismissButton = { DuiButton("Cancel", onClick = onDismiss, style = DuiButtonStyle.Ghost) },
    ) {
        DuiTextField(name, { name = it; validation = null }, Modifier.fillMaxWidth(), label = "Exercise name")
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            NumberField("Sets", sets, { sets = it }, Modifier.weight(1f))
            NumberField("Reps", reps, { reps = it }, Modifier.weight(1f))
        }
        NumberField("Rest (seconds)", rest, { rest = it }, Modifier.fillMaxWidth())
        DuiTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = "Notes (optional)", singleLine = false, minLines = 2)
        imagePath?.let { path ->
            val bitmap = rememberImageBitmap(path)
            if (bitmap != null) DuiImageCard(bitmap, "Exercise photo")
            DuiButton("Remove photo", onClick = { imagePath = null }, style = DuiButtonStyle.Ghost)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            DuiButton("Choose photo", onClick = { scope.launch { container.platform.imagePicker.pickFromGallery()?.let { source -> imagePath = container.platform.imageStorage.copyPickedImage(source) } } }, modifier = Modifier.weight(1f), style = DuiButtonStyle.Secondary)
            DuiButton("Take photo", onClick = { scope.launch { container.platform.imagePicker.takePhoto()?.let { source -> imagePath = container.platform.imageStorage.copyPickedImage(source) } } }, modifier = Modifier.weight(1f), style = DuiButtonStyle.Secondary)
        }
        validation?.let { DuiText(it, style = DuiTextStyle.Caption, color = DuiTheme.colors.negative) }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    DuiTextField(value, { onChange(it.filter(Char::isDigit)) }, modifier, label = label, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

@Composable
private fun SupersetFormDialog(container: AppContainer, library: List<Exercise>, initial: DraftItem?, onDismiss: () -> Unit, onSave: (DraftItem) -> Unit) {
    val members = remember(initial) { mutableStateListOf<Exercise>().apply { addAll(initial?.members.orEmpty()) } }
    var rounds by remember(initial) { mutableStateOf((initial?.rounds ?: 3).toString()) }
    var reps by remember(initial) { mutableStateOf((initial?.reps ?: 10).toString()) }
    var rest by remember(initial) { mutableStateOf((initial?.rest ?: 60).toString()) }
    var selecting by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<Exercise?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val spacing = DuiTheme.spacing
    DuiDialog(
        onDismissRequest = onDismiss,
        title = "Build a superset",
        confirmButton = {
            DuiButton("Save superset", onClick = {
                val parsedRounds = rounds.toIntOrNull(); val parsedReps = reps.toIntOrNull(); val parsedRest = rest.toIntOrNull()
                if (members.size < 2) error = "A superset needs at least two exercises."
                else if (parsedRounds == null || parsedReps == null || parsedRest == null || parsedRounds < 1 || parsedReps < 1 || parsedRest < 0) error = "Enter positive rounds and reps, and a non-negative rest."
                else onSave(DraftItem(WorkoutItemType.SUPERSET, members = members.map { it.copy(defaultReps = parsedReps) }, rounds = parsedRounds, reps = parsedReps, rest = parsedRest))
            })
        },
        dismissButton = { DuiButton("Cancel", onClick = onDismiss, style = DuiButtonStyle.Ghost) },
    ) {
        DuiText("Move from one exercise to the next, then repeat for each round.", style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
        members.forEachIndexed { index, exercise ->
            DuiCard(tone = DuiCardTone.Accent) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    DuiText("${index + 1}. ${exercise.name}", Modifier.weight(1f), style = DuiTextStyle.Label)
                    DuiIconButton(DuiIcons.ArrowUp, "Move up", { if (index > 0) { val prior = members[index - 1]; members[index - 1] = exercise; members[index] = prior } }, enabled = index > 0)
                    DuiIconButton(DuiIcons.ArrowDown, "Move down", { if (index < members.lastIndex) { val next = members[index + 1]; members[index + 1] = exercise; members[index] = next } }, enabled = index < members.lastIndex)
                    DuiIconButton(DuiIcons.Close, "Remove ${exercise.name}", { members.removeAt(index) })
                }
            }
        }
        DuiButton("Add member", onClick = { selecting = true }, style = DuiButtonStyle.Secondary)
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            NumberField("Rounds", rounds, { rounds = it }, Modifier.weight(1f))
            NumberField("Reps", reps, { reps = it }, Modifier.weight(1f))
        }
        NumberField("Rest between rounds (seconds)", rest, { rest = it }, Modifier.fillMaxWidth())
        error?.let { DuiText(it, style = DuiTextStyle.Caption, color = DuiTheme.colors.negative) }
    }
    if (selecting) DuiDialog(onDismissRequest = { selecting = false }, title = "Choose an exercise",
        confirmButton = { DuiButton("Done", onClick = { selecting = false }) }) {
        DuiButton("New exercise", onClick = { creating = true }, style = DuiButtonStyle.Secondary)
        LibraryList(
            library = library,
            emptyText = "Your exercise library is empty. Add a new exercise to start.",
            onPick = { members.add(it); selecting = false },
            onRemove = { removeTarget = it },
        )
    }
    if (creating) {
        ExerciseFormDialog(container, initial = null, onDismiss = { creating = false }, onSave = { exercise ->
            scope.launch { container.exercises.save(exercise) }
            members.add(exercise)
            creating = false
            selecting = false
        })
    }
    removeTarget?.let { target ->
        RemoveFromLibraryDialog(target, onConfirm = {
            scope.launch { runCatching { container.exercises.delete(target.id) } }
            removeTarget = null
        }, onDismiss = { removeTarget = null })
    }
}
