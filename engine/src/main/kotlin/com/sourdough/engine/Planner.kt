package com.sourdough.engine

import java.time.Duration
import java.time.Instant

enum class StepStatus { PENDING, DONE, SKIPPED }

/** A step of a running bake: its definition plus what the baker has done with it so far. */
data class LiveStep(
    val id: Long,
    val spec: StepSpec,
    val status: StepStatus = StepStatus.PENDING,
    /** When Done or Skip was tapped. Null for a step skipped before it began. */
    val doneAt: Instant? = null,
    /** Minutes added with "+15". */
    val extraMin: Int = 0,
) {
    val isFinished: Boolean get() = status != StepStatus.PENDING
}

data class PlannedStep(
    val step: LiveStep,
    val start: Instant,
    val end: Instant,
    /** When each repeat is due; a single entry (the start) for ordinary steps. */
    val occurrences: List<Instant>,
)

enum class AlarmKind { START, CHECK }

/** Identifies one alarm so it can be cancelled and re-set when the plan changes. */
data class AlarmKey(val stepId: Long, val occurrence: Int, val kind: AlarmKind)

data class AlarmSpec(val key: AlarmKey, val at: Instant)

/**
 * Turns a chain of steps into clock times. Nothing is stored as a clock time: every step starts
 * where the previous one ended, so tapping Done, Skip or +15 re-plans everything after it.
 *
 * Fixed steps finish on their own when their time is up. Wait-for-user steps ("bulk until 50%
 * rise") block the chain: everything after them floats until Done is tapped.
 */
object Planner {
    fun plan(anchor: Instant, steps: List<LiveStep>, now: Instant): List<PlannedStep> {
        var cursor = anchor
        return steps.map { step ->
            val start = cursor
            val end = when {
                step.doneAt != null -> step.doneAt
                step.isFinished -> start
                else -> {
                    val nominal = start.plusMinutes(step.spec.durationMin + step.extraMin)
                    if (step.spec.waitForUser) maxOf(nominal, now) else nominal
                }
            }
            cursor = end
            PlannedStep(step, start, end, occurrences(step.spec, start))
        }
    }

    /** Projected times before anything has happened, e.g. from "Fed starter". */
    fun project(anchor: Instant, steps: List<StepSpec>): List<PlannedStep> =
        plan(anchor, steps.mapIndexed { i, spec -> LiveStep(i.toLong(), spec) }, now = anchor)

    /** The step the baker is on, or null when the bake is over. */
    fun currentIndex(plan: List<PlannedStep>, now: Instant): Int? =
        plan.indexOfFirst { !it.step.isFinished && (it.step.spec.waitForUser || it.end > now) }
            .takeIf { it >= 0 }

    /**
     * Alarms still to fire: each step's start (every repeat), plus a "check the dough" alarm at the
     * target end of a wait-for-user step. Nothing past the first unfinished wait step is set, because
     * those times only become real once Done is tapped.
     */
    fun alarms(plan: List<PlannedStep>, now: Instant): List<AlarmSpec> {
        val alarms = mutableListOf<AlarmSpec>()
        for (p in plan) {
            if (p.step.isFinished) continue
            p.occurrences.forEachIndexed { i, at ->
                if (at > now) alarms += AlarmSpec(AlarmKey(p.step.id, i, AlarmKind.START), at)
            }
            if (p.step.spec.waitForUser) {
                if (p.end > now) alarms += AlarmSpec(AlarmKey(p.step.id, 0, AlarmKind.CHECK), p.end)
                break
            }
        }
        return alarms
    }

    /** Every alarm these steps could ever have set, for cancelling before a re-plan. */
    fun allAlarmKeys(steps: List<LiveStep>): List<AlarmKey> = steps.flatMap { step ->
        (0 until step.spec.repeatCount.coerceAtLeast(1)).map { AlarmKey(step.id, it, AlarmKind.START) } +
            AlarmKey(step.id, 0, AlarmKind.CHECK)
    }

    fun done(steps: List<LiveStep>, stepId: Long, now: Instant): List<LiveStep> =
        steps.update(stepId) { it.copy(status = StepStatus.DONE, doneAt = now) }

    /** Skipping the current step ends it now; skipping a later one removes it from the chain. */
    fun skip(anchor: Instant, steps: List<LiveStep>, stepId: Long, now: Instant): List<LiveStep> {
        val started = plan(anchor, steps, now).first { it.step.id == stepId }.start <= now
        return steps.update(stepId) { it.copy(status = StepStatus.SKIPPED, doneAt = if (started) now else null) }
    }

    /** "+15": push the step's end back. An overdue wait step gets [minutes] from now, not from its old target. */
    fun extend(anchor: Instant, steps: List<LiveStep>, stepId: Long, now: Instant, minutes: Int = 15): List<LiveStep> {
        val planned = plan(anchor, steps, now).first { it.step.id == stepId }
        val nominalEnd = planned.start.plusMinutes(planned.step.spec.durationMin + planned.step.extraMin)
        val overdueMin = Duration.between(nominalEnd, now).toMinutes().coerceAtLeast(0).toInt()
        return steps.update(stepId) { it.copy(extraMin = it.extraMin + overdueMin + minutes) }
    }

    private fun occurrences(spec: StepSpec, start: Instant): List<Instant> =
        if (spec.repeatCount <= 1) {
            listOf(start)
        } else {
            (0 until spec.repeatCount).map { start.plusMinutes(it * spec.repeatIntervalMin) }
        }

    private fun Instant.plusMinutes(minutes: Int): Instant = plus(Duration.ofMinutes(minutes.toLong()))

    private fun List<LiveStep>.update(stepId: Long, change: (LiveStep) -> LiveStep) =
        map { if (it.id == stepId) change(it) else it }
}
