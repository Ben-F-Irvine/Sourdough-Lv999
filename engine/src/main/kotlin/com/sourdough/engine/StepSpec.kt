package com.sourdough.engine

/** Which anchor event a step hangs off: "Fed starter" (projections) or "Done mixing" (live alarms). */
enum class Phase { PRE_MIX, POST_MIX }

/**
 * One step of a bake as the engine sees it. Durations are offsets from the previous step,
 * never clock times; that is what lets one tap re-plan everything after it.
 */
data class StepSpec(
    val name: String,
    /** Length of the whole step, including every repeat. */
    val durationMin: Int,
    val phase: Phase,
    /**
     * e.g. 4 stretch-and-folds every 30 min is one step with repeatCount = 4, repeatIntervalMin = 30
     * and durationMin = 120: folds at +0, +30, +60 and +90, next step at +120.
     */
    val repeatCount: Int = 1,
    val repeatIntervalMin: Int = 0,
    /** "Until you say so" steps get a check-in reminder, but the next step waits for Done. */
    val waitForUser: Boolean = false,
    /** Bulk (and optionally the starter) is driven by the temperature curve, not a fixed duration. */
    val tempAdjusted: Boolean = false,
)
