package com.tally.ui.settings

import app.cash.turbine.test
import com.tally.domain.RestMode
import com.tally.domain.ThemeMode
import com.tally.domain.WorkoutSettings
import com.tally.fakes.FakeSettingsRepository
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {
    @Test
    fun `Given settings are not read yet then state is loading instead of defaults`() = runTest {
        val repository = FakeSettingsRepository(settings = null)
        val sut = SettingsViewModel(repository, scope = viewModelTestScope())

        sut.state.test {
            assertEquals(expected = SettingsUiState.Loading, actual = awaitItem())
            repository.stored.value = WorkoutSettings(theme = ThemeMode.DARK)
            assertEquals(
                expected = SettingsUiState.Content(WorkoutSettings(theme = ThemeMode.DARK)),
                actual = awaitItem(),
            )
        }
    }

    @Test
    fun `When two settings change back to back then both changes are kept`() = runTest {
        val repository = FakeSettingsRepository()
        val sut = SettingsViewModel(repository, scope = viewModelTestScope())

        sut.update { copy(sound = false) }
        sut.update { copy(restMode = RestMode.MANUAL) }

        assertEquals(
            expected = WorkoutSettings(sound = false, restMode = RestMode.MANUAL),
            actual = repository.stored.value,
        )
    }
}
