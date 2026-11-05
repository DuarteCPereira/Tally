package com.tally.site

import kotlin.test.Test
import kotlin.test.assertEquals

class WeekStreakTest {

    @Test
    fun `Given a session in each of the last three weeks when counting then the streak is three`() {
        assertEquals(expected = 3, actual = StatsPreview.weekStreak(sessionsOn(1, 9, 16)))
    }

    @Test
    fun `Given no session yet this week when counting then the streak starts from last week`() {
        assertEquals(expected = 2, actual = StatsPreview.weekStreak(sessionsOn(8, 15)))
    }

    @Test
    fun `Given a week without sessions when counting then the streak stops there`() {
        assertEquals(expected = 1, actual = StatsPreview.weekStreak(sessionsOn(2, 20)))
    }

    @Test
    fun `Given nothing in the last two weeks when counting then there is no streak`() {
        assertEquals(expected = 0, actual = StatsPreview.weekStreak(sessionsOn(30)))
    }

    private fun sessionsOn(vararg daysAgo: Int) = daysAgo.map { SessionRecord(daysAgo = it, volumeKg = 1, sets = 1) }
}
