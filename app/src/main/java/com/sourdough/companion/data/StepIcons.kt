package com.sourdough.companion.data

/**
 * A picture for each step, picked from its name so steps you add later get one too.
 * Order matters: "Proof and preheat" must match preheat before bake, "Stretch and fold" before rest.
 */
object StepIcons {
    fun emoji(stepName: String): String {
        val name = stepName.lowercase()
        return when {
            "starter" in name || "feed" in name -> "🫙"
            "autolyse" in name -> "💧"
            "fold" in name -> "🤲"
            "bulk" in name || "ferment" in name -> "🫧"
            "mix" in name -> "🥣"
            "shape" in name -> "🥖"
            "retard" in name || "fridge" in name || "cold" in name -> "❄️"
            "preheat" in name || "proof" in name -> "🌡️"
            "bake" in name -> "🔥"
            "cool" in name -> "🌬️"
            "rest" in name -> "⏳"
            else -> "🍞"
        }
    }

    /** Baking gets a drawn oven on screen, since there is no oven emoji. Notifications use [emoji]. */
    fun isBake(stepName: String): Boolean = emoji(stepName) == "🔥"
}
