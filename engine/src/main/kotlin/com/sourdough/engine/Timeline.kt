package com.sourdough.engine

import java.time.Duration
import java.time.Instant

data class PlannedStep(val step: StepSpec, val startsAt: Instant)

object Timeline {
    /** Projected start of each step = anchor + sum of the durations before it. */
    fun project(anchor: Instant, steps: List<StepSpec>): List<PlannedStep> {
        var cursor = anchor
        return steps.map { step ->
            PlannedStep(step, cursor).also {
                cursor = cursor.plus(Duration.ofMinutes(step.durationMin.toLong()))
            }
        }
    }

    // TODO(phase 1-2): repeats, wait-for-user steps, Done / +15 / Skip re-planning.
    // TODO(phase 6): backward planning from a target finish time.
}
