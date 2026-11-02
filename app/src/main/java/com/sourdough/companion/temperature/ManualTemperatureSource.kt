package com.sourdough.companion.temperature

import com.sourdough.engine.temperature.TemperatureReading
import com.sourdough.engine.temperature.TemperatureSource
import com.sourdough.engine.temperature.TemperatureSourceType
import java.time.Instant

/** The user types the temperature; the app prompts every couple of hours during bulk. */
class ManualTemperatureSource : TemperatureSource {
    override val type = TemperatureSourceType.MANUAL

    @Volatile
    private var latest: TemperatureReading? = null

    fun enter(tempC: Double, at: Instant = Instant.now()) {
        latest = TemperatureReading(tempC, at, type)
    }

    override suspend fun read(): TemperatureReading? = latest
}
