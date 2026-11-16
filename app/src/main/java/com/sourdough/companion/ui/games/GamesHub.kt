package com.sourdough.companion.ui.games

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sourdough.companion.SourdoughApp

enum class Game(val route: String, val emoji: String, val title: String, val blurb: String) {
    Clicker("games/clicker", "🍞", "Loaf Clicker", "Tap the loaf, buy ovens, build a bakery. Keeps baking while you're away."),
    Trivia("games/trivia", "❓", "Bread Trivia", "Ten questions on baking, plus sourdough through history."),
    WordSearch("games/wordsearch", "🔎", "Word Search", "Find the baking words hidden in the grid."),
    Scramble("games/scramble", "🔤", "Dough Scramble", "Unscramble bread words from a clue."),
}

@Composable
fun rememberGameStore(): GameStore {
    val context = LocalContext.current
    return remember { (context.applicationContext as SourdoughApp).container.games }
}

@Composable
fun GamesHub(onOpen: (Game) -> Unit) {
    val store = rememberGameStore()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text("Games", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Something to do while the dough does its thing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(Game.entries) { game ->
            Card(Modifier.fillMaxWidth().clickable { onOpen(game) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(game.emoji, style = MaterialTheme.typography.displaySmall)
                    Column(Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(game.title, style = MaterialTheme.typography.titleMedium)
                        Text(game.blurb, style = MaterialTheme.typography.bodySmall)
                        bestText(game, store)?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

private fun bestText(game: Game, store: GameStore): String? = when (game) {
    Game.Clicker -> store.loadClicker().totalBaked.takeIf { it > 0 }?.let { "${formatLoaves(it)} loaves baked" }
    Game.Trivia -> store.best("trivia").takeIf { it > 0 }?.let { "Best: $it / 10" }
    Game.WordSearch -> store.count("wordsearch").takeIf { it > 0 }?.let { "$it solved" }
    Game.Scramble -> store.best("scramble").takeIf { it > 0 }?.let { "Best: $it / 10" }
}
