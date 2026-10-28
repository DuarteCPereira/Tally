package com.tally.ui.editor

import app.cash.turbine.test
import com.tally.domain.WorkoutContent
import com.tally.domain.WorkoutItemType
import com.tally.fakes.Exercises
import com.tally.fakes.FakeExerciseRepository
import com.tally.fakes.FakeWorkoutRepository
import com.tally.fakes.Workouts
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WorkoutEditorViewModelTest {
    private val library = listOf(Exercises.squat, Exercises.pushUp, Exercises.row)

    @Test
    fun `Given a new workout then the editor starts as content without a loading frame`() = runTest {
        val sut = editor(workoutId = null)

        val content = assertIs<EditorUiState.Content>(sut.state.value)
        assertTrue(content.isNew)
        assertTrue(content.blocks.isEmpty())
    }

    @Test
    fun `Given an existing workout then it loads before showing instead of flashing a new one`() = runTest {
        val workouts = FakeWorkoutRepository(workouts = null, mapOf(Workouts.legDay.id to Workouts.legDayContent))
        val sut = editor(Workouts.legDay.id, workouts)

        sut.state.test {
            assertEquals(expected = EditorUiState.Loading, actual = awaitItem())
            workouts.stored.value = listOf(Workouts.legDay)
            val content = assertIs<EditorUiState.Content>(awaitItem())
            assertFalse(content.isNew)
            assertEquals(expected = "Leg day", actual = sut.name)
            assertEquals(
                expected = listOf(
                    DraftBlock.Single(content.blocks[0].key, Exercises.squat),
                    DraftBlock.Superset(
                        key = content.blocks[1].key,
                        members = listOf(Exercises.pushUp, Exercises.row),
                        rounds = 2,
                        reps = 8,
                        restSeconds = 30,
                    ),
                ),
                actual = content.blocks,
            )
        }
    }

    @Test
    fun `When saving without a name then an error is shown and nothing is saved`() = runTest {
        val workouts = FakeWorkoutRepository()
        val sut = editor(workoutId = null, workouts)

        sut.save()

        val content = assertIs<EditorUiState.Content>(sut.state.value)
        assertEquals(expected = "Give your workout a name first.", actual = content.nameError)
        assertEquals(expected = emptyList(), actual = workouts.stored.value)
    }

    @Test
    fun `When a renamed workout is saved then it keeps its id and position`() = runTest {
        val workouts = FakeWorkoutRepository(
            listOf(Workouts.upper, Workouts.legDay),
            mapOf(Workouts.legDay.id to Workouts.legDayContent),
        )
        val sut = editor(Workouts.legDay.id, workouts)

        sut.rename("Leg day B")
        sut.save()

        assertTrue(assertIs<EditorUiState.Content>(sut.state.value).isSaved)
        assertEquals(
            expected = listOf(Workouts.upper, Workouts.legDay.copy(name = "Leg day B")),
            actual = workouts.stored.value,
        )
    }

    @Test
    fun `When a workout with a superset is saved then member defaults in the library are unchanged`() =
        runTest {
            val exercises = FakeExerciseRepository(library)
            val sut = editor(workoutId = null, exercises = exercises)

            sut.rename("Circuit")
            sut.saveSuperset(
                DraftBlock.Superset(
                    key = "s",
                    members = listOf(Exercises.pushUp, Exercises.row),
                    rounds = 3,
                    reps = 20,
                    restSeconds = 45,
                ),
            )
            sut.save()

            assertEquals(expected = library.toSet(), actual = exercises.stored.value.orEmpty().toSet())
        }

    @Test
    fun `When a block is moved down then it swaps with the next one`() = runTest {
        val sut = editor(workoutId = null)
        sut.addExercise(Exercises.squat)
        sut.addExercise(Exercises.pushUp)
        val first = assertIs<EditorUiState.Content>(sut.state.value).blocks.first()

        sut.move(first.key, offset = 1)
        sut.move(first.key, offset = 1) // Already last: nothing happens

        val blocks = assertIs<EditorUiState.Content>(sut.state.value).blocks
        assertEquals(
            expected = listOf(Exercises.pushUp, Exercises.squat),
            actual = blocks.map { (it as DraftBlock.Single).exercise },
        )
    }

    @Test
    fun `When draft blocks are saved then they round trip through the stored content`() {
        val blocks = listOf(
            DraftBlock.Single(key = "a", Exercises.squat),
            DraftBlock.Superset(key = "b", listOf(Exercises.pushUp, Exercises.row), rounds = 2, reps = 8, 30),
        )
        var id = 0

        val content: WorkoutContent = blocks.toContent(workoutId = "w", newId = { "id-${id++}" })
        val restored = content.toDraftBlocks(library, newKey = { "k" })

        assertEquals(
            expected = listOf(WorkoutItemType.REGULAR, WorkoutItemType.SUPERSET),
            actual = content.items.map { it.type },
        )
        assertEquals(expected = blocks.map { it.withKey("k") }, actual = restored)
    }

    private fun TestScope.editor(
        workoutId: String?,
        workouts: FakeWorkoutRepository = FakeWorkoutRepository(),
        exercises: FakeExerciseRepository = FakeExerciseRepository(library),
    ) = WorkoutEditorViewModel(workoutId, workouts, exercises, scope = viewModelTestScope())

    private fun DraftBlock.withKey(key: String): DraftBlock = when (this) {
        is DraftBlock.Single -> copy(key = key)
        is DraftBlock.Superset -> copy(key = key)
    }
}
