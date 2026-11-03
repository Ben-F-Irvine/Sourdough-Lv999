package com.sourdough.companion.data

import com.sourdough.companion.data.db.AnchorType
import com.sourdough.companion.data.db.TemplateEntity
import com.sourdough.companion.data.db.TemplateStepEntity
import com.sourdough.engine.Phase

/**
 * Hard-coded templates for phase 1, inserted on first launch. Durations are placeholders until
 * the in-app template editor exists (phase 2); change them here to match your own schedule.
 */
object SeedTemplates {
    val all: List<Pair<TemplateEntity, List<TemplateStepEntity>>> = listOf(
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
