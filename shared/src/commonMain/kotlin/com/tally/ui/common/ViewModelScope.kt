package com.tally.ui.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * The scope a ViewModel uses unless one is injected, matching the default `viewModelScope`.
 * ViewModels take it as a constructor parameter so tests can pass a test scope instead.
 */
fun defaultViewModelScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
