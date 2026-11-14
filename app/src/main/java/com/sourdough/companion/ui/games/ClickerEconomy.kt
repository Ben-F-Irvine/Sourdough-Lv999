package com.sourdough.companion.ui.games

import kotlin.math.pow

/** Something that bakes loaves on its own. Each one bought costs 15% more than the last. */
data class Building(val id: String, val emoji: String, val name: String, val baseCost: Double, val perSecond: Double)

data class ClickerState(
    val loaves: Double = 0.0,
    val totalBaked: Double = 0.0,
    val owned: Map<String, Int> = emptyMap(),
    /** Each level of "Stronger arms" doubles loaves per tap. */
    val tapLevel: Int = 0,
    /** Epoch millis of the last save, for offline earnings. */
    val savedAt: Long = 0L,
    /** Frozen: nothing ticks on screen and nothing is earned while away. */
    val paused: Boolean = false,
)

/** The Loaf Clicker rules, kept free of Android so they can be unit-tested. */
object ClickerEconomy {
    val buildings = listOf(
        Building("starter", "🫙", "Starter jar", 15.0, 0.1),
        Building("bowl", "🥣", "Mixing bowl", 100.0, 1.0),
        Building("banneton", "🧺", "Banneton", 1_100.0, 8.0),
        Building("dutch_oven", "🍲", "Dutch oven", 12_000.0, 47.0),
        Building("deck_oven", "🔥", "Deck oven", 130_000.0, 260.0),
        Building("bakery", "🏪", "Bakery", 1_400_000.0, 1_400.0),
    )

    /** Offline baking is capped so a week away doesn't break the game. */
    const val MAX_OFFLINE_SECONDS = 12 * 60 * 60

    fun cost(building: Building, owned: Int): Double = building.baseCost * 1.15.pow(owned)

    fun perSecond(state: ClickerState): Double =
        buildings.sumOf { it.perSecond * (state.owned[it.id] ?: 0) }

    fun perTap(state: ClickerState): Double = 2.0.pow(state.tapLevel)

    fun tapUpgradeCost(state: ClickerState): Double = 50.0 * 8.0.pow(state.tapLevel)

    fun tap(state: ClickerState): ClickerState = state.earn(perTap(state))

    fun tick(state: ClickerState, seconds: Double): ClickerState = state.earn(perSecond(state) * seconds)

    fun add(state: ClickerState, amount: Double): ClickerState = state.earn(amount)

    /**
     * Loaves baked while the game was closed, up to [MAX_OFFLINE_SECONDS]. Nothing runs in the
     * background: this is worked out from the elapsed time when the game is reopened.
     */
    fun offlineEarnings(state: ClickerState, nowMillis: Long): Double {
        if (state.savedAt == 0L || state.paused) return 0.0
        val seconds = ((nowMillis - state.savedAt) / 1000).coerceIn(0, MAX_OFFLINE_SECONDS.toLong())
        return perSecond(state) * seconds
    }

    fun buy(state: ClickerState, building: Building): ClickerState {
        val owned = state.owned[building.id] ?: 0
        val price = cost(building, owned)
        if (state.loaves < price) return state
        return state.copy(loaves = state.loaves - price, owned = state.owned + (building.id to owned + 1))
    }

    fun buyTapUpgrade(state: ClickerState): ClickerState {
        val price = tapUpgradeCost(state)
        if (state.loaves < price) return state
        return state.copy(loaves = state.loaves - price, tapLevel = state.tapLevel + 1)
    }

    private fun ClickerState.earn(amount: Double) = copy(loaves = loaves + amount, totalBaked = totalBaked + amount)
}

/** 950 → "950", 12_345 → "12.3K", 4_500_000 → "4.50M". */
fun formatLoaves(value: Double): String = when {
    value < 1_000 -> if (value < 10) "%.1f".format(value) else "%.0f".format(value)
    value < 1_000_000 -> "%.1fK".format(value / 1_000)
    value < 1_000_000_000 -> "%.2fM".format(value / 1_000_000)
    else -> "%.2fB".format(value / 1_000_000_000)
}
