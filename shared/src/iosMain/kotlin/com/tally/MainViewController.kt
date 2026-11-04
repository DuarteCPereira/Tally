package com.tally

import androidx.compose.ui.window.ComposeUIViewController
import com.tally.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() },
) { App() }
