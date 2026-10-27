package com.duarte.workouttracker.ui.workout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duarte.workouttracker.AppContainer
import com.duarte.workouttracker.domain.Exercise
import com.duarte.workouttracker.domain.FinishedWorkout
import com.duarte.workouttracker.domain.RestMode
import com.duarte.workouttracker.domain.RestTimer
import com.duarte.workouttracker.domain.SupersetMember
import com.duarte.workouttracker.domain.Workout
import com.duarte.workouttracker.domain.WorkoutEngine
import com.duarte.workouttracker.domain.WorkoutItem
import com.duarte.workouttracker.domain.WorkoutItemType
import com.duarte.workouttracker.domain.WorkoutSettings
import com.duarte.workouttracker.ui.common.rememberImageBitmap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private data class RunSetup(
    val workout: Workout,
    val items: List<WorkoutItem>,
    val members: Map<String, List<SupersetMember>>,
    val exercises: List<Exercise>,
    val settings: WorkoutSettings,
)

@Composable
fun WorkoutRunScreen(workoutId: String, container: AppContainer, onExit: () -> Unit) {
    var setup by remember(workoutId) { mutableStateOf<RunSetup?>(null) }
    var loadError by remember(workoutId) { mutableStateOf(false) }
    LaunchedEffect(workoutId, container) {
        try {
            val workout = container.workouts.workouts.first().firstOrNull { it.id == workoutId }
            if (workout == null) {
                loadError = true
            } else {
                val exercises = container.exercises.exercises.first()
                val rows = container.database.exerciseQueries.selectWorkoutItems(workoutId).executeAsList()
                val items = rows.map { row ->
                    WorkoutItem(
                        id = row.id,
                        workoutId = row.workoutId,
                        position = row.position.toInt(),
                        type = WorkoutItemType.valueOf(row.type),
                        exerciseId = row.exerciseId,
                        sets = row.sets.toInt(),
                        rounds = row.rounds.toInt(),
                        reps = row.reps.toInt(),
                        restSeconds = row.restSeconds.toInt(),
                        notes = row.notes,
                    )
                }
                val members = items.filter { it.type == WorkoutItemType.SUPERSET }.associate { item ->
                    item.id to container.database.exerciseQueries.selectSupersetMembers(item.id).executeAsList().map { row ->
                        SupersetMember(row.id, row.itemId, row.position.toInt(), row.exerciseId, row.reps.toInt())
                    }
                }
                setup = RunSetup(workout, items, members, exercises, container.settings.settings.first())
            }
        } catch (_: Throwable) {
            loadError = true
        }
    }

    when {
        setup == null && !loadError -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        setup == null -> LoadFailure(onExit)
        else -> RunSession(setup!!, container, onExit)
    }
}

