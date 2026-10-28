package com.tally.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiSectionHeader
import com.duarte.dui.components.DuiSegmentedControl
import com.duarte.dui.components.DuiSwitchRow
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.tally.domain.RestMode
import com.tally.domain.ThemeMode
import com.tally.domain.WorkoutSettings
import com.tally.ui.common.DelayedLoadingIndicator

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        SettingsUiState.Loading -> DelayedLoadingIndicator(modifier)
        is SettingsUiState.Content -> SettingsContent(current.settings, viewModel::update, modifier)
    }
}

@Composable
private fun SettingsContent(
    settings: WorkoutSettings,
    onChange: (WorkoutSettings.() -> WorkoutSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = DuiTheme.spacing
    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = spacing.space4, vertical = spacing.space4),
    ) {
        DuiText("Make it yours", style = DuiTextStyle.Title)
        DuiText(
            "Tune your workout experience.",
            style = DuiTextStyle.Body,
            color = DuiTheme.colors.textSecondary,
        )

        SettingsGroup(title = "Rest timer", subtitle = "Choose how rest intervals work") {
            DuiSegmentedControl(
                options = RestMode.entries.map { it.label },
                selectedIndex = settings.restMode.ordinal,
                onSelect = { index -> onChange { copy(restMode = RestMode.entries[index]) } },
            )
        }

        SettingsGroup(title = "Feedback", subtitle = "Stay in the rhythm, your way") {
            DuiSwitchRow(
                title = "Sound",
                description = "Play a cue when your timer ends",
                checked = settings.sound,
                onCheckedChange = { checked -> onChange { copy(sound = checked) } },
                leading = DuiIcons.Sound,
            )
            DuiSwitchRow(
                title = "Vibration",
                description = "Feel a cue when your timer ends",
                checked = settings.vibration,
                onCheckedChange = { checked -> onChange { copy(vibration = checked) } },
                leading = DuiIcons.Vibration,
            )
        }

        SettingsGroup(title = "Appearance", subtitle = "Set the mood") {
            DuiSegmentedControl(
                options = ThemeMode.entries.map { it.label },
                selectedIndex = settings.theme.ordinal,
                onSelect = { index -> onChange { copy(theme = ThemeMode.entries[index]) } },
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, subtitle: String, content: @Composable () -> Unit) {
    val spacing = DuiTheme.spacing
    Column {
        DuiSectionHeader(title)
        DuiText(
            subtitle,
            Modifier.padding(bottom = spacing.space2),
            style = DuiTextStyle.Caption,
            color = DuiTheme.colors.textSecondary,
        )
        DuiCard {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) { content() }
        }
    }
}

private val RestMode.label: String
    get() = when (this) {
        RestMode.AUTO -> "Auto"
        RestMode.MANUAL -> "Manual"
    }

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

@Preview
@Composable
private fun SettingsContentPreview() {
    DuiTheme(darkTheme = false) {
        SettingsContent(settings = WorkoutSettings(), onChange = {})
    }
}

@Preview
@Composable
private fun SettingsContentDarkPreview() {
    DuiTheme(darkTheme = true) {
        SettingsContent(settings = WorkoutSettings(theme = ThemeMode.DARK), onChange = {})
    }
}
