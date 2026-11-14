package com.sourdough.companion.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sourdough.companion.ui.components.ScreenHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordSearchScreen(onBack: () -> Unit) {
    val store = rememberGameStore()
    var puzzle by remember { mutableStateOf(WordSearch.generate()) }
    var found by remember { mutableStateOf(setOf<String>()) }
    var dragStart by remember { mutableStateOf<Cell?>(null) }
    var dragEnd by remember { mutableStateOf<Cell?>(null) }
    val solved = found.size == puzzle.words.size

    val selection = dragStart?.let { start -> dragEnd?.let { end -> WordSearch.line(start, end) } }.orEmpty().toSet()
    val foundCells = puzzle.words.filter { it.word in found }.flatMap { it.cells }.toSet()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader("Word Search", onBack)
        Text(
            if (solved) "🎉 All ${puzzle.words.size} found!" else "Drag across a word. ${found.size} of ${puzzle.words.size} found.",
            style = MaterialTheme.typography.bodyMedium,
        )

        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f)) {
            val cellPx = with(LocalDensity.current) { maxWidth.toPx() } / puzzle.size
            fun cellAt(offset: Offset) = Cell(
                (offset.y / cellPx).toInt().coerceIn(0, puzzle.size - 1),
                (offset.x / cellPx).toInt().coerceIn(0, puzzle.size - 1),
            )
            Column(
                Modifier.fillMaxSize().pointerInput(puzzle, cellPx) {
                    detectDragGestures(
                        onDragStart = { dragStart = cellAt(it); dragEnd = dragStart },
                        onDrag = { change, _ -> dragEnd = cellAt(change.position) },
                        onDragEnd = {
                            val cells = dragStart?.let { s -> dragEnd?.let { e -> WordSearch.line(s, e) } }
                            val match = cells?.let(puzzle::match)
                            if (match != null && match.word !in found) {
                                found = found + match.word
                                if (found.size == puzzle.words.size) store.increment("wordsearch")
                            }
                            dragStart = null
                            dragEnd = null
                        },
                        onDragCancel = { dragStart = null; dragEnd = null },
                    )
                },
            ) {
                for (row in 0 until puzzle.size) {
                    Row(Modifier.weight(1f)) {
                        for (col in 0 until puzzle.size) {
                            val cell = Cell(row, col)
                            val background = when (cell) {
                                in selection -> MaterialTheme.colorScheme.primary
                                in foundCells -> MaterialTheme.colorScheme.secondaryContainer
                                else -> Color.Transparent
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f).fillMaxSize().padding(1.dp)
                                    .background(background, RoundedCornerShape(6.dp)),
                            ) {
                                Text(
                                    puzzle.letterAt(cell).toString(),
                                    fontSize = 18.sp,
                                    fontWeight = if (cell in foundCells) FontWeight.Bold else FontWeight.Medium,
                                    color = if (cell in selection) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            puzzle.words.sortedBy { it.word }.forEach { placed ->
                val done = placed.word in found
                Text(
                    placed.word,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Button(onClick = {
            puzzle = WordSearch.generate()
            found = emptySet()
        }, modifier = Modifier.padding(bottom = 16.dp)) { Text(if (solved) "Next puzzle" else "New puzzle") }
    }
}
