package com.tally.ui.history

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private val MonthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)
private const val HOURS_PER_MERIDIEM = 12

/** "45m" or "1h 5m". Negative durations read as zero. */
internal fun formatDuration(duration: Duration): String {
    val safe = duration.coerceAtLeast(Duration.ZERO)
    val hours = safe.inWholeHours
    val minutes = safe.inWholeMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

internal fun formatDuration(seconds: Int): String = formatDuration(seconds.seconds)

/** "Mar 4, 2026  ·  6:05 PM", in the given [timeZone]. */
internal fun formatDateTime(instant: Instant, timeZone: TimeZone): String {
    val date = instant.toLocalDateTime(timeZone)
    val month = MonthNames[date.month.number - 1]
    val hour = (date.hour % HOURS_PER_MERIDIEM).takeIf { it != 0 } ?: HOURS_PER_MERIDIEM
    val minute = date.minute.toString().padStart(2, '0')
    val meridiem = if (date.hour < HOURS_PER_MERIDIEM) "AM" else "PM"
    return "$month ${date.day}, ${date.year}  ·  $hour:$minute $meridiem"
}
