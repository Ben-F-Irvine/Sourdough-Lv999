package com.sourdough.companion.data

import com.sourdough.companion.data.db.FlavourEntity

/** The plan's starter flavours, added on first launch. Ingredients and add-in steps come in phase 5. */
object SeedFlavours {
    val all = listOf(
        FlavourEntity(name = "Plain"),
        FlavourEntity(name = "Jalapeño cheddar", addStepName = "Stretch and fold"),
        FlavourEntity(name = "Olive rosemary", addStepName = "Stretch and fold"),
        FlavourEntity(name = "Cinnamon raisin", addStepName = "Stretch and fold"),
    )
}

object FlavourIcons {
    fun emoji(flavourName: String?): String {
        val name = flavourName?.lowercase() ?: return "🍞"
        return when {
            "jalape" in name || "chili" in name || "pepper" in name -> "🌶️"
            "cheddar" in name || "cheese" in name || "parmesan" in name -> "🧀"
            "olive" in name -> "🫒"
            "rosemary" in name || "herb" in name -> "🌿"
            "cinnamon" in name || "raisin" in name -> "🍇"
            "chocolate" in name || "cocoa" in name -> "🍫"
            "garlic" in name -> "🧄"
            "seed" in name || "sunflower" in name -> "🌻"
            "rye" in name || "spelt" in name || "whole" in name -> "🌾"
            else -> "🍞"
        }
    }
}
