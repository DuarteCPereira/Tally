package com.duarte.workouttracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duarte.workouttracker.AppContainer
import com.duarte.workouttracker.domain.Workout
import kotlinx.coroutines.launch
import com.duarte.workouttracker.ui.editor.duplicateWorkoutWithItems

@Composable
fun HomeScreen(container: AppContainer, onCreate: () -> Unit, onEdit: (String) -> Unit, onStart: (String) -> Unit) {
    val workouts by container.workouts.workouts.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var deleteTarget by remember { mutableStateOf<Workout?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("YOUR TRAINING", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Workouts", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            FilledTonalButton(onClick = onCreate, shape = RoundedCornerShape(18.dp)) { Text("＋  New") }
        }
        Spacer(Modifier.height(10.dp))
        Text("Build a routine, then show up for it.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        if (workouts.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .42f)).padding(28.dp),
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start,
            ) {
                Text("✳", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(18.dp))
                Text("Start with a simple plan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text("Create your first workout and shape it around the way you like to train.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(22.dp))
                Button(onClick = onCreate, shape = RoundedCornerShape(16.dp)) { Text("Create your first workout") }
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(workouts.sortedBy { it.position }, key = { it.id }) { workout ->
                    WorkoutCard(workout, onEdit = { onEdit(workout.id) }, onStart = { onStart(workout.id) }, onDuplicate = { scope.launch { duplicateWorkoutWithItems(container, workout.id) } }, onDelete = { deleteTarget = workout })
                }
            }
        }
    }
    deleteTarget?.let { workout ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete workout?") },
            text = { Text("“${workout.name}” will be removed from your list. This can’t be undone.") },
            confirmButton = { TextButton(onClick = { scope.launch { container.workouts.delete(workout.id); deleteTarget = null } }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Keep workout") } },
        )
    }
}

@Composable
private fun WorkoutCard(workout: Workout, onEdit: () -> Unit, onStart: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(workout.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("WORKOUT  ·  READY WHEN YOU ARE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("•••", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { expanded = false; onEdit() })
                        DropdownMenuItem(text = { Text("Make a copy") }, onClick = { expanded = false; onDuplicate() })
                        DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { expanded = false; onDelete() })
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onEdit) { Text("Edit plan") }
                Spacer(Modifier.weight(1f))
                Button(onClick = onStart, shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 11.dp)) { Text("Start  →") }
            }
        }
    }
}
