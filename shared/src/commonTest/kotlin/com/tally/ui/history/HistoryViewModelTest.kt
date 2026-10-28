package com.tally.ui.history

import app.cash.turbine.test
import com.tally.domain.Session
import com.tally.domain.SessionEntry
import com.tally.domain.WorkoutItemType
import com.tally.fakes.FakeHistoryRepository
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class HistoryViewModelTest {
    private val now = Instant.parse("2026-03-10T12:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }

    private val recent = Session(
        id = "recent",
        workoutId = "w",
        workoutName = "Leg day",
        startedAt = Instant.parse("2026-03-09T18:05:00Z"),
        durationSeconds = 65 * 60,
    )
    private val old = recent.copy(id = "old", startedAt = now - 30.days, durationSeconds = 600)

    @Test
    fun `Given sessions are not read yet then state is loading instead of zero stats`() = runTest {
        val repository = FakeHistoryRepository(sessions = null)
        val sut = HistoryViewModel(repository, clock, TimeZone.UTC, scope = viewModelTestScope())

        sut.state.test {
            assertEquals(expected = HistoryUiState.Loading, actual = awaitItem())
            repository.storedSessions.value = listOf(recent)
            assertIs<HistoryUiState.Content>(awaitItem())
        }
    }

    @Test
    fun `When sessions are loaded then stats summarise them and the newest comes first`() = runTest {
        val entries = listOf(entry(sessionId = "recent", setNumber = 1, completed = true))
        val repository = FakeHistoryRepository(sessions = listOf(old, recent), entries = entries)

        val sut = HistoryViewModel(repository, clock, TimeZone.UTC, scope = viewModelTestScope())

        val content = assertIs<HistoryUiState.Content>(sut.state.value)
        assertEquals(
            expected = StatsUi(sessions = "2", totalTime = "1h 15m", totalSets = "1", thisWeek = "1"),
            actual = content.stats,
        )
        assertEquals(expected = listOf("recent", "old"), actual = content.sessions.map { it.id })
    }

    @Test
    fun `When a session has entries then its card lists every set with its result`() = runTest {
        val entries = listOf(
            entry(sessionId = "recent", setNumber = 2, completed = false, position = 1),
            entry(sessionId = "recent", setNumber = 1, completed = true, position = 0),
        )
        val repository = FakeHistoryRepository(sessions = listOf(recent), entries = entries)

        val sut = HistoryViewModel(repository, clock, TimeZone.UTC, scope = viewModelTestScope())

        val card = assertIs<HistoryUiState.Content>(sut.state.value).sessions.single()
        assertEquals(expected = "1 exercises  ·  1 sets", actual = card.summary)
        assertEquals(
            expected = listOf(
                ExerciseResultUi(
                    name = "Back squat",
                    lines = listOf(
                        ResultLineUi(label = "Set 1", result = "6 reps", done = true),
                        ResultLineUi(label = "Set 2", result = "Skipped", done = false),
                    ),
                ),
            ),
            actual = card.exercises,
        )
    }

    @Test
    fun `When a session is shown then its time is in the device time zone`() = runTest {
        val repository = FakeHistoryRepository(sessions = listOf(recent))
        val lisbon = TimeZone.of("Europe/Lisbon")

        val sut = HistoryViewModel(repository, clock, lisbon, scope = viewModelTestScope())

        // 18:05 UTC on 9 March is 18:05 in Lisbon (winter time) and the date stays the same
        val card = assertIs<HistoryUiState.Content>(sut.state.value).sessions.single()
        assertEquals(expected = "Mar 9, 2026  ·  6:05 PM", actual = card.date)

        val tokyo = HistoryViewModel(repository, clock, TimeZone.of("Asia/Tokyo"), scope = viewModelTestScope())
        val tokyoCard = assertIs<HistoryUiState.Content>(tokyo.state.value).sessions.single()
        assertEquals(expected = "Mar 10, 2026  ·  3:05 AM", actual = tokyoCard.date)
    }

    private fun entry(sessionId: String, setNumber: Int, completed: Boolean, position: Int = 0) = SessionEntry(
        id = "$sessionId-$setNumber",
        sessionId = sessionId,
        position = position,
        exerciseName = "Back squat",
        itemType = WorkoutItemType.REGULAR,
        setNumber = setNumber,
        roundNumber = null,
        targetReps = 5,
        actualReps = 6.takeIf { completed },
        completed = completed,
    )
}
