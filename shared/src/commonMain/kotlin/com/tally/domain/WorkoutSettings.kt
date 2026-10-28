package com.tally.domain

/** Whether rest starts on its own after a set or waits for the user to start it. */
enum class RestMode { AUTO, MANUAL }

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class WorkoutSettings(
    val restMode: RestMode = RestMode.AUTO,
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
)

/** Resolves this mode to a dark or light theme, following the system when [ThemeMode.SYSTEM]. */
fun ThemeMode.isDark(systemIsDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemIsDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
