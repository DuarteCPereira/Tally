package com.tally.domain

import com.tally.Feedback
import com.tally.TimerNotifier
import org.koin.core.annotation.Single

/** Builds the [RestTimer] for a run once the user's sound and vibration [WorkoutSettings] are known. */
fun interface RestTimerFactory {
    fun create(settings: WorkoutSettings): RestTimer
}

@Single(binds = [RestTimerFactory::class])
internal class PlatformRestTimerFactory(
    private val timerNotifier: TimerNotifier,
    private val feedback: Feedback,
) : RestTimerFactory {
    override fun create(settings: WorkoutSettings) = RestTimer(
        timerNotifier = timerNotifier,
        feedback = feedback,
        soundEnabled = settings.sound,
        vibrationEnabled = settings.vibration,
    )
}
