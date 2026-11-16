package com.sourdough.companion.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.sourdough.companion.SourdoughApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The Done and +15 min buttons on a bake reminder. They do what the same buttons on the live bake
 * screen do, but only while the reminder's step is still the current one, so a button on a stale
 * notification does nothing.
 */
class BakeActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bakeId = intent.getLongExtra(AlarmReceiver.EXTRA_BAKE_ID, 0)
        val stepId = intent.getLongExtra(AlarmReceiver.EXTRA_STEP_ID, 0)
        NotificationManagerCompat.from(context).cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))

        val pending = goAsync()
        val repository = (context.applicationContext as SourdoughApp).container.bakeRepository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_DONE -> repository.markDone(bakeId, stepId, onlyIfCurrent = true)
                    ACTION_EXTEND -> repository.addMinutes(bakeId, stepId, onlyIfCurrent = true)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.sourdough.companion.action.DONE"
        const val ACTION_EXTEND = "com.sourdough.companion.action.EXTEND"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
