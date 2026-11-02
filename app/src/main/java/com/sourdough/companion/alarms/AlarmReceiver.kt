package com.sourdough.companion.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fired by AlarmManager for each bake reminder, even with the screen off or the app closed.
 * Timers are system alarms, never in-app countdowns.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // TODO(phase 1): post a lock-screen notification for the bake step in the intent extras.
    }
}
