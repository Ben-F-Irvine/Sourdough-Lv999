package com.sourdough.companion.data

import com.sourdough.companion.data.db.BakeEntity
import com.sourdough.companion.data.db.BakeStepEntity
import com.sourdough.engine.Phase
import com.sourdough.engine.PlannedStep
import com.sourdough.engine.Planner
import java.time.Duration
import java.time.Instant

/**
 * What the baker needs to know right now: the current step and what the countdown is counting
 * down to. Shared by the live bake screen, the in-app timer bar and the ongoing notification.
 */
data class BakeStatus(
    val plan: List<PlannedStep>,
    val currentIndex: Int,
    val step: BakeStepEntity,
    /** What happens when the countdown ends, e.g. "Stretch and fold 2 of 4", "Dough check", "Shape". */
    val nextLabel: String,
    /** Null when a wait-for-user step is overdue: nothing is scheduled until Done is tapped. */
    val target: Instant?,
    /** When the countdown started, for drawing progress. */
    val countdownFrom: Instant,
) {
    val planned: PlannedStep get() = plan[currentIndex]

    fun remaining(now: Instant): Duration? = target?.let { Duration.between(now, it) }

    /** 0..1 through the current countdown. */
    fun progress(now: Instant): Float {
        val end = target ?: return 1f
        val total = Duration.between(countdownFrom, end).toMillis().coerceAtLeast(1)
        return (Duration.between(countdownFrom, now).toMillis().toFloat() / total).coerceIn(0f, 1f)
    }

    companion object {
        /** Null before Done mixing and once every step is finished. */
        fun of(bake: BakeEntity, steps: List<BakeStepEntity>, now: Instant): BakeStatus? {
            val anchor = bake.mixedAt ?: return null
            val postMix = steps.filter { it.phase == Phase.POST_MIX }
            val plan = Planner.plan(anchor, postMix.map { it.toLiveStep() }, now)
            val index = Planner.currentIndex(plan, now) ?: return null
            val step = postMix[index]
            val planned = plan[index]
            val nextStepName = postMix.getOrNull(index + 1)?.name

            val nextRepeat = planned.occurrences.withIndex().firstOrNull { it.value > now }
            return when {
                step.repeatCount > 1 && nextRepeat != null -> BakeStatus(
                    plan, index, step,
                    nextLabel = "${step.name} ${nextRepeat.index + 1} of ${step.repeatCount}",
                    target = nextRepeat.value,
                    countdownFrom = planned.occurrences.getOrNull(nextRepeat.index - 1) ?: planned.start,
                )
                step.waitForUser -> BakeStatus(
                    plan, index, step,
                    nextLabel = "Dough check",
                    target = planned.end.takeIf { it > now },
                    countdownFrom = planned.start,
                )
                else -> BakeStatus(
                    plan, index, step,
                    nextLabel = nextStepName ?: "Bake complete",
                    target = planned.end,
                    countdownFrom = planned.occurrences.lastOrNull { it <= now } ?: planned.start,
                )
            }
        }
    }
}

/** "1:05:09" with hours, "12:34" without. */
fun formatCountdown(remaining: Duration): String {
    val seconds = remaining.seconds.coerceAtLeast(0)
    val h = seconds / 3600
    val m = seconds % 3600 / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
