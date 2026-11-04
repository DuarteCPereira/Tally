package com.tally.di

import com.tally.AppViewModel
import com.tally.Feedback
import com.tally.ImagePicker
import com.tally.ImageStorage
import com.tally.TimerNotifier
import com.tally.data.ExerciseRepository
import com.tally.data.HistoryRepository
import com.tally.data.SettingsRepository
import com.tally.data.WorkoutRepository
import com.tally.fakes.FakeExerciseRepository
import com.tally.fakes.FakeHistoryRepository
import com.tally.fakes.FakeSettingsRepository
import com.tally.fakes.FakeWorkoutRepository
import com.tally.ui.editor.ExerciseFormViewModel
import com.tally.ui.editor.ExerciseLibraryViewModel
import com.tally.ui.editor.SupersetFormViewModel
import com.tally.ui.editor.WorkoutEditorViewModel
import com.tally.ui.history.HistoryViewModel
import com.tally.ui.home.HomeViewModel
import com.tally.ui.settings.SettingsViewModel
import com.tally.ui.workout.WorkoutRunViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class AppModuleTest {
    /** Replaces the database, which needs a real driver, and the platform services. */
    private val fakePlatformAndData = module {
        single<WorkoutRepository> { FakeWorkoutRepository() }
        single<ExerciseRepository> { FakeExerciseRepository() }
        single<SettingsRepository> { FakeSettingsRepository() }
        single<HistoryRepository> { FakeHistoryRepository() }
        single<ImagePicker> { NoopImagePicker }
        single<ImageStorage> { NoopImageStorage }
        single<Feedback> { NoopFeedback }
        single<TimerNotifier> { NoopTimerNotifier }
    }

    private lateinit var koin: Koin

    @BeforeTest
    fun setUp() {
        // ViewModels default to a Main-based scope
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // Later modules override earlier ones
        koin = koinApplication { modules(AppModule().module(), fakePlatformAndData) }.koin
    }

    @AfterTest
    fun tearDown() {
        koin.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `Given the data and platform are provided when resolving the tab ViewModels then they are built`() {
        assertNotNull(koin.get<AppViewModel>())
        assertNotNull(koin.get<HomeViewModel>())
        assertNotNull(koin.get<HistoryViewModel>())
        assertNotNull(koin.get<SettingsViewModel>())
        assertNotNull(koin.get<ExerciseLibraryViewModel>())
    }

    @Test
    fun `Given the screen passes its argument when resolving a parameterised ViewModel then it is built`() {
        assertNotNull(koin.get<WorkoutEditorViewModel> { parametersOf(null) })
        assertNotNull(koin.get<ExerciseFormViewModel> { parametersOf(null) })
        assertNotNull(koin.get<SupersetFormViewModel> { parametersOf(null) })
        assertNotNull(koin.get<WorkoutRunViewModel> { parametersOf("workout-id") })
    }

    private object NoopImagePicker : ImagePicker {
        override suspend fun pickFromGallery(): String? = null
        override suspend fun takePhoto(): String? = null
    }

    private object NoopImageStorage : ImageStorage {
        override suspend fun copyPickedImage(sourcePath: String) = sourcePath
    }

    private object NoopFeedback : Feedback {
        override fun vibrate() = Unit
        override fun playSound() = Unit
    }

    private object NoopTimerNotifier : TimerNotifier {
        override fun schedule(endTimeEpochMillis: Long) = Unit
        override fun cancel() = Unit
    }
}
