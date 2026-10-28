package com.tally.domain

import com.tally.Feedback
import com.tally.TimerNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

/** Wall-clock rest timer. Remaining values are rounded up and exposed in whole seconds. */
class RestTimer(
    private val timerNotifier: TimerNotifier,
    private val feedback: Feedback,
    private val soundEnabled: Boolean = true,
    private val vibrationEnabled: Boolean = true,
    private val nowMillis: () -> Long = { kotlin.time.Clock.System.now().toEpochMilliseconds() },
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    /** Epoch-millisecond deadline while running; null while stopped or paused. */
    var endsAtEpochMillis: Long? = null
        private set

    val isRunning: Boolean get() = endsAtEpochMillis != null
    val isPaused: Boolean get() = pausedRemainingMillis != null

    private var pausedRemainingMillis: Long? = null
    private var ticker: Job? = null

    fun start(seconds: Int) {
        require(seconds >= 0) { "Rest duration cannot be negative" }
        cancelTickerAndNotification()
        pausedRemainingMillis = null
        if (seconds == 0) {
            endsAtEpochMillis = null
            _remainingSeconds.value = 0
            return
        }
        runUntil(nowMillis() + seconds * 1000L)
    }

    fun pause() {
        val end = endsAtEpochMillis ?: return
        pausedRemainingMillis = (end - nowMillis()).coerceAtLeast(0L)
        endsAtEpochMillis = null
        cancelTickerAndNotification()
        refresh()
    }

    fun resume() {
        val remaining = pausedRemainingMillis ?: return
        pausedRemainingMillis = null
        if (remaining <= 0L) finish() else runUntil(nowMillis() + remaining)
    }

    /** Stop immediately and clear the remaining duration. */
    fun skip() {
        cancelTickerAndNotification()
        endsAtEpochMillis = null
        pausedRemainingMillis = null
        _remainingSeconds.value = 0
    }

    /** Adjust by seconds. A running deadline shifts from now; a paused timer adjusts its stored time. */
    fun addSeconds(seconds: Int) {
        if (seconds == 0) return
        val delta = seconds * 1000L
        val runningEnd = endsAtEpochMillis
        if (runningEnd != null) {
            val adjusted = (runningEnd + delta).coerceAtLeast(nowMillis())
            if (adjusted <= nowMillis()) finish() else runUntil(adjusted)
        } else {
            val paused = pausedRemainingMillis
            if (paused != null) {
                val adjusted = (paused + delta).coerceAtLeast(0L)
                if (adjusted == 0L) {
                    pausedRemainingMillis = null
                    _remainingSeconds.value = 0
                } else {
                    pausedRemainingMillis = adjusted
                    _remainingSeconds.value = ceil(adjusted / 1000.0).toInt()
                }
            }
        }
    }

    /** Recompute from the wall-clock deadline; useful when the app returns from the background. */
    fun refresh() {
        val end = endsAtEpochMillis ?: run {
            pausedRemainingMillis?.let { _remainingSeconds.value = ceil(it / 1000.0).toInt() }
            return
        }
        val left = end - nowMillis()
        if (left <= 0L) finish() else _remainingSeconds.value = ceil(left / 1000.0).toInt()
    }

    /** Cancel scheduled notifications and the polling coroutine when the owner is disposed. */
    fun dispose() {
        cancelTickerAndNotification()
        endsAtEpochMillis = null
        pausedRemainingMillis = null
        scope.cancel()
    }

    private fun runUntil(endEpochMillis: Long) {
        endsAtEpochMillis = endEpochMillis
        pausedRemainingMillis = null
        timerNotifier.schedule(endEpochMillis)
        refresh()
        ticker?.cancel()
        ticker = scope.launch {
            while (endsAtEpochMillis != null) {
                delay(TickInterval)
                refresh()
            }
        }
    }

    private fun finish() {
        val wasActive = endsAtEpochMillis != null || pausedRemainingMillis != null
        cancelTickerAndNotification()
        endsAtEpochMillis = null
        pausedRemainingMillis = null
        _remainingSeconds.value = 0
        if (wasActive) {
            if (vibrationEnabled) feedback.vibrate()
            if (soundEnabled) feedback.playSound()
        }
    }

    private fun cancelTickerAndNotification() {
        ticker?.cancel()
        ticker = null
        timerNotifier.cancel()
    }

    private companion object {
        /** How often the remaining time is recomputed from the wall-clock deadline. */
        val TickInterval = 250.milliseconds
    }
}
