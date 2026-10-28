package com.tally.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.duarte.dui.components.DuiLoadingIndicator
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Fills the available space while a screen is loading. The spinner only appears after [delay], so
 * the usual sub-frame database reads show nothing at all instead of flashing a spinner.
 */
@Composable
fun DelayedLoadingIndicator(modifier: Modifier = Modifier, delay: Duration = SpinnerDelay) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay)
        visible = true
    }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (visible) DuiLoadingIndicator()
    }
}

private val SpinnerDelay = 250.milliseconds
