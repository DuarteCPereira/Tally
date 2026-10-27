package com.duarte.workouttracker.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
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

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←  Back") }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
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
            }) { Text("Save", fontWeight = FontWeight.Bold) }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(if (workout == null) "BUILD YOUR PLAN" else "REFINE YOUR PLAN", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(if (workout == null) "New workout" else "Edit workout", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(value = name, onValueChange = { name = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Workout name") }, placeholder = { Text("e.g. Upper body A") }, singleLine = true, shape = RoundedCornerShape(18.dp))
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 12.dp, top = 5.dp)) }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Training flow", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${items.size} ${if (items.size == 1) "block" else "blocks"}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (items.isEmpty()) item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(24.dp)) {
                    Text("Your workout starts here", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text("Add an exercise or group a few into a superset.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { exerciseDialog = true; editing = null }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("＋ Exercise") }
                    OutlinedButton(onClick = { supersetDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("＋ Superset") }
                }
                if (library.isNotEmpty()) {
                    TextButton(onClick = { libraryDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Browse exercise library  →") }
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
        SupersetFormDialog(library, initial = editingSupersetIndex?.let { items.getOrNull(it) }, onDismiss = { supersetDialog = false; editingSupersetIndex = null }, onSave = { item ->
            val existingIndex = editingSupersetIndex
            if (existingIndex != null && existingIndex in items.indices) items[existingIndex] = item else items.add(item)
            editingSupersetIndex = null
            supersetDialog = false
        })
    }
    if (libraryDialog) {
        AlertDialog(onDismissRequest = { libraryDialog = false }, title = { Text("Exercise library") },
            text = { LazyColumn(Modifier.heightIn(max = 420.dp)) { items(library, key = { it.id }) { exercise ->
                TextButton(onClick = { items.add(DraftItem(WorkoutItemType.REGULAR, exercise, rounds = exercise.defaultSets, reps = exercise.defaultReps, rest = exercise.defaultRestSeconds)); libraryDialog = false }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) { Text(exercise.name, fontWeight = FontWeight.SemiBold); Text("${exercise.defaultSets} sets · ${exercise.defaultReps} reps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            } } }, confirmButton = { TextButton(onClick = { libraryDialog = false }) { Text("Done") } })
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise?, index: Int, count: Int, onUp: () -> Unit, onDown: () -> Unit, onEdit: () -> Unit, onRemove: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            OrderButtons(index, count, onUp, onDown)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(exercise?.name ?: "Exercise", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("${exercise?.defaultSets ?: 3} sets  ·  ${exercise?.defaultReps ?: 10} reps  ·  ${exercise?.defaultRestSeconds ?: 60}s rest", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Edit") }
                TextButton(onClick = onRemove, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun SupersetCard(item: DraftItem, index: Int, count: Int, onUp: () -> Unit, onDown: () -> Unit, onEdit: () -> Unit, onRemove: () -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .48f))) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            OrderButtons(index, count, onUp, onDown)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("SUPERSET  ·  ${item.rounds} ROUNDS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                item.members.forEachIndexed { i, member -> Text("${i + 1}. ${member.name}  ·  ${member.defaultReps} reps", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
                Spacer(Modifier.height(5.dp))
                Text("${item.rest}s rest between rounds", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Edit") }
                TextButton(onClick = onRemove, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
private fun OrderButtons(index: Int, count: Int, up: () -> Unit, down: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(30.dp)) {
        TextButton(onClick = up, enabled = index > 0, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(30.dp)) { Text("↑") }
        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = down, enabled = index < count - 1, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(30.dp)) { Text("↓") }
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Add exercise" else "Edit exercise") },
        text = {
            Column(Modifier.heightIn(max = 620.dp).then(Modifier), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                androidx.compose.foundation.rememberScrollState().let { scroll -> Column(Modifier.weight(1f, fill = false).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    OutlinedTextField(name, { name = it; validation = null }, label = { Text("Exercise name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberField("Sets", sets, { sets = it }, Modifier.weight(1f))
                        NumberField("Reps", reps, { reps = it }, Modifier.weight(1f))
                    }
                    NumberField("Rest (seconds)", rest, { rest = it }, Modifier.fillMaxWidth())
                    OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    imagePath?.let { path ->
                        val bitmap = rememberImageBitmap(path)
                        if (bitmap != null) Image(bitmap, contentDescription = "Exercise photo", modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
                        TextButton(onClick = { imagePath = null }) { Text("Remove photo") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { scope.launch { container.platform.imagePicker.pickFromGallery()?.let { source -> imagePath = container.platform.imageStorage.copyPickedImage(source) } } }, modifier = Modifier.weight(1f)) { Text("Choose photo") }
                        OutlinedButton(onClick = { scope.launch { container.platform.imagePicker.takePhoto()?.let { source -> imagePath = container.platform.imageStorage.copyPickedImage(source) } } }, modifier = Modifier.weight(1f)) { Text("Take photo") }
                    }
                    validation?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                } }
            }
        },
        confirmButton = { TextButton(onClick = {
            val parsedSets = sets.toIntOrNull(); val parsedReps = reps.toIntOrNull(); val parsedRest = rest.toIntOrNull()
            if (name.isBlank()) validation = "Enter an exercise name."
            else if (parsedSets == null || parsedReps == null || parsedRest == null || parsedSets < 1 || parsedReps < 1 || parsedRest < 0) validation = "Use positive sets and reps, and a non-negative rest."
            else onSave(Exercise(initial?.id ?: "exercise-${KotlinClock.System.now().toEpochMilliseconds()}", name.trim(), parsedSets, parsedReps, parsedRest, notes.trim().ifEmpty { null }, imagePath))
        }) { Text("Save exercise") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(value, { onChange(it.filter(Char::isDigit)) }, modifier = modifier, label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
}

@Composable
private fun SupersetFormDialog(library: List<Exercise>, initial: DraftItem?, onDismiss: () -> Unit, onSave: (DraftItem) -> Unit) {
    val members = remember(initial) { mutableStateListOf<Exercise>().apply { addAll(initial?.members.orEmpty()) } }
    var rounds by remember(initial) { mutableStateOf((initial?.rounds ?: 3).toString()) }
    var reps by remember(initial) { mutableStateOf((initial?.reps ?: 10).toString()) }
    var rest by remember(initial) { mutableStateOf((initial?.rest ?: 60).toString()) }
    var selecting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Build a superset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.heightIn(max = 560.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                Text("Move from one exercise to the next, then repeat for each round.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                members.forEachIndexed { index, exercise ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f))) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}. ${exercise.name}", Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            TextButton(onClick = { if (index > 0) { val prior = members[index - 1]; members[index - 1] = exercise; members[index] = prior } }, enabled = index > 0) { Text("↑") }
                            TextButton(onClick = { if (index < members.lastIndex) { val next = members[index + 1]; members[index + 1] = exercise; members[index] = next } }, enabled = index < members.lastIndex) { Text("↓") }
                            TextButton(onClick = { members.removeAt(index) }) { Text("×", color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
                OutlinedButton(onClick = { selecting = true }, modifier = Modifier.fillMaxWidth()) { Text("＋ Add a member") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Rounds", rounds, { rounds = it }, Modifier.weight(1f))
                    NumberField("Reps", reps, { reps = it }, Modifier.weight(1f))
                }
                NumberField("Rest between rounds (seconds)", rest, { rest = it }, Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (library.isEmpty()) Text("Save exercises to your library first, or add an exercise from the workout editor.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = {
            val parsedRounds = rounds.toIntOrNull(); val parsedReps = reps.toIntOrNull(); val parsedRest = rest.toIntOrNull()
            if (members.size < 2) error = "A superset needs at least two exercises."
            else if (parsedRounds == null || parsedReps == null || parsedRest == null || parsedRounds < 1 || parsedReps < 1 || parsedRest < 0) error = "Enter positive rounds and reps, and a non-negative rest."
            else onSave(DraftItem(WorkoutItemType.SUPERSET, members = members.map { it.copy(defaultReps = parsedReps) }, rounds = parsedRounds, reps = parsedReps, rest = parsedRest))
        }) { Text("Save superset") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
    if (selecting) AlertDialog(onDismissRequest = { selecting = false }, title = { Text("Choose an exercise") },
        text = { if (library.isEmpty()) Text("Your exercise library is empty. Save a regular exercise first.") else LazyColumn(Modifier.heightIn(max = 350.dp)) { items(library, key = { it.id }) { exercise ->
            TextButton(onClick = { members.add(exercise); selecting = false }, modifier = Modifier.fillMaxWidth()) { Text(exercise.name, modifier = Modifier.fillMaxWidth()) }
        } } }, confirmButton = { TextButton(onClick = { selecting = false }) { Text("Done") } })
}
