package com.duarte.workouttracker.domain

import com.duarte.workouttracker.Feedback
import com.duarte.workouttracker.TimerNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RestTimerTest {
    @Test
    fun deadlinePauseResumeAdjustAndSkipUseWallClock() {
        var now = 1_000L
        var scheduled: Long? = null
        var cancels = 0
        var sounds = 0
        var vibrations = 0
        val timer = RestTimer(
            object : TimerNotifier {
                override fun schedule(endTimeEpochMillis: Long) { scheduled = endTimeEpochMillis }
                override fun cancel() { cancels++ }
            },
            object : Feedback {
                override fun vibrate() { vibrations++ }
                override fun playSound() { sounds++ }
            },
            nowMillis = { now },
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )

        timer.start(10)
        assertEquals(11_000L, timer.endsAtEpochMillis)
        assertEquals(10, timer.remainingSeconds.value)
        now = 3_500L
        timer.refresh()
        assertEquals(8, timer.remainingSeconds.value)
        timer.addSeconds(2)
        assertEquals(13_000L, scheduled)
        timer.pause()
        assertTrue(timer.isPaused)
        assertNull(timer.endsAtEpochMillis)
        now += 10_000L
        timer.resume()
        assertEquals(23_000L, timer.endsAtEpochMillis)
        timer.skip()
        assertFalse(timer.isRunning)
        assertEquals(0, timer.remainingSeconds.value)
        assertTrue(cancels > 0)
        assertEquals(0, sounds)
        assertEquals(0, vibrations)
        timer.dispose()
    }

    @Test
    fun timerCompletionTriggersConfiguredFeedback() {
        var now = 0L
        var sounds = 0
        var vibrations = 0
        val timer = RestTimer(
            object : TimerNotifier {
                override fun schedule(endTimeEpochMillis: Long) = Unit
                override fun cancel() = Unit
            },
            object : Feedback {
                override fun vibrate() { vibrations++ }
                override fun playSound() { sounds++ }
            },
            nowMillis = { now },
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )
        timer.start(3)
        now = 3_000L
        timer.refresh()
        assertEquals(0, timer.remainingSeconds.value)
        assertEquals(1, sounds)
        assertEquals(1, vibrations)
        timer.dispose()
    }
}
