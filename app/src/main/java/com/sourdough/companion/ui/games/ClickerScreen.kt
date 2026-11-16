package com.sourdough.companion.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sourdough.companion.ui.components.ScreenHeader
import kotlinx.coroutines.delay

@Composable
fun ClickerScreen(onBack: () -> Unit) {
    val store = rememberGameStore()
    // Bake whatever the ovens made while the game was closed, once, on open.
    val (initial, offline) = remember {
        val saved = store.loadClicker()
        val earned = ClickerEconomy.offlineEarnings(saved, System.currentTimeMillis())
        ClickerEconomy.add(saved, earned) to earned
    }
    var state by remember { mutableStateOf(initial) }
    var taps by remember { mutableIntStateOf(0) }
    val latest by rememberUpdatedState(state)

    // The only loop in the game, and it only runs while this screen is open, unpaused and an oven
    // is actually producing something.
    val running = !state.paused && ClickerEconomy.perSecond(state) > 0
    LaunchedEffect(running) {
        while (running) {
            delay(250)
            state = ClickerEconomy.tick(state, 0.25)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)
            store.saveClicker(latest.copy(savedAt = System.currentTimeMillis()))
        }
    }
    DisposableEffect(Unit) {
        onDispose { store.saveClicker(latest.copy(savedAt = System.currentTimeMillis())) }
    }

    val bounce by animateFloatAsState(if (taps % 2 == 0) 1f else 0.92f, label = "loaf bounce")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { ScreenHeader("Loaf Clicker", onBack) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = {
                    state = state.copy(paused = !state.paused)
                    store.saveClicker(state.copy(savedAt = System.currentTimeMillis()))
                }) { Text(if (state.paused) "▶  Resume" else "⏸  Pause") }
                if (state.paused) {
                    Text(
                        "Paused. Nothing bakes, even while you're away.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (offline >= 1) {
            item {
                Text(
                    "While you were away your ovens baked ${formatLoaves(offline)} loaves",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    formatLoaves(state.loaves),
                    style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("loaves · ${formatLoaves(ClickerEconomy.perSecond(state))} per second", style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(200.dp).scale(bounce).alpha(if (state.paused) 0.4f else 1f).clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !state.paused,
                ) {
                    state = ClickerEconomy.tap(state)
                    taps++
                },
            ) {
                Box(contentAlignment = Alignment.Center) { Text("🍞", fontSize = 110.sp) }
            }
        }
        item {
            Text("+${formatLoaves(ClickerEconomy.perTap(state))} per tap", style = MaterialTheme.typography.labelLarge)
        }
        item {
            ShopRow(
                emoji = "💪",
                name = "Stronger arms",
                detail = "Doubles loaves per tap · level ${state.tapLevel}",
                cost = ClickerEconomy.tapUpgradeCost(state),
                canAfford = !state.paused && state.loaves >= ClickerEconomy.tapUpgradeCost(state),
                onBuy = { state = ClickerEconomy.buyTapUpgrade(state) },
            )
        }
        items(ClickerEconomy.buildings, key = { it.id }) { building ->
            val owned = state.owned[building.id] ?: 0
            val cost = ClickerEconomy.cost(building, owned)
            ShopRow(
                emoji = building.emoji,
                name = "${building.name}${if (owned > 0) " ×$owned" else ""}",
                detail = "+${formatLoaves(building.perSecond)} per second each",
                cost = cost,
                canAfford = !state.paused && state.loaves >= cost,
                onBuy = { state = ClickerEconomy.buy(state, building) },
            )
        }
        item { Box(Modifier.padding(bottom = 16.dp)) }
    }
}

@Composable
private fun ShopRow(emoji: String, name: String, detail: String, cost: Double, canAfford: Boolean, onBuy: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(name, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onBuy, enabled = canAfford) { Text("🍞 ${formatLoaves(cost)}") }
        }
    }
}
