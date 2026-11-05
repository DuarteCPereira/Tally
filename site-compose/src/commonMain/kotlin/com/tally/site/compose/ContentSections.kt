package com.tally.site.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiEyebrow
import com.duarte.dui.components.DuiFeatureCard
import com.duarte.dui.components.DuiSiteSection
import com.duarte.dui.components.DuiSiteSectionTone
import com.duarte.dui.components.DuiTallyMark
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.tally.site.Download
import com.tally.site.Eyebrow
import com.tally.site.Fact
import com.tally.site.Features
import com.tally.site.LinkTarget
import com.tally.site.Principle
import com.tally.site.Step
import com.tally.site.Steps

/** "One screen. One action." next to a card of facts. */
@Composable
internal fun PrincipleSection(principle: Principle, modifier: Modifier = Modifier) {
    DuiSiteSection(modifier, tone = DuiSiteSectionTone.Surface, divider = true) {
        ResponsiveGrid(
            items = listOf(true, false),
            minCellWidth = TwoColumnMinWidth,
            gap = DuiTheme.spacing.space8,
            verticalAlignment = Alignment.CenterVertically,
            equalHeights = false,
        ) { isCopy ->
            if (isCopy) {
                SectionHeading(principle.eyebrow, principle.title, principle.body)
            } else {
                FactsCard(principle.facts)
            }
        }
    }
}

@Composable
private fun FactsCard(facts: List<Fact>) {
    val colors = DuiTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.bg, DuiTheme.shapes.lg)
            .padding(horizontal = DuiTheme.spacing.space5, vertical = DuiTheme.spacing.space2),
    ) {
        facts.forEachIndexed { index, fact ->
            if (index > 0) DuiDivider()
            Row(
                Modifier.padding(vertical = DuiTheme.spacing.space4),
                horizontalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space4),
            ) {
                DuiText(fact.term, Modifier.weight(1f), style = DuiTextStyle.Counter, color = colors.textSecondary)
                DuiText(
                    fact.value,
                    style = DuiTextStyle.Counter,
                    color = if (fact.accent) colors.accent else colors.textPrimary,
                )
            }
        }
    }
}

/** Build, run, review: three cards, each with a real screen at the bottom. */
@Composable
internal fun StepsSection(steps: Steps, modifier: Modifier = Modifier) {
    DuiSiteSection(modifier) {
        SectionHeading(steps.eyebrow, steps.title, modifier = Modifier.widthIn(max = HeadingMaxWidth))
        ResponsiveGrid(items = steps.items, minCellWidth = StepMinWidth, gap = DuiTheme.spacing.space5) { step ->
            StepCard(step)
        }
    }
}

@Composable
private fun StepCard(step: Step) {
    val spacing = DuiTheme.spacing
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(DuiTheme.colors.surface, DuiTheme.shapes.lg)
            .padding(spacing.space5),
        verticalArrangement = Arrangement.spacedBy(spacing.space5),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
            DuiEyebrow(step.eyebrow.text, index = step.eyebrow.index)
            DuiText(step.title, style = DuiTextStyle.Title)
            DuiText(step.body, color = DuiTheme.colors.textSecondary)
        }
        // Pushes the phone to the bottom so the three phones line up across the row.
        Spacer(Modifier.weight(1f))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AppScreenFrame(step.screen, scale = StepScale)
        }
    }
}

/** The six feature cards. */
@Composable
internal fun FeaturesSection(features: Features, modifier: Modifier = Modifier) {
    DuiSiteSection(modifier, divider = true) {
        SectionHeading(features.eyebrow, features.title, modifier = Modifier.widthIn(max = HeadingMaxWidth))
        ResponsiveGrid(features.items, minCellWidth = FeatureMinWidth, gap = DuiTheme.spacing.space4) { feature ->
            DuiFeatureCard(
                title = feature.title,
                body = feature.body,
                icon = feature.icon.vector,
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                index = feature.index,
            )
        }
    }
}

/** The closing call to action, centred. */
@Composable
internal fun DownloadSection(download: Download, onLink: (LinkTarget) -> Unit, modifier: Modifier = Modifier) {
    DuiSiteSection(modifier, tone = DuiSiteSectionTone.Surface) {
        Column(
            Modifier
                .fillMaxWidth()
                .widthIn(max = DownloadMaxWidth)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space5),
        ) {
            DuiTallyMark(size = 72.dp, animate = !LocalReduceMotion.current)
            DuiText(download.title, style = DuiTextStyle.Display, textAlign = TextAlign.Center)
            DuiText(download.body, color = DuiTheme.colors.textSecondary, textAlign = TextAlign.Center)
            StoreButtons(
                stores = download.stores,
                onLink = onLink,
                horizontalArrangement = Arrangement.spacedBy(
                    space = DuiTheme.spacing.space2,
                    alignment = Alignment.CenterHorizontally,
                ),
            )
        }
    }
}

/** Eyebrow, title and an optional body: how every section opens. */
@Composable
internal fun SectionHeading(
    eyebrow: Eyebrow,
    title: String,
    body: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space4)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DuiTheme.spacing.space3),
        ) {
            DuiEyebrow(eyebrow.text, index = eyebrow.index)
            trailing?.invoke()
        }
        DuiText(title, style = DuiTextStyle.Display)
        body?.let { DuiText(it, Modifier.widthIn(max = BodyMaxWidth), color = DuiTheme.colors.textSecondary) }
    }
}

internal val TwoColumnMinWidth = 440.dp
private val HeadingMaxWidth = 640.dp
private val BodyMaxWidth = 480.dp
private val StepMinWidth = 320.dp
private val FeatureMinWidth = 340.dp
private val DownloadMaxWidth = 720.dp
private const val StepScale = 0.7f
