package com.tally.site

/**
 * A year of made-up training for the statistics preview, labelled "sample data" on the page.
 *
 * It is deterministic (a fixed-seed generator, no clock), so both sites, the tests and every
 * page load show the same numbers. Volume slowly grows over the year, so the line rises.
 */
public object SampleSessions {
    /** Sessions from today back to two years ago, newest first. */
    public val all: List<SessionRecord> by lazy { generate() }

    private const val DaysOfHistory = 730
    private const val Seed = 20_260_101L
    private const val BaseVolumeKg = 1_300
    private const val VolumeSpreadKg = 500
    private const val MinSets = 12
    private const val SetsSpread = 6

    /** A week off five weeks ago, so the streak tile shows a realistic "5 wk". */
    private val BreakDays = 35..41

    /** Rest days between sessions cycle through this pattern: roughly 3 to 4 sessions a week. */
    private val RestPattern = listOf(1, 2, 1, 3, 2)

    private fun generate(): List<SessionRecord> = buildList {
        val random = Lcg(Seed)
        var daysAgo = 0
        var step = 0
        while (daysAgo < DaysOfHistory) {
            if (daysAgo in BreakDays) {
                daysAgo = BreakDays.last + 1
                continue
            }
            // Older sessions are lighter: up to a third less volume two years back.
            val growth = 1.0 - daysAgo.toDouble() / DaysOfHistory / 3
            val volume = (BaseVolumeKg + random.next(VolumeSpreadKg)) * growth
            add(SessionRecord(daysAgo = daysAgo, volumeKg = volume.toInt(), sets = MinSets + random.next(SetsSpread)))
            daysAgo += 1 + RestPattern[step++ % RestPattern.size]
        }
    }

    /** A tiny linear congruential generator, so the sample never depends on a platform's Random. */
    private class Lcg(private var state: Long) {
        fun next(bound: Int): Int {
            state = (state * Multiplier + Increment) and Mask
            return ((state ushr Shift) % bound).toInt()
        }

        private companion object {
            const val Multiplier = 25_214_903_917L
            const val Increment = 11L
            const val Mask = (1L shl 48) - 1
            const val Shift = 17
        }
    }
}
