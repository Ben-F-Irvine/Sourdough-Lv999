package com.sourdough.engine.temperature

data class CurvePoint(val tempC: Double, val durationMin: Int)

/** Room temperature → bulk (or starter) duration, linearly interpolated between the user's points. */
class TempCurve(points: List<CurvePoint>) {
    private val points = points.sortedBy { it.tempC }

    init {
        require(this.points.isNotEmpty()) { "A curve needs at least one point" }
    }

    /** Outside the user's range the nearest end point is used rather than extrapolating. */
    fun durationMinAt(tempC: Double): Double {
        if (tempC <= points.first().tempC) return points.first().durationMin.toDouble()
        if (tempC >= points.last().tempC) return points.last().durationMin.toDouble()
        val hi = points.indexOfFirst { it.tempC >= tempC }
        val a = points[hi - 1]
        val b = points[hi]
        val t = (tempC - a.tempC) / (b.tempC - a.tempC)
        return a.durationMin + t * (b.durationMin - a.durationMin)
    }

    /** Fraction of bulk (0..1) completed by [minutes] spent at [tempC]. */
    fun progressFor(minutes: Double, tempC: Double): Double = minutes / durationMinAt(tempC)
}
