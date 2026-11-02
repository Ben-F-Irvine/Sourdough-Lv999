package com.sourdough.companion

import android.content.Context
import com.sourdough.companion.data.db.SourdoughDatabase
import com.sourdough.companion.temperature.ManualTemperatureSource
import com.sourdough.engine.temperature.TemperatureSource

/** Hand-rolled dependency container: one app, one phone, no DI framework needed. */
class AppContainer(context: Context) {
    val database: SourdoughDatabase by lazy { SourdoughDatabase.create(context) }

    // Swap for HomeAssistantTemperatureSource / BluetoothTemperatureSource in phase 4.
    val temperatureSource: TemperatureSource by lazy { ManualTemperatureSource() }
}
