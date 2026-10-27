package com.duarte.workouttracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.duarte.dui.components.DuiCard
import com.duarte.dui.components.DuiIcons
import com.duarte.dui.components.DuiSectionHeader
import com.duarte.dui.components.DuiSegmentedControl
import com.duarte.dui.components.DuiSwitchRow
import com.duarte.dui.components.DuiText
import com.duarte.dui.components.DuiTextStyle
import com.duarte.dui.theme.DuiTheme
import com.duarte.workouttracker.AppContainer
import com.duarte.workouttracker.domain.RestMode
import com.duarte.workouttracker.domain.ThemeMode
import com.duarte.workouttracker.domain.WorkoutSettings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(container: AppContainer) {
    val settings by container.settings.settings.collectAsState(initial = WorkoutSettings())
    val scope = rememberCoroutineScope()
    val update: (WorkoutSettings) -> Unit = { next -> scope.launch { container.settings.update(next) } }
    val spacing = DuiTheme.spacing

    Column(Modifier.fillMaxSize().padding(horizontal = spacing.space4, vertical = spacing.space4)) {
        DuiText("Make it yours", style = DuiTextStyle.Title)
        DuiText("Tune your workout experience.", style = DuiTextStyle.Body, color = DuiTheme.colors.textSecondary)

        val restModes = listOf(RestMode.AUTO, RestMode.MANUAL)
        SettingsGroup(title = "Rest timer", subtitle = "Choose how rest intervals work") {
            DuiSegmentedControl(
                options = listOf("Auto", "Manual"),
                selectedIndex = restModes.indexOf(settings.restMode),
                onSelect = { update(settings.copy(restMode = restModes[it])) },
            )
        }

        SettingsGroup(title = "Feedback", subtitle = "Stay in the rhythm, your way") {
            DuiSwitchRow(
                title = "Sound",
                description = "Play a cue when your timer ends",
                checked = settings.sound,
                onCheckedChange = { update(settings.copy(sound = it)) },
                leading = DuiIcons.Sound,
            )
            DuiSwitchRow(
                title = "Vibration",
                description = "Feel a cue when your timer ends",
                checked = settings.vibration,
                onCheckedChange = { update(settings.copy(vibration = it)) },
                leading = DuiIcons.Vibration,
            )
        }

        val themeModes = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)
        SettingsGroup(title = "Appearance", subtitle = "Set the mood") {
            DuiSegmentedControl(
                options = themeModes.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                selectedIndex = themeModes.indexOf(settings.theme),
                onSelect = { update(settings.copy(theme = themeModes[it])) },
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, subtitle: String, content: @Composable () -> Unit) {
    val spacing = DuiTheme.spacing
    Column {
        DuiSectionHeader(title)
        DuiText(subtitle, Modifier.padding(bottom = spacing.space2), style = DuiTextStyle.Caption, color = DuiTheme.colors.textSecondary)
        DuiCard {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.space3)) { content() }
        }
    }
}
