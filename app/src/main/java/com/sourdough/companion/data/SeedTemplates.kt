package com.sourdough.companion.data

import com.sourdough.companion.data.db.AnchorType
import com.sourdough.companion.data.db.TemplateEntity
import com.sourdough.companion.data.db.TemplateStepEntity
import com.sourdough.engine.Phase

/**
 * Starter templates, inserted on first launch; edit them in the Schedules tab. The first one,
 * [coldBulk], becomes the default schedule.
 */
object SeedTemplates {
    /** Room-temperature bulk, then an overnight cold bulk in the fridge; shape, proof and bake in the morning. */
    val coldBulk: Pair<TemplateEntity, List<TemplateStepEntity>> = TemplateEntity(
        name = "Bulk + overnight cold bulk",
        anchorType = AnchorType.FEED,
        notes = "Bulk at room temperature, then into the fridge overnight. Shape, proof and bake in the morning.",
    ) to listOf(
        step("Feed starter", 300, Phase.PRE_MIX, waitForUser = true,
            instructions = "Feed 1:1:1. Ready when doubled, domed and bubbly."),
        step("Autolyse", 60, Phase.PRE_MIX,
            instructions = "Mix flour and water until no dry bits. Cover and rest."),
        step("Mix", 15, Phase.PRE_MIX,
            instructions = "Add levain and salt. Squeeze and fold until fully combined."),
        step("Rest", 60, Phase.POST_MIX,
            instructions = "Cover and leave the dough alone."),
        step("Stretch and fold", 120, Phase.POST_MIX, repeatCount = 4, repeatIntervalMin = 30,
            instructions = "Wet hands. Lift each side and fold over, four turns of the bowl."),
        step("Bulk ferment", 270, Phase.POST_MIX, waitForUser = true, tempAdjusted = true,
            instructions = "Leave covered at room temperature. Ready for the fridge at about 50% rise, with domed edges."),
        step("Cold bulk (fridge)", 720, Phase.POST_MIX, waitForUser = true,
            instructions = "Cover and refrigerate overnight. Tap Done when you're ready to shape in the morning."),
        step("Shape", 20, Phase.POST_MIX,
            instructions = "The dough is cold and easy to handle. Pre-shape, bench rest 20 min, final shape, into the banneton."),
        step("Proof and preheat", 120, Phase.POST_MIX,
            instructions = "Proof at room temperature until puffy and the poke test springs back slowly. " +
                "Preheat the Dutch oven to 250 °C for the last 45 minutes."),
        step("Bake, lid on", 20, Phase.POST_MIX,
            instructions = "Score, load, lid on."),
        step("Bake, lid off", 25, Phase.POST_MIX,
            instructions = "Lid off, drop to 230 °C. Bake until deep brown."),
        step("Cool", 60, Phase.POST_MIX,
            instructions = "On a rack. Don't cut it yet."),
    )

    val all: List<Pair<TemplateEntity, List<TemplateStepEntity>>> = listOf(
        coldBulk,
        TemplateEntity(
            name = "Morning feed, overnight bulk",
            anchorType = AnchorType.FEED,
            notes = "Starter peaks midday, mix afternoon, bulk overnight, shape and bake next morning.",
        ) to listOf(
            step("Feed starter", 300, Phase.PRE_MIX, waitForUser = true,
                instructions = "Feed 1:1:1. Ready when doubled, domed and bubbly."),
            step("Autolyse", 60, Phase.PRE_MIX,
                instructions = "Mix flour and water until no dry bits. Cover and rest."),
            step("Mix", 15, Phase.PRE_MIX,
                instructions = "Add levain and salt. Squeeze and fold until fully combined."),
            step("Rest", 60, Phase.POST_MIX,
                instructions = "Cover and leave the dough alone."),
            step("Stretch and fold", 120, Phase.POST_MIX, repeatCount = 4, repeatIntervalMin = 30,
                instructions = "Wet hands. Lift each side and fold over, four turns of the bowl."),
            step("Bulk ferment", 270, Phase.POST_MIX, waitForUser = true, tempAdjusted = true,
                instructions = "Leave covered. Done at about 50% rise, domed edges, bubbles on the sides."),
            step("Shape", 20, Phase.POST_MIX,
                instructions = "Pre-shape, bench rest 15 min, final shape, into the banneton."),
            step("Proof and preheat", 60, Phase.POST_MIX,
                instructions = "Dutch oven in, preheat to 250 °C while the loaf proofs."),
            step("Bake, lid on", 20, Phase.POST_MIX,
                instructions = "Score, load, lid on."),
            step("Bake, lid off", 25, Phase.POST_MIX,
                instructions = "Lid off, drop to 230 °C. Bake until deep brown."),
            step("Cool", 60, Phase.POST_MIX,
                instructions = "On a rack. Don't cut it yet."),
        ),
        // Minute-long steps to check alarms fire with the screen off, without waiting a day.
        TemplateEntity(
            name = "Alarm test (10 min)",
            anchorType = AnchorType.MIX,
            notes = "Tap Done mixing, lock the phone and wait for every alarm.",
        ) to listOf(
            step("Mix", 1, Phase.PRE_MIX, instructions = "Nothing to mix: tap Done mixing."),
            step("Rest", 1, Phase.POST_MIX),
            step("Stretch and fold", 3, Phase.POST_MIX, repeatCount = 3, repeatIntervalMin = 1),
            step("Bulk ferment", 2, Phase.POST_MIX, waitForUser = true,
                instructions = "Wait for the check alarm, then tap Done."),
            step("Shape", 1, Phase.POST_MIX),
            step("Bake", 2, Phase.POST_MIX),
        ),
    )

    private fun step(
        name: String,
        durationMin: Int,
        phase: Phase,
        instructions: String = "",
        repeatCount: Int = 1,
        repeatIntervalMin: Int = 0,
        waitForUser: Boolean = false,
        tempAdjusted: Boolean = false,
    ) = TemplateStepEntity(
        templateId = 0,
        position = 0,
        name = name,
        instructions = instructions,
        durationMin = durationMin,
        repeatCount = repeatCount,
        repeatIntervalMin = repeatIntervalMin,
        waitForUser = waitForUser,
        tempAdjusted = tempAdjusted,
        phase = phase,
    )
}
