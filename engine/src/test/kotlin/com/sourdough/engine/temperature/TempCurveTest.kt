package com.sourdough.engine.temperature

import org.junit.Assert.assertEquals
import org.junit.Test

class TempCurveTest {
    private val curve = TempCurve(
        listOf(
            CurvePoint(20.0, 540),
            CurvePoint(22.0, 420),
            CurvePoint(26.0, 300),
        ),
    )

    @Test
    fun `interpolates linearly between points`() {
        assertEquals(480.0, curve.durationMinAt(21.0), 1e-9)
        assertEquals(360.0, curve.durationMinAt(24.0), 1e-9)
    }

    @Test
    fun `clamps outside the user's range`() {
        assertEquals(540.0, curve.durationMinAt(15.0), 1e-9)
        assertEquals(300.0, curve.durationMinAt(30.0), 1e-9)
    }

    @Test
    fun `cold then warm periods add up to full bulk`() {
        // 4.5 h at 20 °C is half of 9 h; 3.5 h at 22 °C is half of 7 h.
        val progress = curve.progressFor(270.0, 20.0) + curve.progressFor(210.0, 22.0)
        assertEquals(1.0, progress, 1e-9)
    }
}
