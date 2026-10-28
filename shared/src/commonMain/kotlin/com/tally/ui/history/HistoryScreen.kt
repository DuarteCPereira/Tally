package com.tally.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.duarte.dui.components.DuiAvatar
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiEmptyState
import com.duarte.dui.components.DuiIcon
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiListRow
import com.duarte.dui.components.DuiStatTile
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.tally.AppContainer
import com.tally.domain.HistoryStats
import com.tally.domain.Session
import com.tally.domain.SessionEntry
import com.tally.domain.WorkoutItemType
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
    val loadedSessions by container.history.sessions.collectAsState(initial = null)
    val sessions = loadedSessions ?: emptyList()
    val stats by container.history.stats.collectAsState(initial = HistoryStats(0, 0, 0, 0))
    val entries by sessionEntries.collectAsState(initial = emptyList())
    var expandedSessionId by remember { mutableStateOf<String?>(null) }
    val spacing = DuiTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = spacing.space4, top = spacing.space4, end = spacing.space4, bottom = spacing.space6),
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        item {
            Column {
                DuiText("Your progress", style = DuiTextStyle.Title)
                DuiText("Every session adds up.", style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
            }
        }
        item { StatsGrid(stats) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = spacing.space3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DuiText("Recent sessions", style = DuiTextStyle.Title)
                DuiText("${sessions.size} total", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            }
        }
        if (loadedSessions == null) {
            // Still loading: no empty state yet.
        } else if (sessions.isEmpty()) {
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
    val gap = DuiTheme.spacing.space2
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(gap)) {
            DuiStatTile("Sessions", stats.totalSessions.toString(), Modifier.weight(1f).fillMaxHeight(), DuiIcons.History, emphasis = true)
            DuiStatTile("Total time", formatDuration(stats.totalTimeSeconds), Modifier.weight(1f).fillMaxHeight(), DuiIcons.TrendUp)
        }
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(gap)) {
            DuiStatTile("Total sets", stats.totalSets.toString(), Modifier.weight(1f).fillMaxHeight(), DuiIcons.Sets)
            DuiStatTile("This week", stats.thisWeekCount.toString(), Modifier.weight(1f).fillMaxHeight(), DuiIcons.Spark)
        }
    }
}

@Composable
private fun SessionCard(session: Session, entries: List<SessionEntry>, expanded: Boolean, onClick: () -> Unit) {
    val spacing = DuiTheme.spacing
    DuiCard(onClick = onClick) {
        DuiListRow(
            title = session.workoutName,
            subtitle = formatDate(session),
            value = formatDuration(session.durationSeconds.toLong()),
            leading = { DuiAvatar(DuiIcons.TrendUp) },
        )
        if (expanded) {
            DuiDivider(Modifier.padding(vertical = spacing.space3))
            SessionDetail(entries)
        } else if (entries.isNotEmpty()) {
            DuiText(
                "${entries.map { it.exerciseName }.distinct().size} exercises  ·  ${entries.count { it.completed }} sets",
                style = DuiTextStyle.Caption,
                color = DuiTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SessionDetail(entries: List<SessionEntry>) {
    if (entries.isEmpty()) {
        DuiText("Exercise details aren’t available for this session.", style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)
        return
    }
    val exercises = entries.groupBy { it.exerciseName }
    Column(verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space3)) {
        exercises.forEach { (name, exerciseEntries) ->
            Column(verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space1)) {
                DuiText(name, style = DuiTextStyle.Label)
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
    val colors = DuiTheme.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space2)) {
        DuiIcon(
            if (done) DuiIcons.Check else DuiIcons.Minus,
            contentDescription = if (done) "Completed" else "Skipped",
            tint = if (done) colors.accent else colors.textDisabled,
            size = DuiTheme.spacing.space4,
        )
        DuiText(leading, Modifier.weight(1f), style = DuiTextStyle.Body, color = colors.textSecondary)
        DuiText(trailing, style = DuiTextStyle.Label)
    }
}

@Composable
private fun EmptyHistory() {
    DuiEmptyState(
        title = "A clean slate",
        body = "Finish a workout and it’ll show up here.",
        icon = DuiIcons.History,
    )
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
