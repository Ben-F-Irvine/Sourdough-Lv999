package com.sourdough.companion.ui.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ClickerEconomyTest {
    private val jar = ClickerEconomy.buildings.first { it.id == "starter" }

    @Test
    fun `buying costs 15 percent more each time`() {
        assertEquals(15.0, ClickerEconomy.cost(jar, 0), 1e-9)
        assertEquals(15.0 * 1.15, ClickerEconomy.cost(jar, 1), 1e-9)
    }

    @Test
    fun `can't buy without enough loaves`() {
        val broke = ClickerState(loaves = 10.0)
        assertEquals(broke, ClickerEconomy.buy(broke, jar))
        val bought = ClickerEconomy.buy(ClickerState(loaves = 20.0), jar)
        assertEquals(1, bought.owned["starter"])
        assertEquals(5.0, bought.loaves, 1e-9)
    }

    @Test
    fun `offline earnings are capped at 12 hours`() {
        val state = ClickerState(owned = mapOf("bowl" to 1), savedAt = 1_000L)
        val aWeekLater = 1_000L + 7 * 24 * 3600 * 1000L
        assertEquals(12 * 3600.0, ClickerEconomy.offlineEarnings(state, aWeekLater), 1e-6)
    }

    @Test
    fun `nothing is earned while paused`() {
        val state = ClickerState(owned = mapOf("bowl" to 3), savedAt = 1_000L, paused = true)
        assertEquals(0.0, ClickerEconomy.offlineEarnings(state, 1_000L + 3_600_000L), 1e-9)
    }

    @Test
    fun `tap upgrades double each tap`() {
        assertEquals(1.0, ClickerEconomy.perTap(ClickerState()), 1e-9)
        assertEquals(4.0, ClickerEconomy.perTap(ClickerState(tapLevel = 2)), 1e-9)
    }

    @Test
    fun `big numbers are shortened`() {
        assertEquals("950", formatLoaves(950.0))
        assertEquals("12.3K", formatLoaves(12_345.0))
        assertEquals("4.50M", formatLoaves(4_500_000.0))
    }
}

class WordSearchTest {
    @Test
    fun `every placed word reads correctly from the grid`() {
        repeat(50) { seed ->
            val puzzle = WordSearch.generate(random = Random(seed))
            assertEquals(8, puzzle.words.size)
            puzzle.words.forEach { placed ->
                assertEquals(placed.word, placed.cells.map(puzzle::letterAt).joinToString(""))
                assertTrue(placed.cells.all { it.row in 0 until puzzle.size && it.col in 0 until puzzle.size })
            }
            assertTrue(puzzle.grid.all { row -> row.all { it in 'A'..'Z' } })
        }
    }

    @Test
    fun `a word matches when selected backwards`() {
        val puzzle = WordSearch.generate(random = Random(1))
        val word = puzzle.words.first()
        assertNotNull(puzzle.match(word.cells.reversed()))
    }

    @Test
    fun `lines must be straight`() {
        assertEquals(4, WordSearch.line(Cell(0, 0), Cell(3, 3))?.size)
        assertEquals(listOf(Cell(2, 1), Cell(1, 1), Cell(0, 1)), WordSearch.line(Cell(2, 1), Cell(0, 1)))
        assertNull(WordSearch.line(Cell(0, 0), Cell(1, 2)))
    }
}

class TriviaContentTest {
    @Test
    fun `every question has four distinct answers and a valid correct index`() {
        Trivia.questions.forEach { q ->
            assertEquals(q.question, 4, q.answers.toSet().size)
            assertTrue(q.question, q.correct in q.answers.indices)
        }
        // Each topic needs enough questions for a full ten-question round.
        TriviaCategory.entries.forEach { assertTrue(it.name, Trivia.questions(it).size >= 10) }
    }

    @Test
    fun `no question is asked twice`() {
        // Compare loosely so the same question with different quote marks still counts as a repeat.
        fun key(q: TriviaQuestion) = q.question.lowercase().filter { it.isLetterOrDigit() }
        val duplicates = Trivia.questions.groupBy(::key).filterValues { it.size > 1 }.keys
        assertEquals(emptySet<String>(), duplicates)
    }

    @Test
    fun `bank counts`() {
        println("TRIVIA total=${Trivia.questions.size} " +
            TriviaCategory.entries.joinToString(" ") { "${it.name}=${Trivia.questions(it).size}" })
    }

    @Test
    fun `scramble words are long enough to scramble`() {
        Scramble.words.forEach { assertTrue(it.word, it.word.toSet().size > 1) }
        assertTrue(Scramble.words.size >= 10)
    }
}
