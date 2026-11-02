package com.sourdough.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class PlannerTest {
    private val mixedAt = Instant.parse("2026-11-02T14:00:00Z")

    private fun at(hhmm: String) = Instant.parse("2026-11-02T$hhmm:00Z")

    private fun List<PlannedStep>.starts() = map { it.start.toString().substring(11, 16) }

    private val steps = listOf(
        LiveStep(1, StepSpec("Rest", 30, Phase.POST_MIX)),
        LiveStep(2, StepSpec("Folds", 120, Phase.POST_MIX, repeatCount = 4, repeatIntervalMin = 30)),
        LiveStep(3, StepSpec("Bulk", 300, Phase.POST_MIX, waitForUser = true)),
        LiveStep(4, StepSpec("Shape", 20, Phase.POST_MIX)),
        LiveStep(5, StepSpec("Bake", 45, Phase.POST_MIX)),
    )

    @Test
    fun `project sums durations from the anchor`() {
        val plan = Planner.project(at("08:00"), listOf(
            StepSpec("Feed starter", 300, Phase.PRE_MIX),
            StepSpec("Autolyse", 60, Phase.PRE_MIX),
            StepSpec("Mix", 15, Phase.PRE_MIX),
        ))
        assertEquals(listOf("08:00", "13:00", "14:00"), plan.starts())
    }

    @Test
    fun `repeats fire every interval and the step lasts its full duration`() {
        val plan = Planner.plan(mixedAt, steps, now = mixedAt)
        assertEquals(listOf(at("14:30"), at("15:00"), at("15:30"), at("16:00")), plan[1].occurrences)
        assertEquals(listOf("14:00", "14:30", "16:30", "21:30", "21:50"), plan.starts())
    }

    @Test
    fun `alarms stop at the first unfinished wait step`() {
        val alarms = Planner.alarms(Planner.plan(mixedAt, steps, now = mixedAt), now = mixedAt)
        // Rest starts at "now" (the Done mixing tap), so it gets no alarm; Shape and Bake wait for bulk.
        assertEquals(
            listOf(
                AlarmKey(2, 0, AlarmKind.START) to at("14:30"),
                AlarmKey(2, 1, AlarmKind.START) to at("15:00"),
                AlarmKey(2, 2, AlarmKind.START) to at("15:30"),
                AlarmKey(2, 3, AlarmKind.START) to at("16:00"),
                AlarmKey(3, 0, AlarmKind.START) to at("16:30"),
                AlarmKey(3, 0, AlarmKind.CHECK) to at("21:30"),
            ),
            alarms.map { it.key to it.at },
        )
    }

    @Test
    fun `done on a wait step early pulls later steps in`() {
        val updated = Planner.done(steps, 3, now = at("20:00"))
        val plan = Planner.plan(mixedAt, updated, now = at("20:00"))
        assertEquals("20:00", plan.starts()[3])
        // Shape starts the moment Done is tapped, so only Bake needs an alarm.
        val alarms = Planner.alarms(plan, now = at("20:00"))
        assertEquals(listOf(5L), alarms.map { it.key.stepId })
    }

    @Test
    fun `overdue wait step holds later steps at now`() {
        val plan = Planner.plan(mixedAt, steps, now = at("23:00"))
        assertEquals("23:00", plan.starts()[3])
        assertEquals(2, Planner.currentIndex(plan, at("23:00")))
        assertEquals(emptyList<AlarmSpec>(), Planner.alarms(plan, at("23:00")))
    }

    @Test
    fun `plus 15 on an overdue wait step counts from now`() {
        val updated = Planner.extend(mixedAt, steps, 3, now = at("22:00"))
        val plan = Planner.plan(mixedAt, updated, now = at("22:00"))
        assertEquals(at("22:15"), plan[2].end)
        assertEquals(AlarmSpec(AlarmKey(3, 0, AlarmKind.CHECK), at("22:15")), Planner.alarms(plan, at("22:00")).single())
    }

    @Test
    fun `plus 15 on a fixed step shifts everything after it`() {
        val updated = Planner.extend(mixedAt, steps, 1, now = at("14:10"))
        assertEquals("14:45", Planner.plan(mixedAt, updated, at("14:10")).starts()[1])
    }

    @Test
    fun `skipping the current step starts the next one now`() {
        val updated = Planner.skip(mixedAt, steps, 2, now = at("15:10"))
        assertEquals("15:10", Planner.plan(mixedAt, updated, at("15:10")).starts()[2])
    }

    @Test
    fun `skipping a later step removes its time`() {
        val updated = Planner.skip(mixedAt, steps, 4, now = at("14:10"))
        val plan = Planner.plan(mixedAt, updated, at("14:10"))
        assertEquals(plan[3].start, plan[4].start)
    }

    @Test
    fun `fixed steps finish on their own`() {
        val plan = Planner.plan(mixedAt, steps, now = at("15:10"))
        assertEquals(1, Planner.currentIndex(plan, at("15:10")))
    }

    @Test
    fun `bake is over once every step is finished or past`() {
        val finished = steps.fold(steps) { acc, s -> Planner.done(acc, s.id, at("23:00")) }
        assertNull(Planner.currentIndex(Planner.plan(mixedAt, finished, at("23:00")), at("23:00")))
    }
}
