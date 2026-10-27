package com.duarte.workouttracker.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.duarte.dui.components.DuiAmountText
import com.duarte.dui.components.DuiAvatar
import com.duarte.dui.components.DuiBadge
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiEmptyState
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiImageCard
import com.duarte.dui.components.DuiListRow
import com.duarte.dui.components.DuiLoadingIndicator
import com.duarte.dui.components.DuiProgressBar
import com.duarte.dui.components.DuiScaffold
import com.duarte.dui.components.DuiStepper
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.components.DuiTopBar
import com.duarte.dui.theme.DuiTheme
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
import kotlinx.coroutines.delay
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
        setup == null && !loadError -> DuiScaffold {
            var showSpinner by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(250); showSpinner = true }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (showSpinner) DuiLoadingIndicator()
            }
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
    var restReady by remember(engine) { mutableStateOf(false) }
    var restTrigger by remember(engine) { mutableStateOf(0) }
    var finishedPayload by remember(engine) { mutableStateOf<FinishedWorkout?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf(false) }

    DisposableEffect(timer) { onDispose { timer.dispose() } }
    LaunchedEffect(state.restSuggestedSeconds, state.finished, restTrigger) {
        if (!state.finished && state.restSuggestedSeconds > 0) {
            restPaused = false
            if (state.restMode == RestMode.AUTO) {
                timer.start(state.restSuggestedSeconds)
                restReady = false
            } else {
                restReady = true
            }
            restVisible = true
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

    val image = rememberImageBitmap(currentExercise?.imagePath)

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

    val spacing = DuiTheme.spacing
    DuiScaffold(
        topBar = { DuiTopBar(onBack = onExit, backLabel = "Exit", trailing = "${state.completedCount} / ${state.total} done") },
    ) {
        Column(Modifier.fillMaxSize()) {
            DuiProgressBar(state.progress, Modifier.padding(horizontal = spacing.space4))

            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = spacing.space4, end = spacing.space4, top = spacing.space4, bottom = spacing.space4),
                verticalArrangement = Arrangement.spacedBy(spacing.space4),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            DuiText(
                                if (state.isSuperset) "Round ${state.roundNumber ?: 1}" else "Set ${state.setNumber ?: 1}",
                                style = DuiTextStyle.Title,
                            )
                            DuiText(setup.workout.name, style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary, maxLines = 1)
                        }
                        DuiBadge(if (state.isSuperset) "Superset" else "Regular")
                    }
                }

                if (image != null) {
                    item {
                        DuiImageCard(image, "${currentExercise?.name ?: setup.workout.name} exercise photo")
                    }
                }

                item {
                    Column {
                        DuiText(currentExercise?.name ?: "Exercise", style = DuiTextStyle.Title)
                        currentItem?.notes?.takeIf { it.isNotBlank() }?.let {
                            DuiText(it, style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
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

            Column(
                Modifier.fillMaxWidth().padding(start = spacing.space4, end = spacing.space4, bottom = spacing.space4, top = spacing.space2),
                verticalArrangement = Arrangement.spacedBy(spacing.space2),
            ) {
                DuiButton(
                    "Complete set",
                    onClick = {
                        engine.completeSet(actualReps)
                        cursor++
                        restTrigger++
                        actualReps = engine.state.value.targetReps ?: 0
                    },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                    DuiButton(
                        "Previous",
                        onClick = {
                            engine.previous()
                            cursor = (cursor - 1).coerceAtLeast(0)
                            actualReps = engine.state.value.targetReps ?: actualReps
                            timer.skip()
                            restVisible = false
                            restReady = false
                        },
                        modifier = Modifier.weight(1f),
                        style = DuiButtonStyle.Secondary,
                        enabled = cursor > 0,
                    )
                    DuiButton(
                        "Skip",
                        onClick = {
                            engine.skip()
                            cursor++
                            actualReps = engine.state.value.targetReps ?: 0
                            restVisible = false
                            restReady = false
                            timer.skip()
                            restTrigger++
                        },
                        modifier = Modifier.weight(1f),
                        style = DuiButtonStyle.Secondary,
                    )
                }
            }
        }
    }

    if (restVisible && (restReady || remaining > 0)) {
        RestOverlay(
            remaining = if (restReady) state.restSuggestedSeconds else remaining,
            ready = restReady,
            onStart = {
                timer.start(state.restSuggestedSeconds)
                restReady = false
                restPaused = false
            },
            paused = restPaused,
            onPauseResume = {
                if (restPaused) timer.resume() else timer.pause()
                restPaused = !restPaused
            },
            onAdjust = { timer.addSeconds(it) },
            onSkip = { timer.skip(); restVisible = false; restReady = false; restPaused = false },
        )
    }
}

@Composable
private fun SupersetPanel(members: List<SupersetMember>, exercises: List<Exercise>, currentIndex: Int, round: Int, rounds: Int) {
    val spacing = DuiTheme.spacing
    DuiCard {
        Row(Modifier.fillMaxWidth().padding(bottom = spacing.space2), verticalAlignment = Alignment.CenterVertically) {
            DuiText("The circuit", Modifier.weight(1f), style = DuiTextStyle.Label)
            DuiText("Round $round of $rounds", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
        }
        members.sortedBy { it.position }.forEachIndexed { index, member ->
            val active = index == currentIndex
            if (index > 0) DuiDivider()
            DuiListRow(
                title = exercises.firstOrNull { it.id == member.exerciseId }?.name ?: "Exercise",
                subtitle = if (active) "Now" else null,
                value = "${member.reps} reps",
                selected = active,
                leading = { DuiAvatar("${index + 1}") },
            )
        }
        DuiProgressBar(round.toFloat().div(rounds.coerceAtLeast(1)), Modifier.padding(top = spacing.space3))
    }
}

@Composable
private fun RepAdjuster(target: Int, actual: Int, onAdjust: (Int) -> Unit) {
    DuiCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                DuiText("Reps", style = DuiTextStyle.Label)
                DuiText("Target $target", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            }
            DuiStepper(
                value = "$actual",
                onDecrement = { onAdjust(-1) },
                onIncrement = { onAdjust(1) },
                decrementEnabled = actual > 0,
                decrementLabel = "Decrease reps",
                incrementLabel = "Increase reps",
            )
        }
    }
}

