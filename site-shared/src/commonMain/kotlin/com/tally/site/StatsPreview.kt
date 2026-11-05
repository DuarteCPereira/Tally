package com.tally.site

import kotlin.math.roundToInt

/** A finished session, counted in whole days before today (0 is today). */
public data class SessionRecord(
    public val daysAgo: Int,
    public val volumeKg: Int,
    public val sets: Int,
) {
    init {
        require(daysAgo >= 0) { "daysAgo must not be negative, was $daysAgo" }
        require(volumeKg >= 0) { "volumeKg must not be negative, was $volumeKg" }
        require(sets >= 0) { "sets must not be negative, was $sets" }
    }
}

/** The ranges under the statistics chart. [label] is the chip text, [comparison] its "vs" phrase. */
public enum class StatsRange(public val label: String, public val days: Int, public val comparison: String) {
    Week(label = "1W", days = 7, comparison = "last week"),
    Month(label = "1M", days = 30, comparison = "last month"),
    Quarter(label = "3M", days = 90, comparison = "last quarter"),
    Year(label = "1Y", days = 365, comparison = "last year"),
}

/** Whether the volume went up or down against the previous period. */
public enum class Trend { Up, Down }

/** A small labelled number under the chart ("Sessions", "14"). */
public data class StatTile(public val label: String, public val value: String)

/**
 * Everything the statistics card shows for one [range], already formatted, so both sites render
 * the same strings.
 *
 * @property points cumulative volume across the range, oldest first, for a rising line chart.
 */
public data class StatsSummary(
    public val range: StatsRange,
    public val title: String,
    public val volume: String,
    public val change: String,
    public val trend: Trend,
    public val points: List<Float>,
    public val chartDescription: String,
    public val tiles: List<StatTile>,
)

/**
 * Turns a list of sessions into the statistics card. This is the logic the web statistics page
 * and the app will share, so it lives here, not in either site.
 */
public object StatsPreview {
    /** How many points the chart gets, whatever the range. */
    public const val ChartPoints: Int = 12

    /** Summarises [sessions] over [range], compared with the period just before it. */
    public fun summarize(sessions: List<SessionRecord>, range: StatsRange): StatsSummary {
        val current = sessions.filter { it.daysAgo < range.days }
        val previous = sessions.filter { it.daysAgo >= range.days && it.daysAgo < range.days * 2 }
        val volume = current.sumOf { it.volumeKg }
        val change = percentChange(from = previous.sumOf { it.volumeKg }, to = volume)
        val trend = if (change < 0) Trend.Down else Trend.Up
        return StatsSummary(
            range = range,
            title = "Volume ${range.title}",
            volume = "${volume.withThousands()} kg",
            change = "${change.signed()}% vs ${range.comparison}",
            trend = trend,
            points = cumulativePoints(current, range),
            chartDescription = "Volume ${if (trend == Trend.Up) "rising" else "falling"} over the ${range.noun}",
            tiles = listOf(
                StatTile(label = "Sessions", value = current.size.toString()),
                StatTile(label = "Streak", value = "${weekStreak(sessions)} wk"),
                StatTile(label = "Sets", value = current.sumOf { it.sets }.withThousands()),
            ),
        )
    }

    /**
     * Consecutive weeks, counting back from this one, with at least one session. A week with no
     * session ends the streak, except the current week, which may still be in progress.
     */
    public fun weekStreak(sessions: List<SessionRecord>): Int {
        val weeks = sessions.map { it.daysAgo / DaysPerWeek }.toSet()
        val start = if (0 in weeks) 0 else 1
        return generateSequence(start) { it + 1 }.takeWhile { it in weeks }.count()
    }

    /** Whole-number change from [from] to [to]; 100 when there was nothing before. */
    internal fun percentChange(from: Int, to: Int): Int = when {
        from == 0 && to == 0 -> 0
        from == 0 -> FullChange
        else -> ((to - from) * FullChange.toDouble() / from).roundToInt()
    }

    private fun cumulativePoints(sessions: List<SessionRecord>, range: StatsRange): List<Float> {
        val bucketDays = range.days.toDouble() / ChartPoints
        val perBucket = IntArray(ChartPoints)
        sessions.forEach { session ->
            // Oldest bucket first: a session 29 days ago lands near index 0 of a month.
            val fromStart = range.days - 1 - session.daysAgo
            val index = (fromStart / bucketDays).toInt().coerceIn(0, ChartPoints - 1)
            perBucket[index] += session.volumeKg
        }
        return perBucket.runningReduce(Int::plus).map(Int::toFloat)
    }

    private const val DaysPerWeek = 7
    private const val FullChange = 100

    private val StatsRange.title: String
        get() = when (this) {
            StatsRange.Week -> "this week"
            StatsRange.Month -> "this month"
            StatsRange.Quarter -> "this quarter"
            StatsRange.Year -> "this year"
        }

    private val StatsRange.noun: String
        get() = when (this) {
            StatsRange.Week -> "week"
            StatsRange.Month -> "month"
            StatsRange.Quarter -> "quarter"
            StatsRange.Year -> "year"
        }
}

//region Formatting

/** "18240" → "18,240". Kotlin common has no locale formatting, and the site copy is English. */
internal fun Int.withThousands(): String {
    val digits = kotlin.math.abs(this).toString()
    val grouped = digits.reversed().chunked(ThousandsGroup).joinToString(",").reversed()
    return if (this < 0) "-$grouped" else grouped
}

/** Explicit sign for changes, as the design system asks: "+12", "-3", "0". */
internal fun Int.signed(): String = if (this > 0) "+$this" else toString()

private const val ThousandsGroup = 3

//endregion
