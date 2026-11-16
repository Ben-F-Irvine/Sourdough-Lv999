package com.sourdough.companion.ui.games

import android.content.Context

/**
 * Saved game progress. Kept in SharedPreferences, apart from the bake database, so games never
 * need a database migration.
 */
class GameStore(context: Context) {
    private val prefs = context.getSharedPreferences("games", Context.MODE_PRIVATE)

    fun loadClicker(): ClickerState = ClickerState(
        loaves = getDouble("clicker_loaves"),
        totalBaked = getDouble("clicker_total"),
        owned = ClickerEconomy.buildings.associate { it.id to prefs.getInt("clicker_owned_${it.id}", 0) },
        tapLevel = prefs.getInt("clicker_tap_level", 0),
        savedAt = prefs.getLong("clicker_saved_at", 0L),
        paused = prefs.getBoolean("clicker_paused", false),
    )

    fun saveClicker(state: ClickerState) {
        prefs.edit().apply {
            putLong("clicker_loaves", state.loaves.toRawBits())
            putLong("clicker_total", state.totalBaked.toRawBits())
            state.owned.forEach { (id, count) -> putInt("clicker_owned_$id", count) }
            putInt("clicker_tap_level", state.tapLevel)
            putLong("clicker_saved_at", state.savedAt)
            putBoolean("clicker_paused", state.paused)
        }.apply()
    }

    // Doubles are stored as their bit pattern; a Float would lose precision past ~16 million loaves.
    private fun getDouble(key: String) = Double.fromBits(prefs.getLong(key, 0.0.toRawBits()))

    fun best(game: String): Int = prefs.getInt("best_$game", 0)

    fun count(key: String): Int = prefs.getInt("count_$key", 0)

    fun increment(key: String): Int = (count(key) + 1).also { prefs.edit().putInt("count_$key", it).apply() }

    /** Saves [score] if it beats the best; returns true when it's a new best. */
    fun recordScore(game: String, score: Int): Boolean {
        if (score <= best(game)) return false
        prefs.edit().putInt("best_$game", score).apply()
        return true
    }
}
