package com.duarte.dui.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.duarte.dui.components.DuiAmountText
import com.duarte.dui.components.DuiBadge
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiCardTone
import com.duarte.dui.components.DuiDialog
import com.duarte.dui.components.DuiDivider
import com.duarte.dui.components.DuiEmptyState
import com.duarte.dui.components.DuiIconButton
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiImageCard
import com.duarte.dui.components.DuiLoadingIndicator
import com.duarte.dui.components.DuiMenu
import com.duarte.dui.components.DuiMenuItem
import com.duarte.dui.components.DuiProgressBar
import com.duarte.dui.components.DuiSegmentedControl
import com.duarte.dui.components.DuiStatTile
import com.duarte.dui.components.DuiStepper
import com.duarte.dui.components.DuiSwitchRow
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextField
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.components.DuiTopBar
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.mutableStateOf
import com.duarte.dui.components.DuiAvatar
import com.duarte.dui.components.DuiBottomBar
import com.duarte.dui.components.DuiButton
import com.duarte.dui.components.DuiButtonStyle
import com.duarte.dui.components.DuiLineChart
import com.duarte.dui.components.DuiListRow
import com.duarte.dui.components.DuiRangeChips
import com.duarte.dui.components.DuiSectionHeader
import com.duarte.dui.components.DuiTab
import com.duarte.dui.components.DuiTrend
import com.duarte.dui.theme.DuiTheme

