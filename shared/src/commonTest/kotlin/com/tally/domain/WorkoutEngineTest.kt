package com.tally.domain

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutEngineTest {
    private val workout = Workout("w", "Workout", 0, Instant.fromEpochMilliseconds(1_000))

    @Test
    fun supersetMembersAreConsecutiveAndRestIsSuggestedAfterRound() {
        val superset = WorkoutItem("s", "w", 0, WorkoutItemType.SUPERSET, null, rounds = 2, restSeconds = 30)
        val members = listOf(
            SupersetMember("a", "s", 0, "ea", 8),
            SupersetMember("b", "s", 1, "eb", 12),
        )
        val engine = WorkoutEngine(workout, listOf(superset), mapOf("s" to members))

        assertEquals(0, engine.state.value.memberIndex)
        engine.completeSet(7)
        assertEquals(1, engine.state.value.memberIndex)
        assertEquals(0, engine.state.value.restSuggestedSeconds)
        engine.completeSet(12)
        assertEquals(0, engine.state.value.memberIndex)
        assertEquals(2, engine.state.value.roundNumber)
        assertEquals(30, engine.state.value.restSuggestedSeconds)
        assertEquals(2, engine.state.value.completedCount)
    }

    @Test
    fun regularSetsCanBeSkippedAndRevisitedAndFinishBuildsHistory() {
        val item = WorkoutItem("i", "w", 0, WorkoutItemType.REGULAR, "e", sets = 2, reps = 10)
        val engine = WorkoutEngine(
            workout,
            listOf(item),
            exerciseNames = mapOf("e" to "Squat"),
            settings = WorkoutSettings(restMode = RestMode.MANUAL),
            startedAt = Instant.fromEpochMilliseconds(1_000),
            idGenerator = { "id" },
        )
        assertEquals(RestMode.MANUAL, engine.state.value.restMode)
        assertFalse(engine.state.value.shouldAutoRest)
        engine.completeSet(9)
        assertEquals(60, engine.state.value.restSuggestedSeconds)
        engine.skip()
        assertTrue(engine.state.value.finished)
        assertEquals(0.5f, engine.state.value.progress)
        assertNull(engine.finish()?.entries?.get(1)?.actualReps)

        engine.previous()
        assertEquals(2, engine.state.value.setNumber)
        engine.completeSet(11)
        val finished = assertNotNull(engine.finish())
        assertEquals("id", finished.session.id)
        assertEquals("id", finished.entries.first().sessionId)
        assertEquals("Squat", finished.entries.first().exerciseName)
        assertEquals(9, finished.entries.first().actualReps)
        assertEquals(11, finished.entries.last().actualReps)
    }
}
