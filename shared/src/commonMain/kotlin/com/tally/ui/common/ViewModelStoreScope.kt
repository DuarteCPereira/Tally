package com.tally.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

/**
 * Gives [content] its own [ViewModelStore], so ViewModels created inside it live exactly as long
 * as this scope is composed for the same [key]. Use it for screens that are opened and closed
 * (an editor, a running workout) so their ViewModels are cleared on exit instead of living in the
 * activity's store forever.
 */
@Composable
fun ViewModelStoreScope(key: Any?, content: @Composable () -> Unit) {
    val owner = remember(key) {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    CompositionLocalProvider(LocalViewModelStoreOwner provides owner, content = content)
}
