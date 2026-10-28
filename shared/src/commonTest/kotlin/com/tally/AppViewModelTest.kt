package com.tally

import app.cash.turbine.test
import com.tally.domain.ThemeMode
import com.tally.domain.WorkoutSettings
import com.tally.fakes.FakeSettingsRepository
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppViewModelTest {
    @Test
    fun `Given the saved theme is not read yet then the app waits instead of using the system theme`() =
        runTest {
            val repository = FakeSettingsRepository(settings = null)
            val sut = AppViewModel(repository, scope = viewModelTestScope())

            sut.state.test {
                assertEquals(expected = AppUiState.Loading, actual = awaitItem())
                repository.stored.value = WorkoutSettings(theme = ThemeMode.LIGHT)
                assertEquals(expected = AppUiState.Ready(ThemeMode.LIGHT), actual = awaitItem())
            }
        }

    @Test
    fun `When a setting other than the theme changes then the app state does not change`() = runTest {
        val repository = FakeSettingsRepository(WorkoutSettings(theme = ThemeMode.DARK))
        val sut = AppViewModel(repository, scope = viewModelTestScope())

        sut.state.test {
            assertEquals(expected = AppUiState.Ready(ThemeMode.DARK), actual = awaitItem())
            repository.stored.value = WorkoutSettings(theme = ThemeMode.DARK, sound = false)
            expectNoEvents()
        }
    }
}