/** Every DUI component with the mockups' sample content, in the current theme. */
@Composable
fun ComponentGallery(themeName: String) {
    val spacing = DuiTheme.spacing
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = spacing.space4)) {
            BasicText("$themeName components", style = DuiTheme.typography.title.copy(color = DuiTheme.colors.textPrimary))

            GalleryLabel("AmountText")
            DuiAmountText(label = "Portfolio", amount = "€12,480.32", change = "+€248.10 (2.03%)", trend = DuiTrend.Up)

            GalleryLabel("PrimaryButton")
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space2)) {
                DuiButton("Invest", onClick = {})
                DuiButton("Continue", onClick = {}, style = DuiButtonStyle.Accent)
                DuiButton("Add to watchlist", onClick = {}, style = DuiButtonStyle.Secondary)
                DuiButton("Learn more", onClick = {}, style = DuiButtonStyle.Ghost)
                DuiButton("Unavailable", onClick = {}, enabled = false)
            }

            GalleryLabel("SectionHeader + ListRow")
            DuiSectionHeader("Your portfolio", actionText = "See all", onAction = {})
            Column(Modifier.background(DuiTheme.colors.surface, DuiTheme.shapes.lg).padding(horizontal = spacing.space4)) {
                DuiListRow("Apple", subtitle = "3 shares", value = "€612.40", change = "+1.2%", trend = DuiTrend.Up, onClick = {}, leading = { DuiAvatar("A") })
                DuiListRow("MSCI World", subtitle = "Savings plan", value = "€3,204.90", change = "-0.4%", trend = DuiTrend.Down, divider = true, onClick = {}, leading = { DuiAvatar("M") })
                DuiListRow("Bitcoin", subtitle = "Selected", value = "€1,020.00", change = "+3.8%", selected = true, divider = true, onClick = {}, leading = { DuiAvatar("B") })
            }

            GalleryLabel("LineChart + RangeChips")
            var range by remember { mutableIntStateOf(1) }
            DuiLineChart(RisingPoints, DuiTrend.Up, contentDescription = "Value rising over the period")
            DuiRangeChips(listOf("1D", "1W", "1M", "1Y"), selectedIndex = range, onSelect = { range = it })
            DuiLineChart(RisingPoints.reversed(), DuiTrend.Down, contentDescription = "Value falling over the period")

            GalleryLabel("DuiText")
            DuiText("Display 40", style = DuiTextStyle.Display)
            DuiText("Title 22", style = DuiTextStyle.Title)
            DuiText("Label 15", style = DuiTextStyle.Label)
            DuiText("Body 16", style = DuiTextStyle.Body)
            DuiText("Caption 13", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)

            GalleryLabel("DuiIcons + DuiIconButton")
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.space1)) {
                listOf(
                    DuiIcons.Home, DuiIcons.History, DuiIcons.Settings, DuiIcons.Sound, DuiIcons.Vibration, DuiIcons.Back,
                    DuiIcons.Plus, DuiIcons.Minus, DuiIcons.Check, DuiIcons.Close, DuiIcons.More, DuiIcons.ArrowUp,
                    DuiIcons.ArrowDown, DuiIcons.TrendUp, DuiIcons.Sets, DuiIcons.Spark,
                ).take(8).forEach { com.duarte.dui.components.DuiIcon(it, null) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.space1)) {
                listOf(
                    DuiIcons.Check, DuiIcons.Close, DuiIcons.More, DuiIcons.ArrowUp,
                    DuiIcons.ArrowDown, DuiIcons.TrendUp, DuiIcons.Sets, DuiIcons.Spark,
                ).forEach { com.duarte.dui.components.DuiIcon(it, null) }
            }
            Row { DuiIconButton(DuiIcons.ArrowUp, "Move up", {}, filled = true); DuiIconButton(DuiIcons.ArrowDown, "Move down", {}, enabled = false) }

            GalleryLabel("DuiTopBar")
            DuiTopBar(onBack = {}, actionText = "Save")
            DuiTopBar(onBack = {}, backLabel = "Exit", trailing = "3 / 12 done")

            GalleryLabel("DuiCard + DuiBadge + DuiDivider")
            DuiCard {
                DuiText("Surface card", style = DuiTextStyle.Label)
                DuiText("Flat, no shadow", style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
                DuiDivider(Modifier.padding(vertical = spacing.space3))
                DuiBadge("Superset")
            }
            Spacer(Modifier.height(spacing.space2))
            DuiCard(tone = DuiCardTone.Accent, onClick = {}) { DuiText("Accent card", style = DuiTextStyle.Label, color = DuiTheme.colors.accent) }

            GalleryLabel("DuiStatTile")
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                DuiStatTile("Sessions", "24", Modifier.weight(1f), icon = DuiIcons.History, emphasis = true)
                DuiStatTile("Total time", "18h 40m", Modifier.weight(1f), icon = DuiIcons.TrendUp)
            }

            GalleryLabel("DuiTextField")
            var name by remember { mutableStateOf("") }
            DuiTextField(name, { name = it }, label = "Workout name", placeholder = "e.g. Upper body A")
            Spacer(Modifier.height(spacing.space2))
            DuiTextField("", {}, label = "Exercise name", error = "Enter an exercise name.")

            GalleryLabel("DuiSegmentedControl + DuiSwitchRow")
            var seg by remember { mutableIntStateOf(0) }
            DuiSegmentedControl(listOf("System", "Light", "Dark"), seg, { seg = it })
            var sound by remember { mutableStateOf(true) }
            var vib by remember { mutableStateOf(false) }
            DuiSwitchRow("Sound", sound, { sound = it }, description = "Play a cue when your timer ends", leading = DuiIcons.Sound)
            DuiSwitchRow("Vibration", vib, { vib = it }, description = "Feel a cue when your timer ends", leading = DuiIcons.Vibration)

            GalleryLabel("DuiStepper + DuiProgressBar + DuiLoadingIndicator")
            var reps by remember { mutableIntStateOf(10) }
            DuiStepper(reps.toString(), { reps = (reps - 1).coerceAtLeast(0) }, { reps++ }, decrementEnabled = reps > 0)
            Spacer(Modifier.height(spacing.space3))
            DuiProgressBar(0.4f)
            Spacer(Modifier.height(spacing.space3))
            DuiLoadingIndicator()

            GalleryLabel("DuiEmptyState + DuiImageCard")
            DuiEmptyState("Start with a simple plan", "Create your first workout and shape it around the way you like to train.", icon = DuiIcons.Spark, actionText = "Create workout")
            Spacer(Modifier.height(spacing.space2))
            DuiImageCard(null, "Exercise photo", placeholderIcon = DuiIcons.Spark)

            GalleryLabel("DuiDialog + DuiMenu")
            var dialog by remember { mutableStateOf(false) }
            var menu by remember { mutableStateOf(false) }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.space2)) {
                DuiButton("Open dialog", { dialog = true }, Modifier.weight(1f), style = DuiButtonStyle.Secondary)
                DuiMenu(menu, { menu = false }, listOf(DuiMenuItem("Edit", {}), DuiMenuItem("Make a copy", {}), DuiMenuItem("Delete", {}))) {
                    DuiIconButton(DuiIcons.More, "More options", { menu = true }, filled = true)
                }
            }
            if (dialog) DuiDialog(
                onDismissRequest = { dialog = false },
                title = "Delete workout?",
                confirmButton = { DuiButton("Delete workout", { dialog = false }) },
                dismissButton = { DuiButton("Keep workout", { dialog = false }, style = DuiButtonStyle.Ghost) },
            ) { DuiText("It will be removed from your list. This can't be undone.", color = DuiTheme.colors.textSecondary) }

            GalleryLabel("DuiBottomBar")
        }
        var tab by remember { mutableIntStateOf(0) }
        DuiBottomBar(SampleTabs, selectedIndex = tab, onSelect = { tab = it })
    }
}

@Composable
private fun GalleryLabel(text: String) {
    BasicText(
        text,
        Modifier.padding(top = DuiTheme.spacing.space5, bottom = DuiTheme.spacing.space2),
        style = DuiTheme.typography.caption.copy(color = DuiTheme.colors.textSecondary),
    )
}

// Same shape as the LineChart mockup's polyline.
private val RisingPoints = listOf(10f, 16f, 6f, 28f, 22f, 40f, 34f, 50f, 44f, 64f, 58f, 74f)

private val SampleTabs = listOf(
    DuiTab("Home", DuiIcons.Home),
    DuiTab("History", DuiIcons.History),
    DuiTab("Settings", DuiIcons.Settings),
)
