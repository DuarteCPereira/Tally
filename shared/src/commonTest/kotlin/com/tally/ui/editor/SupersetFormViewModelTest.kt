package com.tally.ui.editor

import com.tally.fakes.Exercises
import com.tally.fakes.viewModelTestScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SupersetFormViewModelTest {
    @Test
    fun `Given fewer than two members when saving then it publishes a message and returns nothing`() = runTest {
        val sut = superset()
        sut.addMember(Exercises.squat)

        assertNull(sut.save())
        assertEquals(expected = "A superset needs at least two exercises.", actual = sut.state.value.message)
    }

    @Test
    fun `Given two members when saving then the superset keeps their order`() = runTest {
        val sut = superset()
        sut.addMember(Exercises.squat)
        sut.addMember(Exercises.pushUp)

        assertEquals(
            expected = listOf(Exercises.squat, Exercises.pushUp),
            actual = assertNotNull(sut.save()).members,
        )
    }

    @Test
    fun `Given members when moving one then the order changes`() = runTest {
        val sut = superset()
        sut.addMember(Exercises.squat)
        sut.addMember(Exercises.pushUp)

        sut.moveMember(from = 1, to = 0)

        assertEquals(
            expected = listOf(Exercises.pushUp, Exercises.squat),
            actual = sut.state.value.form.members,
        )
    }

    @Test
    fun `Given members when moving out of range then the order is unchanged`() = runTest {
        val sut = superset()
        sut.addMember(Exercises.squat)

        sut.moveMember(from = 0, to = -1)
        sut.moveMember(from = 5, to = 0)

        assertEquals(expected = listOf(Exercises.squat), actual = sut.state.value.form.members)
    }

    @Test
    fun `Given members when removing one then only that one is dropped`() = runTest {
        val sut = superset()
        sut.addMember(Exercises.squat)
        sut.addMember(Exercises.pushUp)

        sut.removeMember(index = 0)

        assertEquals(expected = listOf(Exercises.pushUp), actual = sut.state.value.form.members)
    }

    private fun TestScope.superset() = SupersetFormViewModel(initial = null, scope = viewModelTestScope())
}
