package com.tally.ui.home

import app.cash.turbine.test
import com.tally.fakes.FakeWorkoutRepository
import com.tally.fakes.Workouts
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {
    @Test
    fun `Given workouts are not read yet then state is loading instead of the empty list`() = runTest {
        val repository = FakeWorkoutRepository(workouts = null)
        val sut = HomeViewModel(repository, scope = viewModelTestScope())

        sut.state.test {
            assertEquals(expected = HomeUiState.Loading, actual = awaitItem())
            repository.stored.value = listOf(Workouts.legDay, Workouts.upper)
            assertEquals(
                expected = HomeUiState.Content(listOf(Workouts.upper, Workouts.legDay)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `When a workout is deleted then it is removed from the list`() = runTest {
        val repository = FakeWorkoutRepository(listOf(Workouts.upper, Workouts.legDay))
        val sut = HomeViewModel(repository, scope = viewModelTestScope())

        sut.delete(Workouts.legDay)

        assertEquals(expected = listOf(Workouts.legDay.id), actual = repository.deleted)
        assertEquals(expected = HomeUiState.Content(listOf(Workouts.upper)), actual = sut.state.value)
    }

    @Test
    fun `When a workout is duplicated then the repository copies it`() = runTest {
        val repository = FakeWorkoutRepository(listOf(Workouts.upper))
        val sut = HomeViewModel(repository, scope = viewModelTestScope())

        sut.duplicate(Workouts.upper)

        assertEquals(expected = listOf(Workouts.upper.id), actual = repository.duplicated)
    }
}
