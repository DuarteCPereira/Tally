package com.tally.ui.editor

import com.tally.domain.Exercise
import com.tally.fakes.Exercises
import kotlin.test.Test
import kotlin.test.assertEquals

class EditorFormsTest {
    @Test
    fun `Given a complete exercise form then it parses into an exercise`() {
        val form = ExerciseForm(name = "  Deadlift ", sets = "3", reps = "5", rest = "0", notes = " ")

        assertEquals(
            expected = FormResult.Valid(
                Exercise(id = "e", name = "Deadlift", defaultSets = 3, defaultReps = 5, defaultRestSeconds = 0),
            ),
            actual = form.validate(id = "e"),
        )
    }

    @Test
    fun `Given a blank name or zero reps then the exercise form is invalid`() {
        assertEquals(
            expected = FormResult.Invalid("Enter an exercise name."),
            actual = ExerciseForm(name = " ").validate(id = "e"),
        )
        assertEquals(
            expected = FormResult.Invalid("Use positive sets and reps, and a non-negative rest."),
            actual = ExerciseForm(name = "Deadlift", reps = "0").validate(id = "e"),
        )
    }

    @Test
    fun `Given fewer than two members then the superset form is invalid`() {
        assertEquals(
            expected = FormResult.Invalid("A superset needs at least two exercises."),
            actual = SupersetForm(members = listOf(Exercises.pushUp)).validate(key = "s"),
        )
    }

    @Test
    fun `When an existing superset is edited then its form starts from its values`() {
        val superset = DraftBlock.Superset("s", listOf(Exercises.pushUp, Exercises.row), rounds = 4, reps = 6, 20)

        assertEquals(expected = FormResult.Valid(superset), actual = SupersetForm.of(superset).validate(key = "s"))
    }
}
