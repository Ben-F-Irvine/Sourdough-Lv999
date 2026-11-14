package com.sourdough.companion.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sourdough.companion.data.BakeStatus
import com.sourdough.companion.data.formatCountdown
import com.sourdough.companion.ui.bake.BakeHome
import com.sourdough.companion.ui.bake.BakeViewModel
import com.sourdough.companion.ui.components.StepIcon
import com.sourdough.companion.ui.components.rememberNow
import com.sourdough.companion.ui.games.ClickerScreen
import com.sourdough.companion.ui.games.Game
import com.sourdough.companion.ui.games.GamesHub
import com.sourdough.companion.ui.games.ScrambleScreen
import com.sourdough.companion.ui.games.TriviaScreen
import com.sourdough.companion.ui.games.WordSearchScreen
import com.sourdough.companion.ui.log.BakeDetailScreen
import com.sourdough.companion.ui.log.BakeLogScreen
import com.sourdough.companion.ui.templates.SchedulesScreen
import com.sourdough.companion.ui.templates.StepEditorScreen
import com.sourdough.companion.ui.templates.TemplateEditorScreen
import com.sourdough.engine.Phase

/** Bottom-bar tabs. Every route belongs to the tab whose prefix it starts with. */
private enum class Tab(val route: String, val emoji: String, val label: String) {
    Bake("bake", "🍞", "Bake"),
    Schedules("schedules", "📋", "Schedules"),
    Log("log", "📖", "Log"),
    Games("games", "🎮", "Games");

    companion object {
        fun of(route: String?): Tab = when {
            route == null -> Bake
            route.startsWith("template") -> Schedules
            else -> entries.firstOrNull { route.startsWith(it.route) } ?: Bake
        }
    }
}

private object Routes {
    const val TEMPLATE = "template/{templateId}"
    const val STEP = "template/{templateId}/step/{stepId}?phase={phase}"
    const val BAKE_DETAIL = "log/{bakeId}"

    fun template(id: Long) = "template/$id"

    /** stepId 0 opens a new step in [phase]. */
    fun step(templateId: Long, stepId: Long, phase: Phase) = "template/$templateId/step/$stepId?phase=$phase"

    fun bakeDetail(id: Long) = "log/$id"
}

@Composable
fun SourdoughNavHost(navController: NavHostController = rememberNavController()) {
    // Activity-scoped, so the bake tab and the timer bar on every other tab share one instance.
    val bakeViewModel: BakeViewModel = viewModel(factory = BakeViewModel.Factory)
    val backStack by navController.currentBackStackEntryAsState()
    val currentTab = Tab.of(backStack?.destination?.route)

    fun openTab(tab: Tab) = navController.navigate(tab.route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        topBar = { if (currentTab != Tab.Bake) TimerBar(bakeViewModel, onClick = { openTab(Tab.Bake) }) },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = { openTab(tab) },
                        icon = { Text(tab.emoji, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Bake.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Tab.Bake.route) {
                BakeHome(
                    viewModel = bakeViewModel,
                    onBakeEnded = { navController.navigate(Routes.bakeDetail(it)) },
                )
            }

            composable(Tab.Schedules.route) {
                SchedulesScreen(bakeViewModel, onEditTemplate = { navController.navigate(Routes.template(it)) })
            }
            composable(Routes.TEMPLATE, arguments = listOf(navArgument("templateId") { type = NavType.LongType })) {
                TemplateEditorScreen(
                    onBack = { navController.popBackStack() },
                    onEditStep = { templateId, stepId, phase -> navController.navigate(Routes.step(templateId, stepId, phase)) },
                    onOpenTemplate = { id ->
                        navController.navigate(Routes.template(id)) { popUpTo(Routes.TEMPLATE) { inclusive = true } }
                    },
                )
            }
            composable(
                Routes.STEP,
                arguments = listOf(
                    navArgument("templateId") { type = NavType.LongType },
                    navArgument("stepId") { type = NavType.LongType },
                    navArgument("phase") { type = NavType.StringType; defaultValue = Phase.POST_MIX.name },
                ),
            ) {
                StepEditorScreen(onBack = { navController.popBackStack() })
            }

            composable(Tab.Log.route) {
                BakeLogScreen(onOpenBake = { navController.navigate(Routes.bakeDetail(it)) })
            }
            composable(Routes.BAKE_DETAIL, arguments = listOf(navArgument("bakeId") { type = NavType.LongType })) {
                BakeDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(Tab.Games.route) { GamesHub(onOpen = { navController.navigate(it.route) }) }
            composable(Game.Clicker.route) { ClickerScreen(onBack = { navController.popBackStack() }) }
            composable(Game.Trivia.route) { TriviaScreen(onBack = { navController.popBackStack() }) }
            composable(Game.WordSearch.route) { WordSearchScreen(onBack = { navController.popBackStack() }) }
            composable(Game.Scramble.route) { ScrambleScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

/** A slim "current step · time left" bar on every tab but Bake, while a bake is running. */
@Composable
private fun TimerBar(viewModel: BakeViewModel, onClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bake = state.bake ?: return
    val now = rememberNow()
    val status = BakeStatus.of(bake, state.steps, now) ?: return
    val remaining = status.remaining(now)

    Surface(onClick = onClick, color = MaterialTheme.colorScheme.primaryContainer) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            StepIcon(status.step.name, size = 24.dp, modifier = Modifier.padding(end = 10.dp))
            Text(status.step.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                if (remaining == null) "Check now" else "${status.nextLabel} in ${formatCountdown(remaining)}",
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