@Composable
private fun RunSession(setup: RunSetup, container: AppContainer, onExit: () -> Unit) {
    val exerciseNames = remember(setup.exercises) { setup.exercises.associate { it.id to it.name } }
    val engine = remember(setup) {
        WorkoutEngine(setup.workout, setup.items, setup.members, exerciseNames, setup.settings)
    }
    val state by engine.state.collectAsState()
    val timer = remember(container, setup.settings) {
        RestTimer(
            container.platform.timerNotifier,
            container.platform.feedback,
            soundEnabled = setup.settings.sound,
            vibrationEnabled = setup.settings.vibration,
        )
    }
    val remaining by timer.remainingSeconds.collectAsState()
    val scope = rememberCoroutineScope()
    var cursor by remember(engine) { mutableStateOf(0) }
    var actualReps by remember(engine) { mutableStateOf(state.targetReps ?: 0) }
    var restVisible by remember(engine) { mutableStateOf(false) }
    var restPaused by remember(engine) { mutableStateOf(false) }
    var finishedPayload by remember(engine) { mutableStateOf<FinishedWorkout?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf(false) }

    DisposableEffect(timer) { onDispose { timer.dispose() } }
    LaunchedEffect(state.restSuggestedSeconds, state.finished) {
        if (!state.finished && state.restMode == RestMode.AUTO && state.restSuggestedSeconds > 0) {
            timer.start(state.restSuggestedSeconds)
            restVisible = true
            restPaused = false
        }
    }
    LaunchedEffect(remaining) {
        if (remaining == 0) {
            restVisible = false
            restPaused = false
        }
    }
    LaunchedEffect(state.finished) {
        if (state.finished && finishedPayload == null) finishedPayload = engine.finish()
    }

    val currentItem = state.currentItem
    val currentMember = currentItem?.let { item ->
        setup.members[item.id].orEmpty().getOrNull(state.memberIndex ?: -1)
    }
    val currentExercise = currentMember?.let { member -> setup.exercises.firstOrNull { it.id == member.exerciseId } }
        ?: currentItem?.exerciseId?.let { id -> setup.exercises.firstOrNull { it.id == id } }

    if (state.finished) {
        FinishSummary(
            workoutName = setup.workout.name,
            completed = state.completedCount,
            total = state.total,
            saving = saving,
            saveError = saveError,
            onSave = {
                val payload = finishedPayload ?: engine.finish()
                if (payload != null && !saving) {
                    saving = true
                    saveError = false
                    scope.launch {
                        try {
                            container.history.addSession(payload.session, payload.entries)
                            onExit()
                        } catch (_: Throwable) {
                            saving = false
                            saveError = true
                        }
                    }
                }
            },
            onExit = onExit,
        )
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onExit, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
                Text("←  Exit", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.weight(1f))
            Text("${state.completedCount} / ${state.total} done", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { state.progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${setup.workout.name.uppercase()}  ·  SESSION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            if (state.isSuperset) "Round ${state.roundNumber ?: 1}" else "Set ${state.setNumber ?: 1}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    ItemTypeBadge(state.isSuperset)
                }
            }

            item {
                ExercisePhotoCard(currentExercise, setup.workout.name)
            }

            item {
                Column {
                    Text(currentExercise?.name ?: "Exercise", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    currentItem?.notes?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(5.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (state.isSuperset && currentItem != null) {
                item {
                    SupersetPanel(
                        members = setup.members[currentItem.id].orEmpty(),
                        exercises = setup.exercises,
                        currentIndex = state.memberIndex ?: 0,
                        round = state.roundNumber ?: 1,
                        rounds = currentItem.rounds,
                    )
                }
            }

            item {
                RepAdjuster(
                    target = state.targetReps ?: 0,
                    actual = actualReps,
                    onAdjust = { actualReps = (actualReps + it).coerceAtLeast(0) },
                )
            }
        }

        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 18.dp, top = 8.dp)) {
            if (state.restMode == RestMode.MANUAL && state.restSuggestedSeconds > 0) {
                OutlinedButton(
                    onClick = {
                        timer.start(state.restSuggestedSeconds)
                        restVisible = true
                        restPaused = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(17.dp),
                ) { Text("Start rest  ·  ${state.restSuggestedSeconds}s", fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(10.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = {
                        engine.previous()
                        cursor = (cursor - 1).coerceAtLeast(0)
                        actualReps = engine.state.value.targetReps ?: actualReps
                        timer.skip()
                        restVisible = false
                    },
                    enabled = cursor > 0,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(17.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text("←  Prev") }
                OutlinedButton(
                    onClick = {
                        engine.skip()
                        cursor++
                        actualReps = engine.state.value.targetReps ?: 0
                        restVisible = false
                        timer.skip()
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(17.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text("Skip") }
                Button(
                    onClick = {
                        engine.completeSet(actualReps)
                        cursor++
                        actualReps = engine.state.value.targetReps ?: 0
                    },
                    modifier = Modifier.weight(1.35f).height(56.dp),
                    shape = RoundedCornerShape(17.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) { Text("Complete  ✓", fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (restVisible && remaining > 0) {
        RestOverlay(
            remaining = remaining,
            paused = restPaused,
            onPauseResume = {
                if (restPaused) timer.resume() else timer.pause()
                restPaused = !restPaused
            },
            onAdjust = { timer.addSeconds(it) },
            onSkip = { timer.skip(); restVisible = false; restPaused = false },
        )
    }
}

@Composable
private fun ExercisePhotoCard(exercise: Exercise?, workoutName: String) {
    val image = rememberImageBitmap(exercise?.imagePath)
    Box(
        Modifier.fillMaxWidth().height(228.dp).clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF17291F), Color(0xFF354F3F), Color(0xFFB38B55))), RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(image, contentDescription = "${exercise?.name ?: workoutName} exercise photo", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .48f)))))
        } else {
            Text("✳", style = MaterialTheme.typography.displayLarge, color = Color.White.copy(alpha = .76f), fontWeight = FontWeight.Light)
            Text("MOVE WITH INTENT", Modifier.align(Alignment.BottomStart).padding(18.dp), style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ItemTypeBadge(isSuperset: Boolean) {
    val accent = if (isSuperset) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    Surface(color = accent.copy(alpha = .13f), shape = RoundedCornerShape(50)) {
        Text(
            if (isSuperset) "SUPERSET" else "REGULAR",
            Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun SupersetPanel(members: List<SupersetMember>, exercises: List<Exercise>, currentIndex: Int, round: Int, rounds: Int) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .52f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("THE CIRCUIT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("ROUND $round OF $rounds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            members.sortedBy { it.position }.forEachIndexed { index, member ->
                val active = index == currentIndex
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(if (active) MaterialTheme.colorScheme.surface.copy(alpha = .9f) else Color.Transparent)
                        .padding(horizontal = 11.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = .6f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(exercises.firstOrNull { it.id == member.exerciseId }?.name ?: "Exercise", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
                    Text("${member.reps} reps", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (active) Text("  NOW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
                if (index < members.lastIndex) Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(9.dp))
            LinearProgressIndicator(
                progress = { round.toFloat().div(rounds.coerceAtLeast(1)).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            )
        }
    }
}

@Composable
private fun RepAdjuster(target: Int, actual: Int, onAdjust: (Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("REPS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("Target $target", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RepButton("−", enabled = actual > 0, onClick = { onAdjust(-1) })
            Text("$actual", Modifier.width(58.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            RepButton("+", enabled = true, onClick = { onAdjust(1) })
        }
    }
}

@Composable
private fun RepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) { Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium) }
}

@Composable
private fun RestOverlay(remaining: Int, paused: Boolean, onPauseResume: () -> Unit, onAdjust: (Int) -> Unit, onSkip: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        shape = RoundedCornerShape(30.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("TAKE A BREATH", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("${(remaining / 60).toString().padStart(2, '0')}:${(remaining % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text(if (paused) "Paused" else "Resting", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedButton(onClick = { onAdjust(-15) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(15.dp)) { Text("−15 sec") }
                    OutlinedButton(onClick = { onAdjust(15) }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(15.dp)) { Text("+15 sec") }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onPauseResume, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(15.dp)) {
                    Text(if (paused) "Resume timer" else "Pause timer")
                }
            }
        },
        confirmButton = { Button(onClick = onSkip, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Skip rest", fontWeight = FontWeight.Bold) } },
    )
}

@Composable
private fun FinishSummary(
    workoutName: String,
    completed: Int,
    total: Int,
    saving: Boolean,
    saveError: Boolean,
    onSave: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(horizontal = 24.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("SESSION COMPLETE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("That’s a wrap.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(7.dp))
        Text(workoutName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(26.dp))
        Card(shape = RoundedCornerShape(25.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.fillMaxWidth().padding(22.dp)) {
                Text("SETS COMPLETED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("$completed / $total", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(if (completed == total) "Every set logged. Nice work." else "Skipped sets are saved as incomplete." , style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(23.dp))
        if (saveError) Text("Couldn’t save this session. Please try again.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onSave, enabled = !saving, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp)) {
            Text(if (saving) "Saving…" else "Save session", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Leave without saving") }
    }
}

@Composable
private fun LoadFailure(onExit: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Workout unavailable", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("This workout couldn’t be loaded. It may have been removed.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onExit, modifier = Modifier.height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Back to workouts") }
    }
}
