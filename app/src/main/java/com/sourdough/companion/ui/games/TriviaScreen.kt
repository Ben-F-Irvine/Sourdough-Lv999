package com.sourdough.companion.ui.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sourdough.companion.ui.components.ScreenHeader

private const val ROUNDS = 10

/** One question with its answers in play order; [correct] indexes into [answers]. */
private data class Round(val question: TriviaQuestion, val answers: List<String>, val correct: Int)

/** [category] null means a mix of everything. */
private fun newGame(category: TriviaCategory?): List<Round> = Trivia.questions(category).shuffled().take(ROUNDS).map { q ->
    val shuffled = q.answers.shuffled()
    Round(q, shuffled, shuffled.indexOf(q.answers[q.correct]))
}

@Composable
fun TriviaScreen(onBack: () -> Unit) {
    val store = rememberGameStore()
    var category by remember { mutableStateOf<TriviaCategory?>(null) }
    var rounds by remember { mutableStateOf(newGame(category)) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var picked by remember { mutableStateOf<Int?>(null) }
    var newBest by remember { mutableStateOf(false) }
    // Mixed keeps the original "trivia" key, so earlier best scores carry over.
    val bestKey = category?.let { "trivia_${it.name.lowercase()}" } ?: "trivia"

    fun restart(newCategory: TriviaCategory?) {
        category = newCategory
        rounds = newGame(newCategory)
        index = 0
        score = 0
        picked = null
        newBest = false
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader("Bread Trivia", onBack)
        // Switching topic starts a fresh round.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = category == null, onClick = { restart(null) }, label = { Text("Mixed") })
            TriviaCategory.entries.forEach { c ->
                FilterChip(selected = category == c, onClick = { restart(c) }, label = { Text(c.label) })
            }
        }

        if (index >= rounds.size) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                Text(if (score >= 8) "🏆" else if (score >= 5) "🥖" else "🍞", style = MaterialTheme.typography.displayLarge)
                Text("$score / $ROUNDS", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (newBest) "New best!" else "Best: ${store.best(bestKey)} / $ROUNDS",
                    style = MaterialTheme.typography.titleMedium,
                )
                Button(onClick = { restart(category) }, modifier = Modifier.padding(top = 16.dp)) { Text("Play again") }
            }
            return@Column
        }

        val round = rounds[index]
        LinearProgressIndicator(progress = { index / ROUNDS.toFloat() }, modifier = Modifier.fillMaxWidth())
        Text("Question ${index + 1} of $ROUNDS · Score $score", style = MaterialTheme.typography.labelLarge)
        Text(round.question.question, style = MaterialTheme.typography.titleLarge)

        round.answers.forEachIndexed { i, answer ->
            val answered = picked != null
            val colors = when {
                answered && i == round.correct -> ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
                answered && i == picked -> ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                )
                else -> ButtonDefaults.outlinedButtonColors()
            }
            OutlinedButton(
                onClick = {
                    if (picked == null) {
                        picked = i
                        if (i == round.correct) score++
                    }
                },
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(answer, modifier = Modifier.padding(vertical = 4.dp)) }
        }

        picked?.let { choice ->
            Text(
                (if (choice == round.correct) "✅ Correct! " else "❌ Not quite. ") + round.question.explanation,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = {
                picked = null
                index++
                if (index >= rounds.size) newBest = store.recordScore(bestKey, score)
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (index + 1 >= rounds.size) "See score" else "Next question")
            }
        }
    }
}
