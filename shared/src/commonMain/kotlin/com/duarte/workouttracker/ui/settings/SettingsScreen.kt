package com.duarte.workouttracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Make it yours", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Tune your workout experience.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SettingsGroup(title = "REST TIMER", subtitle = "Choose how rest intervals work") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(RestMode.AUTO, RestMode.MANUAL).forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.restMode == mode,
                        onClick = { update(settings.copy(restMode = mode)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                    ) {
                        Text(if (mode == RestMode.AUTO) "Auto" else "Manual")
                    }
                }
            }
        }

        SettingsGroup(title = "FEEDBACK", subtitle = "Stay in the rhythm, your way") {
            SettingToggle(
                icon = "♫",
                title = "Sound",
                description = "Play a cue when your timer ends",
                checked = settings.sound,
                onCheckedChange = { update(settings.copy(sound = it)) },
            )
            SettingToggle(
                icon = "⌁",
                title = "Vibration",
                description = "Feel a cue when your timer ends",
                checked = settings.vibration,
                onCheckedChange = { update(settings.copy(vibration = it)) },
            )
        }

        SettingsGroup(title = "APPEARANCE", subtitle = "Set the mood") {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.theme == mode,
                        onClick = { update(settings.copy(theme = mode)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    ) {
                        Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun SettingsGroup(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { content() }
        }
    }
}

@Composable
private fun SettingToggle(
    icon: String,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
        Text(icon, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
