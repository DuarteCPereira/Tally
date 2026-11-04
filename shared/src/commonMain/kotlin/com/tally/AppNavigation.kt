package com.tally

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.duarte.dui.components.DuiBottomBar
import com.duarte.dui.components.DuiScaffold
import com.duarte.dui.components.DuiTab
import com.tally.ui.editor.WorkoutEditorScreen
import com.tally.ui.history.HistoryScreen
import com.tally.ui.home.HomeNavigation
import com.tally.ui.home.HomeScreen
import com.tally.ui.settings.SettingsScreen
import com.tally.ui.workout.WorkoutRunScreen
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

/**
 * The app's screens, shown by a Navigation 3 [NavDisplay] over a back stack of [Route]s.
 *
 * [Route.Main] is always the root and holds the tabs, whose selection is plain saved state so that
 * switching tab never animates or touches the back stack. The editor and a run are pushed on top,
 * so back (or finishing) returns to the tabs, and back on the tabs leaves the app. Each entry has
 * its own ViewModel store, so the editor's and the run's ViewModels are cleared when their entry is
 * popped. The tab ViewModels are created by [App] and passed in, so they outlive the tabs. The back
 * stack and the selected tab are saved, so they are restored after a rotation or the process being
 * killed.
 */
@Composable
internal fun AppNavigation(tabs: TabViewModels) {
    val backStack = rememberNavBackStack(BackStackConfiguration, Route.Main)
    var selectedTab by rememberSaveable(stateSaver = AppTabSaver) { mutableStateOf(AppTab.HOME) }
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.Main> {
                TabScaffold(selected = selectedTab, onSelect = { selectedTab = it }) { modifier ->
                    AnimatedContent(
                        targetState = selectedTab,
                        modifier = modifier,
                        transitionSpec = { tabTransition() },
                        label = "tab",
                    ) { tab ->
                        TabScreen(tab, tabs, backStack, Modifier.fillMaxSize())
                    }
                }
            }
            entry<Route.Editor> { route ->
                TabScaffold(
                    selected = null,
                    onSelect = {
                        selectedTab = it
                        backStack.popToRoot()
                    },
                ) { modifier ->
                    WorkoutEditorScreen(
                        editor = koinViewModel { parametersOf(route.workoutId) },
                        library = koinViewModel(),
                        onBack = backStack::pop,
                        modifier = modifier,
                    )
                }
            }
            entry<Route.Run> { route ->
                WorkoutRunScreen(koinViewModel { parametersOf(route.workoutId) }, onExit = backStack::pop)
            }
        },
    )
}

/** The bottom bar around a screen, with [selected] highlighted, or none for a screen opened from a tab. */
@Composable
private fun TabScaffold(selected: AppTab?, onSelect: (AppTab) -> Unit, content: @Composable (Modifier) -> Unit) {
    DuiScaffold(
        bottomBar = {
            DuiBottomBar(
                items = AppTab.entries.map { DuiTab(it.label, it.icon) },
                selectedIndex = selected?.ordinal ?: -1,
                onSelect = { onSelect(AppTab.entries[it]) },
            )
        },
    ) { padding ->
        content(
            Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
private fun TabScreen(tab: AppTab, tabs: TabViewModels, backStack: NavBackStack<NavKey>, modifier: Modifier) {
    when (tab) {
        AppTab.HOME -> HomeScreen(
            viewModel = tabs.home,
            navigation = HomeNavigation(
                onCreate = { backStack.add(Route.Editor(workoutId = null)) },
                onEdit = { backStack.add(Route.Editor(it)) },
                onStart = { backStack.add(Route.Run(it)) },
            ),
            modifier = modifier,
        )

        AppTab.HISTORY -> HistoryScreen(tabs.history, modifier)
        AppTab.SETTINGS -> SettingsScreen(tabs.settings, modifier)
    }
}

/** Non-Android targets can't find [Route]'s serializers by reflection, so they are listed here. */
internal val BackStackConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Main::class)
            subclass(Route.Editor::class)
            subclass(Route.Run::class)
        }
    }
}

private val TabTransitionDuration = 220.milliseconds
private const val TAB_SLIDE_FRACTION = 6 // of the width, so the tabs drift rather than swipe

/** Fades and nudges the tabs towards the side of the bar the new tab is on. */
private fun AnimatedContentTransitionScope<AppTab>.tabTransition(): ContentTransform {
    val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
    val spec = tween<Float>(TabTransitionDuration.inWholeMilliseconds.toInt())
    val offsetSpec = tween<IntOffset>(TabTransitionDuration.inWholeMilliseconds.toInt())
    return (fadeIn(spec) + slideInHorizontally(offsetSpec) { direction * it / TAB_SLIDE_FRACTION }) togetherWith
        (fadeOut(spec) + slideOutHorizontally(offsetSpec) { -direction * it / TAB_SLIDE_FRACTION })
}

private val AppTabSaver = Saver<AppTab, String>(save = { it.name }, restore = AppTab::valueOf)

/** Pops everything above [Route.Main], so leaving a screen through the bottom bar lands on the tabs. */
private fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) removeLast()
}

private fun NavBackStack<NavKey>.pop() {
    removeLastOrNull()
}
