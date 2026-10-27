package com.duarte.workouttracker.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.duarte.workouttracker.AppContainer
import com.duarte.workouttracker.domain.HistoryStats
import com.duarte.workouttracker.domain.Session
import com.duarte.workouttracker.domain.SessionEntry
import com.duarte.workouttracker.domain.WorkoutItemType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** History view. Supply sessionEntries when the host has a session-entry stream available. */
@Composable
fun HistoryScreen(
    container: AppContainer,
    sessionEntries: Flow<List<SessionEntry>> = flowOf(emptyList()),
) {
    val sessions by container.history.sessions.collectAsState(initial = emptyList())
    val stats by container.history.stats.collectAsState(initial = HistoryStats(0, 0, 0, 0))
    val entries by sessionEntries.collectAsState(initial = emptyList())
    var expandedSessionId by remember { mutableStateOf<String?>(null) }
    val colors = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Your progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Every session adds up.", style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
        }
        item { StatsGrid(stats) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Recent sessions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("${sessions.size} total", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            }
        }
        if (sessions.isEmpty()) {
            item { EmptyHistory() }
        } else {
            items(sessions.sortedByDescending { it.startedAt }, key = { it.id }) { session ->
                val isExpanded = expandedSessionId == session.id
                SessionCard(
                    session = session,
                    entries = entries.filter { it.sessionId == session.id }.sortedBy { it.position },
                    expanded = isExpanded,
                    onClick = { expandedSessionId = if (isExpanded) null else session.id },
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: HistoryStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("SESSIONS", stats.totalSessions.toString(), "◷", Modifier.weight(1f), true)
            StatCard("TOTAL TIME", formatDuration(stats.totalTimeSeconds), "↗", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("TOTAL SETS", stats.totalSets.toString(), "▤", Modifier.weight(1f))
            StatCard("THIS WEEK", stats.thisWeekCount.toString(), "✦", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, mark: String, modifier: Modifier, featured: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    val container = if (featured) scheme.primaryContainer else scheme.surfaceContainerHigh
    val content = if (featured) scheme.onPrimaryContainer else scheme.onSurface
    Card(modifier = modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = container)) {
        Column(
            modifier = Modifier.fillMaxWidth().height(116.dp).padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = content.copy(alpha = .72f), fontWeight = FontWeight.SemiBold)
                Text(mark, style = MaterialTheme.typography.titleMedium, color = content)
            }
            Text(value, style = MaterialTheme.typography.headlineMedium, color = content, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SessionCard(session: Session, entries: List<SessionEntry>, expanded: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
    ) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Box(
                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(scheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("↗", style = MaterialTheme.typography.titleLarge, color = scheme.onSecondaryContainer)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(session.workoutName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(formatDate(session), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
                Text(formatDuration(session.durationSeconds.toLong()), style = MaterialTheme.typography.labelLarge, color = scheme.primary, fontWeight = FontWeight.SemiBold)
            }
            if (expanded) {
                HorizontalDivider(color = scheme.outlineVariant.copy(alpha = .65f))
                SessionDetail(entries)
            } else if (entries.isNotEmpty()) {
                Text(
                    "${entries.map { it.exerciseName }.distinct().size} exercises  ·  ${entries.count { it.completed }} sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SessionDetail(entries: List<SessionEntry>) {
    if (entries.isEmpty()) {
        Text("Exercise details aren’t available for this session.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    val exercises = entries.groupBy { it.exerciseName }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        exercises.forEach { (name, exerciseEntries) ->
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (exerciseEntries.any { it.itemType == WorkoutItemType.SUPERSET }) {
                    exerciseEntries.forEach { entry ->
                        DetailLine(
                            leading = "Round ${entry.roundNumber ?: entry.setNumber ?: "—"}",
                            trailing = actualReps(entry),
                            done = entry.completed,
                        )
                    }
                } else {
                    exerciseEntries.sortedBy { it.setNumber ?: Int.MAX_VALUE }.forEach { entry ->
                        DetailLine(
                            leading = "Set ${entry.setNumber ?: "—"}",
                            trailing = actualReps(entry),
                            done = entry.completed,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLine(leading: String, trailing: String, done: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(if (done) scheme.primary else scheme.outlineVariant))
        Text(leading, Modifier.weight(1f).padding(start = 9.dp), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        Text(trailing, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EmptyHistory() {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(22.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("◷", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Text("A clean slate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Finish a workout and it’ll show up here.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun actualReps(entry: SessionEntry): String = when {
    !entry.completed -> "Skipped"
    entry.actualReps != null -> "${entry.actualReps} reps"
    else -> "${entry.targetReps} reps"
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds.coerceAtLeast(0) / 60
    val hours = minutes / 60
    return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
}

private fun formatDate(session: Session): String {
    val date = session.startedAt.toLocalDateTime(TimeZone.UTC)
    val month = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[date.monthNumber - 1]
    val hour = (date.hour % 12).let { if (it == 0) 12 else it }
    val minute = date.minute.toString().padStart(2, '0')
    val meridiem = if (date.hour < 12) "AM" else "PM"
    return "$month ${date.dayOfMonth}, ${date.year}  ·  $hour:$minute $meridiem"
}
