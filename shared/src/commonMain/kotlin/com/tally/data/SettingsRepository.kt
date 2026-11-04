package com.tally.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tally.database.WorkoutDatabase
import com.tally.domain.RestMode
import com.tally.domain.ThemeMode
import com.tally.domain.WorkoutSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

interface SettingsRepository {
    /** The saved settings, with defaults for anything never saved. */
    val settings: Flow<WorkoutSettings>

    suspend fun update(settings: WorkoutSettings)
}

@Single(binds = [SettingsRepository::class])
class SqlDelightSettingsRepository(
    database: WorkoutDatabase,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : SettingsRepository {
    private val queries = database.exerciseQueries

    override val settings: Flow<WorkoutSettings> = queries.selectAllSettings()
        .asFlow()
        .mapToList(io)
        .map { rows -> rows.associate { it.key to it.value_ }.toWorkoutSettings() }

    override suspend fun update(settings: WorkoutSettings) = withContext(io) {
        queries.transaction {
            queries.upsertSetting(Keys.REST_MODE, settings.restMode.name)
            queries.upsertSetting(Keys.SOUND, settings.sound.toString())
            queries.upsertSetting(Keys.VIBRATION, settings.vibration.toString())
            queries.upsertSetting(Keys.THEME, settings.theme.name)
        }
    }

    private fun Map<String, String>.toWorkoutSettings(): WorkoutSettings {
        val defaults = WorkoutSettings()
        return WorkoutSettings(
            restMode = enumValueOrNull<RestMode>(get(Keys.REST_MODE)) ?: defaults.restMode,
            sound = get(Keys.SOUND)?.toBooleanStrictOrNull() ?: defaults.sound,
            vibration = get(Keys.VIBRATION)?.toBooleanStrictOrNull() ?: defaults.vibration,
            theme = enumValueOrNull<ThemeMode>(get(Keys.THEME)) ?: defaults.theme,
        )
    }

    private object Keys {
        const val REST_MODE = "restMode"
        const val SOUND = "sound"
        const val VIBRATION = "vibration"
        const val THEME = "theme"
    }
}

private inline fun <reified E : Enum<E>> enumValueOrNull(name: String?): E? =
    enumValues<E>().firstOrNull { it.name == name }
