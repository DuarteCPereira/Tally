package com.tally

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class RouteSerializationTest {
    private val serializer = ListSerializer(PolymorphicSerializer(NavKey::class))

    @Test
    fun `Given a back stack with every kind of route when saved and restored then it is unchanged`() {
        val backStack: List<NavKey> = listOf(
            Route.Main,
            Route.Editor(workoutId = null),
            Route.Editor(workoutId = "w1"),
            Route.Run(workoutId = "w2"),
        )

        val saved = encodeToSavedState(serializer, backStack, BackStackConfiguration)

        assertEquals(
            expected = backStack,
            actual = decodeFromSavedState(serializer, saved, BackStackConfiguration),
        )
    }
}
