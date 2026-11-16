package com.sourdough.companion.alarms

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sourdough.companion.MainActivity
import com.sourdough.companion.R
import com.sourdough.companion.SourdoughApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fired by AlarmManager for each bake reminder, even with the screen off or the app closed.
 * Timers are system alarms, never in-app countdowns.
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // POST_NOTIFICATIONS only exists from Android 13; before that notifications need no permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
        )
        val notificationId = intent.dataString.hashCode()
        val builder = NotificationCompat.Builder(context, SourdoughApp.CHANNEL_BAKE_ALARMS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(intent.getStringExtra(EXTRA_TITLE))
            .setContentText(intent.getStringExtra(EXTRA_TEXT))
            .setStyle(NotificationCompat.BigTextStyle().bigText(intent.getStringExtra(EXTRA_TEXT)))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openApp)
            .setAutoCancel(true)

        // Done and +15 without opening the app (or unlocking the phone) with doughy hands.
        val bakeId = intent.getLongExtra(EXTRA_BAKE_ID, 0)
        val stepId = intent.getLongExtra(EXTRA_STEP_ID, 0)
        if (bakeId != 0L && stepId != 0L) {
            fun action(action: String, label: String) = builder.addAction(
                0,
                label,
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(context, BakeActionReceiver::class.java)
                        .setAction(action)
                        .setData(intent.data)
                        .putExtra(EXTRA_BAKE_ID, bakeId)
                        .putExtra(EXTRA_STEP_ID, stepId)
                        .putExtra(BakeActionReceiver.EXTRA_NOTIFICATION_ID, notificationId),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            action(BakeActionReceiver.ACTION_DONE, "Done")
            action(BakeActionReceiver.ACTION_EXTEND, "+15 min")
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())

        // The step has changed, so move the ongoing countdown on to the next thing.
        val pending = goAsync()
        val repository = (context.applicationContext as SourdoughApp).container.bakeRepository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.refreshActiveTimer()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_BAKE_ID = "bake_id"
        const val EXTRA_STEP_ID = "step_id"
    }
}
