package com.tally.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.tally.ui.common.DelayedLoadingIndicator

@Composable
fun HistoryScreen(viewModel: HistoryViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        HistoryUiState.Loading -> DelayedLoadingIndicator(modifier)
        is HistoryUiState.Content -> HistoryContent(current, modifier)
    }
}

@Composable
private fun HistoryContent(state: HistoryUiState.Content, modifier: Modifier = Modifier) {
    var expandedSessionId by remember { mutableStateOf<String?>(null) }
    val spacing = DuiTheme.spacing

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.space4,
            top = spacing.space4,
            end = spacing.space4,
            bottom = spacing.space6,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.space3),
    ) {
        item {
            Column {
                DuiText("Your progress", style = DuiTextStyle.Title)
                DuiText(
                    "Every session adds up.",
                    style = DuiTextStyle.Body,
                    color = DuiTheme.colors.textSecondary,
                )
            }
        }
        item { StatsGrid(state.stats) }
        item { SessionsHeader(count = state.sessions.size) }
        if (state.sessions.isEmpty()) {
            item { EmptyHistory() }
        }
        items(state.sessions, key = { it.id }) { session ->
            val expanded = expandedSessionId == session.id
            SessionCard(
                session = session,
                expanded = expanded,
                onClick = { expandedSessionId = session.id.takeUnless { expanded } },
            )
        }
    }
}

@Composable
private fun StatsGrid(stats: StatsUi) {
    val gap = DuiTheme.spacing.space2
    val tile = Modifier.fillMaxHeight()
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(gap)) {
            DuiStatTile("Sessions", stats.sessions, tile.weight(1f), DuiIcons.History, emphasis = true)
            DuiStatTile("Total time", stats.totalTime, tile.weight(1f), DuiIcons.TrendUp)
        }
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(gap)) {
            DuiStatTile("Total sets", stats.totalSets, tile.weight(1f), DuiIcons.Sets)
            DuiStatTile("This week", stats.thisWeek, tile.weight(1f), DuiIcons.Spark)
        }
    }
}

@Composable
private fun SessionsHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DuiTheme.spacing.space3),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DuiText("Recent sessions", style = DuiTextStyle.Title)
        DuiText("$count total", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
    }
}

@Composable
private fun SessionCard(session: SessionUi, expanded: Boolean, onClick: () -> Unit) {
    DuiCard(onClick = onClick) {
        DuiListRow(
            title = session.title,
            subtitle = session.date,
            value = session.duration,
            leading = { DuiAvatar(DuiIcons.TrendUp) },
        )
        if (expanded) {
            DuiDivider(Modifier.padding(vertical = DuiTheme.spacing.space3))
            SessionDetail(session.exercises)
        } else {
            session.summary?.let {
                DuiText(it, style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
            }
        }
    }
}

@Composable
private fun SessionDetail(exercises: List<ExerciseResultUi>) {
    val spacing = DuiTheme.spacing
    if (exercises.isEmpty()) {
        DuiText(
            "Exercise details aren’t available for this session.",
            style = DuiTextStyle.Body,
            color = DuiTheme.colors.textSecondary,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) {
        exercises.forEach { exercise ->
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space1)) {
                DuiText(exercise.name, style = DuiTextStyle.Label)
                exercise.lines.forEach { ResultLine(it) }
            }
        }
    }
}

@Composable
private fun ResultLine(line: ResultLineUi) {
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space2),
    ) {
        DuiIcon(
            if (line.done) DuiIcons.Check else DuiIcons.Minus,
            contentDescription = if (line.done) "Completed" else "Skipped",
            tint = if (line.done) colors.accent else colors.textDisabled,
            size = spacing.space4,
        )
        DuiText(line.label, Modifier.weight(1f), style = DuiTextStyle.Body, color = colors.textSecondary)
        DuiText(line.result, style = DuiTextStyle.Label)
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

@Preview
@Composable
private fun HistoryContentPreview() {
    val session = SessionUi(
        id = "s",
        title = "Upper body A",
        date = "Mar 4, 2026  ·  6:05 PM",
        duration = "48m",
        summary = "2 exercises  ·  5 sets",
        exercises = listOf(
            ExerciseResultUi(
                name = "Bench press",
                lines = listOf(
                    ResultLineUi(label = "Set 1", result = "10 reps", done = true),
                    ResultLineUi(label = "Set 2", result = "Skipped", done = false),
                ),
            ),
        ),
    )
    DuiTheme(darkTheme = false) {
        HistoryContent(
            HistoryUiState.Content(
                stats = StatsUi(sessions = "12", totalTime = "9h 40m", totalSets = "184", thisWeek = "3"),
                sessions = listOf(session, session.copy(id = "t")),
            ),
        )
    }
}
