package com.sourdough.companion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/** Top-level screens from the plan. Each is a placeholder until its build phase. */
enum class Screen(val route: String, val title: String) {
    Home("home", "Home"),
    LiveBake("live_bake", "Live bake"),
    Templates("templates", "Schedule templates"),
    Flavours("flavours", "Flavours"),
    TempCurve("temp_curve", "Temperature curve"),
    Settings("settings", "Settings"),
}

@Composable
fun SourdoughNavHost(navController: NavHostController = rememberNavController()) {
    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Screen.Home.route) { HomePlaceholder(navController) }
            Screen.entries.filter { it != Screen.Home }.forEach { screen ->
                composable(screen.route) { Text(screen.title, Modifier.padding(16.dp)) }
            }
        }
    }
}

@Composable
private fun HomePlaceholder(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Sourdough Companion")
        Screen.entries.filter { it != Screen.Home }.forEach { screen ->
            TextButton(onClick = { navController.navigate(screen.route) }) { Text(screen.title) }
        }
    }
}
