package com.sourdough.companion.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sourdough.companion.ui.components.ScreenHeader
import kotlinx.coroutines.delay

private const val SCRAMBLE_ROUNDS = 10

/** Letters of [word] in an order that isn't the word itself. */
private fun scramble(word: String): List<Char> {
    var letters = word.toList()
    while (letters.joinToString("") == word) letters = letters.shuffled()
    return letters
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrambleScreen(onBack: () -> Unit) {
    val store = rememberGameStore()
    var words by remember { mutableStateOf(Scramble.words.shuffled().take(SCRAMBLE_ROUNDS)) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var newBest by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenHeader("Dough Scramble", onBack)

        if (index >= words.size) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text("🔤", style = MaterialTheme.typography.displayLarge)
                Text("$score / $SCRAMBLE_ROUNDS", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                Text(if (newBest) "New best!" else "Best: ${store.best("scramble")} / $SCRAMBLE_ROUNDS", style = MaterialTheme.typography.titleMedium)
                Button(onClick = {
                    words = Scramble.words.shuffled().take(SCRAMBLE_ROUNDS)
                    index = 0
                    score = 0
                    newBest = false
                }, modifier = Modifier.padding(top = 16.dp)) { Text("Play again") }
            }
            return@Column
        }

        val target = words[index]
        fun advance(correct: Boolean) {
            if (correct) score++
            index++
            if (index >= words.size) newBest = store.recordScore("scramble", score)
        }
        // Keyed on the round so each word starts with fresh tiles.
        ScrambleRound(key = index, word = target, roundLabel = "Word ${index + 1} of $SCRAMBLE_ROUNDS · Score $score", onDone = ::advance)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScrambleRound(key: Int, word: ScrambleWord, roundLabel: String, onDone: (correct: Boolean) -> Unit) {
    val tiles = remember(key) { scramble(word.word) }
    var used by remember(key) { mutableStateOf(listOf<Int>()) }
    val guess = used.map { tiles[it] }.joinToString("")
    val complete = guess.length == word.word.length
    val right = complete && guess == word.word

    LaunchedEffect(key, complete) {
        if (complete) {
            delay(if (right) 700 else 900)
            if (right) onDone(true) else used = emptyList()
        }
    }

    Text(roundLabel, style = MaterialTheme.typography.labelLarge)
    Text("Clue: ${word.clue}", style = MaterialTheme.typography.titleMedium)
    Text(
        guess.padEnd(word.word.length, '_').toList().joinToString(" "),
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        color = when {
            right -> MaterialTheme.colorScheme.primary
            complete -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        },
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.forEachIndexed { i, letter ->
            FilledTonalButton(
                onClick = { if (!complete) used = used + i },
                enabled = i !in used,
                modifier = Modifier.size(56.dp),
            ) { Text(letter.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { used = used.dropLast(1) }, enabled = used.isNotEmpty() && !complete) { Text("Undo") }
        OutlinedButton(onClick = { used = emptyList() }, enabled = used.isNotEmpty() && !complete) { Text("Clear") }
        TextButton(onClick = { onDone(false) }) { Text("Skip") }
    }
}
