package com.sourdough.engine

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TimelineTest {
    @Test
    fun `each step starts at anchor plus the durations before it`() {
        val anchor = Instant.parse("2026-11-02T08:00:00Z")
        val steps = listOf(
            StepSpec("Feed starter", 300, Phase.PRE_MIX),
            StepSpec("Autolyse", 60, Phase.PRE_MIX),
            StepSpec("Mix", 15, Phase.POST_MIX),
        )

        val plan = Timeline.project(anchor, steps)

        assertEquals(
            listOf("08:00", "13:00", "14:00"),
            plan.map { it.startsAt.toString().substring(11, 16) },
        )
    }
}
