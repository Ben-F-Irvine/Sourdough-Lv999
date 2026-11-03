package com.sourdough.companion.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sourdough.companion.ui.bake.BakeHome
import com.sourdough.companion.ui.templates.StepEditorScreen
import com.sourdough.companion.ui.templates.TemplateEditorScreen
import com.sourdough.engine.Phase

/** Top-level screens from the plan. Home runs the bake; the rest are placeholders until their phase. */
enum class Screen(val route: String, val title: String) {
    Home("home", "Home"),
    Flavours("flavours", "Flavours"),
    TempCurve("temp_curve", "Temperature curve"),
    Settings("settings", "Settings"),
}

private object Routes {
    const val TEMPLATE = "template/{templateId}"
    const val STEP = "template/{templateId}/step/{stepId}?phase={phase}"

    fun template(id: Long) = "template/$id"

    /** stepId 0 opens a new step in [phase]. */
    fun step(templateId: Long, stepId: Long, phase: Phase) = "template/$templateId/step/$stepId?phase=$phase"
}

@Composable
fun SourdoughNavHost(navController: NavHostController = rememberNavController()) {
    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Screen.Home.route) {
                BakeHome(
                    onOpen = { navController.navigate(it) },
                    onEditTemplate = { navController.navigate(Routes.template(it)) },
                )
            }
            composable(
                Routes.TEMPLATE,
                arguments = listOf(navArgument("templateId") { type = NavType.LongType }),
            ) {
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
            Screen.entries.filter { it != Screen.Home }.forEach { screen ->
                composable(screen.route) { Text(screen.title, Modifier.padding(16.dp)) }
            }
        }
    }
}
