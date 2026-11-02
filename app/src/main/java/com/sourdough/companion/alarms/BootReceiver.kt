package com.sourdough.companion.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sourdough.companion.SourdoughApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android clears every alarm on reboot, and when the app is reinstalled over itself, so the
 * active bake's alarms are set again from bake_steps.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val pending = goAsync()
        val repository = (context.applicationContext as SourdoughApp).container.bakeRepository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.replanActive()
            } finally {
                pending.finish()
            }
        }
    }
}
