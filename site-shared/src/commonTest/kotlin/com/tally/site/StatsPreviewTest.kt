package com.tally.site

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StatsPreviewTest {

    @Test
    fun `Given sessions in this and last month when summarised then volume and change compare the two`() {
        val sessions = listOf(
            session(daysAgo = 1, volumeKg = 6_000, sets = 20),
            session(daysAgo = 10, volumeKg = 5_200, sets = 18),
            session(daysAgo = 40, volumeKg = 10_000, sets = 30),
        )

        val summary = StatsPreview.summarize(sessions, StatsRange.Month)

        assertEquals(expected = "Volume this month", actual = summary.title)
        assertEquals(expected = "11,200 kg", actual = summary.volume)
        assertEquals(expected = "+12% vs last month", actual = summary.change)
        assertEquals(expected = Trend.Up, actual = summary.trend)
        assertEquals(
            expected = listOf(StatTile("Sessions", "2"), StatTile("Streak", "2 wk"), StatTile("Sets", "38")),
            actual = summary.tiles,
        )
    }

    @Test
    fun `Given less volume than last week when summarised then change is negative with its sign`() {
        val sessions = listOf(session(daysAgo = 0, volumeKg = 900), session(daysAgo = 8, volumeKg = 1_000))

        val summary = StatsPreview.summarize(sessions, StatsRange.Week)

        assertEquals(expected = "-10% vs last week", actual = summary.change)
        assertEquals(expected = Trend.Down, actual = summary.trend)
        assertEquals(expected = "Volume falling over the week", actual = summary.chartDescription)
    }

    @Test
    fun `Given no sessions when summarised then everything is zero and the line is flat`() {
        val summary = StatsPreview.summarize(emptyList(), StatsRange.Year)

        assertEquals(expected = "0 kg", actual = summary.volume)
        assertEquals(expected = "0% vs last year", actual = summary.change)
        assertEquals(expected = List(StatsPreview.ChartPoints) { 0f }, actual = summary.points)
    }

    @Test
    fun `Given sessions only in this period when summarised then change is a full hundred percent`() {
        val summary = StatsPreview.summarize(listOf(session(daysAgo = 2, volumeKg = 500)), StatsRange.Week)

        assertEquals(expected = "+100% vs last week", actual = summary.change)
    }

    @Test
    fun `Given sessions across a range when summarised then the chart is cumulative and oldest first`() {
        val sessions = listOf(session(daysAgo = 0, volumeKg = 3), session(daysAgo = 29, volumeKg = 5))

        val points = StatsPreview.summarize(sessions, StatsRange.Month).points

        assertEquals(expected = StatsPreview.ChartPoints, actual = points.size)
        assertEquals(expected = 5f, actual = points.first())
        assertEquals(expected = 8f, actual = points.last())
        assertTrue(points.zipWithNext().all { (a, b) -> b >= a }, "points never go down: $points")
    }

    @Test
    fun `Given a negative day offset when creating a session then it is rejected`() {
        assertFailsWith<IllegalArgumentException> { session(daysAgo = -1) }
    }

    @Test
    fun `Given the sample sessions when summarised twice then the numbers are identical`() {
        val first = StatsPreview.summarize(SampleSessions.all, StatsRange.Month)
        val second = StatsPreview.summarize(SampleSessions.all, StatsRange.Month)

        assertEquals(expected = first, actual = second)
        assertEquals(expected = Trend.Up, actual = first.trend)
        assertEquals(expected = StatTile(label = "Streak", value = "5 wk"), actual = first.tiles[1])
    }

    private fun session(daysAgo: Int, volumeKg: Int = 1_000, sets: Int = 10) =
        SessionRecord(daysAgo = daysAgo, volumeKg = volumeKg, sets = sets)
}
