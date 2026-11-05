package com.tally.site.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.duarte.dui.components.DuiBadge
import com.duarte.dui.components.DuiLineChart
import com.duarte.dui.components.DuiRangeChips
import com.duarte.dui.components.DuiSiteSection
import com.duarte.dui.components.DuiStatTile
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.components.DuiTrend
import com.duarte.dui.theme.DuiTheme
import com.tally.site.SampleSessions
import com.tally.site.StatsPreview
import com.tally.site.StatsRange
import com.tally.site.StatsSummary
import com.tally.site.StatsTeaser
import com.tally.site.Trend

/**
 * The statistics teaser: copy on the left, a live preview card on the right. The range chips are
 * real state; the numbers are recomputed by [StatsPreview] from the sample sessions.
 */
@Composable
internal fun StatsSection(teaser: StatsTeaser, modifier: Modifier = Modifier) {
    DuiSiteSection(modifier, grid = true, divider = true) {
        ResponsiveGrid(
            items = listOf(true, false),
            minCellWidth = TwoColumnMinWidth,
            gap = DuiTheme.spacing.space8,
            verticalAlignment = Alignment.CenterVertically,
            equalHeights = false,
        ) { isCopy ->
            if (isCopy) TeaserCopy(teaser) else StatsCard(teaser.previewNote)
        }
    }
}

@Composable
private fun TeaserCopy(teaser: StatsTeaser) {
    SectionHeading(
        eyebrow = teaser.eyebrow,
        title = teaser.title,
        body = teaser.body,
        modifier = Modifier
            .background(DuiTheme.colors.bg, DuiTheme.shapes.lg)
            .padding(DuiTheme.spacing.space6),
        trailing = { DuiBadge(teaser.badge) },
    )
}

@Composable
private fun StatsCard(previewNote: String) {
    var range by rememberSaveable { mutableStateOf(StatsRange.Month) }
    val summary = remember(range) { StatsPreview.summarize(SampleSessions.all, range) }
    val colors = DuiTheme.colors
    val spacing = DuiTheme.spacing
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, colors.divider, DuiTheme.shapes.lg)
            .background(colors.bg, DuiTheme.shapes.lg)
            .padding(spacing.space5),
        verticalArrangement = Arrangement.spacedBy(spacing.space4),
    ) {
        StatsHeadline(summary, previewNote)
        DuiLineChart(
            points = summary.points,
            trend = summary.trend.toDui(),
            contentDescription = summary.chartDescription,
            height = ChartHeight,
        )
        DuiRangeChips(
            options = StatsRange.entries.map { it.label },
            selectedIndex = range.ordinal,
            onSelect = { range = StatsRange.entries[it] },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
            summary.tiles.forEach { tile ->
                DuiStatTile(label = tile.label, value = tile.value, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatsHeadline(summary: StatsSummary, previewNote: String) {
    val colors = DuiTheme.colors
    // Wraps the note under the numbers on phones instead of squeezing the volume.
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space2),
    ) {
        Column {
            DuiText(summary.title, style = DuiTextStyle.Caption, color = colors.textSecondary)
            DuiText(summary.volume, style = DuiTextStyle.Display, maxLines = 1)
            DuiText(
                summary.change,
                style = DuiTextStyle.Label,
                color = if (summary.trend == Trend.Up) colors.positive else colors.negative,
            )
        }
        DuiText(previewNote, style = DuiTextStyle.Eyebrow, color = colors.textSecondary)
    }
}

private fun Trend.toDui(): DuiTrend = when (this) {
    Trend.Up -> DuiTrend.Up
    Trend.Down -> DuiTrend.Down
}

private val ChartHeight = 120.dp
