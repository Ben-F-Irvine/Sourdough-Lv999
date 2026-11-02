package com.sourdough.companion.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Android clears every alarm on reboot, so an active bake's alarms are re-set from bake_steps. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // TODO(phase 1): re-schedule pending bake_steps of the active bake.
    }
}
