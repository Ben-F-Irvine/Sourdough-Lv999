package com.sourdough.engine.temperature

import java.time.Instant

enum class TemperatureSourceType { MANUAL, HOME_ASSISTANT, BLUETOOTH }

data class TemperatureReading(val tempC: Double, val readAt: Instant, val source: TemperatureSourceType)

/**
 * The one seam between scheduling and sensors. Start with manual entry; Home Assistant and
 * Bluetooth implementations plug in later without touching the scheduling code.
 */
interface TemperatureSource {
    val type: TemperatureSourceType

    /** Returns null when the sensor can't be reached; callers fall back to the last known reading. */
    suspend fun read(): TemperatureReading?
}
