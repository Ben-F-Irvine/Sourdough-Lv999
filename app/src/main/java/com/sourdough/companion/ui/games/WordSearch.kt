package com.sourdough.companion.ui.games

import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

data class Cell(val row: Int, val col: Int)

data class PlacedWord(val word: String, val cells: List<Cell>)

class WordSearchPuzzle(val size: Int, val grid: List<CharArray>, val words: List<PlacedWord>) {
    fun letterAt(cell: Cell): Char = grid[cell.row][cell.col]

    /** The word whose letters are exactly [cells], read either way, if any. */
    fun match(cells: List<Cell>): PlacedWord? =
        words.firstOrNull { it.cells == cells || it.cells == cells.reversed() }
}

object WordSearch {
    val wordPool = listOf(
        "STARTER", "LEVAIN", "CRUMB", "CRUST", "SCORE", "PROOF", "FLOUR", "WATER", "SALT", "GLUTEN",
        "KNEAD", "OVEN", "LOAF", "BAKE", "DOUGH", "YEAST", "RYE", "BOULE", "LAME", "HOOCH",
        "STEAM", "FOLD", "RISE", "SPELT", "BATARD", "COUCHE",
    )

    // Right, down, down-right and up-right: words read forwards, like a printed word search.
    private val directions = listOf(0 to 1, 1 to 0, 1 to 1, -1 to 1)

    fun generate(size: Int = 10, wordCount: Int = 8, random: Random = Random.Default): WordSearchPuzzle {
        val grid = List(size) { CharArray(size) { ' ' } }
        val placed = mutableListOf<PlacedWord>()
        for (word in wordPool.filter { it.length <= size }.shuffled(random)) {
            if (placed.size == wordCount) break
            place(word, grid, size, random)?.let { placed += PlacedWord(word, it) }
        }
        for (row in grid) for (c in row.indices) if (row[c] == ' ') row[c] = 'A' + random.nextInt(26)
        return WordSearchPuzzle(size, grid, placed)
    }

    /** Cells from [start] to [end] if they make a straight line (row, column or diagonal), else null. */
    fun line(start: Cell, end: Cell): List<Cell>? {
        val dr = end.row - start.row
        val dc = end.col - start.col
        if (dr != 0 && dc != 0 && abs(dr) != abs(dc)) return null
        val steps = max(abs(dr), abs(dc))
        val sr = Integer.signum(dr)
        val sc = Integer.signum(dc)
        return (0..steps).map { Cell(start.row + it * sr, start.col + it * sc) }
    }

    private fun place(word: String, grid: List<CharArray>, size: Int, random: Random): List<Cell>? {
        repeat(200) {
            val (dr, dc) = directions[random.nextInt(directions.size)]
            val row = random.nextInt(size)
            val col = random.nextInt(size)
            val cells = word.indices.map { Cell(row + it * dr, col + it * dc) }
            val fits = cells.withIndex().all { (i, cell) ->
                cell.row in 0 until size && cell.col in 0 until size &&
                    grid[cell.row][cell.col].let { it == ' ' || it == word[i] }
            }
            if (fits) {
                cells.forEachIndexed { i, cell -> grid[cell.row][cell.col] = word[i] }
                return cells
            }
        }
        return null
    }
}
