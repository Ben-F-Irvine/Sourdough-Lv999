package com.sourdough.companion.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sourdough.companion.ui.bake.BakeHome

/** Top-level screens from the plan. Home runs the bake; the rest are placeholders until their phase. */
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
            composable(Screen.Home.route) { BakeHome(onOpen = { navController.navigate(it) }) }
            Screen.entries.filter { it != Screen.Home }.forEach { screen ->
                composable(screen.route) { Text(screen.title, Modifier.padding(16.dp)) }
            }
        }
    }
}