@Composable
private fun RestOverlay(
    remaining: Int,
    ready: Boolean,
    onStart: () -> Unit,
    paused: Boolean,
    onPauseResume: () -> Unit,
    onAdjust: (Int) -> Unit,
    onSkip: () -> Unit,
) {
    val spacing = DuiTheme.spacing
    val clock = "${(remaining / 60).toString().padStart(2, '0')}:${(remaining % 60).toString().padStart(2, '0')}"
    DuiDialog(
        onDismissRequest = { },
        title = "Take a breath",
        dismissible = false,
        confirmButton = {
            if (ready) DuiButton("Start rest", onClick = onStart) else DuiButton("Skip rest", onClick = onSkip)
        },
        dismissButton = if (ready) {
            { DuiButton("Skip rest", onClick = onSkip, style = DuiButtonStyle.Ghost) }
        } else null,
    ) {
        DuiAmountText(label = if (ready) "Ready" else if (paused) "Paused" else "Resting", amount = clock)
        if (!ready) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                DuiButton("−15 sec", onClick = { onAdjust(-15) }, modifier = Modifier.weight(1f), style = DuiButtonStyle.Secondary)
                DuiButton("+15 sec", onClick = { onAdjust(15) }, modifier = Modifier.weight(1f), style = DuiButtonStyle.Secondary)
            }
            DuiButton(if (paused) "Resume timer" else "Pause timer", onClick = onPauseResume, style = DuiButtonStyle.Secondary)
        }
    }
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
    val spacing = DuiTheme.spacing
    DuiScaffold {
        Column(
            Modifier.fillMaxSize().padding(horizontal = spacing.space4, vertical = spacing.space5),
            verticalArrangement = Arrangement.Center,
        ) {
            DuiText("Session complete", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            DuiText("That’s a wrap.", style = DuiTextStyle.Title)
            DuiText(workoutName, style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
            DuiCard(Modifier.padding(top = spacing.space5)) {
                DuiAmountText(label = "Sets completed", amount = "$completed / $total")
                DuiText(
                    if (completed == total) "Every set logged. Nice work." else "Skipped sets are saved as incomplete.",
                    Modifier.padding(top = spacing.space2),
                    style = DuiTextStyle.Body,
                    color = DuiTheme.colors.textSecondary,
                )
            }
            if (saveError) DuiText("Couldn’t save this session. Please try again.", Modifier.padding(top = spacing.space4), style = DuiTextStyle.Body, color = DuiTheme.colors.negative)
            Column(Modifier.padding(top = spacing.space5), verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                DuiButton(if (saving) "Saving…" else "Save session", onClick = onSave, enabled = !saving)
                DuiButton("Leave without saving", onClick = onExit, style = DuiButtonStyle.Ghost)
            }
        }
    }
}

@Composable
private fun LoadFailure(onExit: () -> Unit) {
    DuiScaffold {
        Column(Modifier.fillMaxSize().padding(DuiTheme.spacing.space4), verticalArrangement = Arrangement.Center) {
            DuiEmptyState(
                title = "Workout unavailable",
                body = "This workout couldn’t be loaded. It may have been removed.",
                actionText = "Back to workouts",
                onAction = onExit,
            )
        }
    }
}
