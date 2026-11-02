package com.sourdough.companion.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.sourdough.companion.MainActivity
import com.sourdough.engine.AlarmKey
import com.sourdough.engine.AlarmSpec

/**
 * Hands bake reminders to Android's AlarmManager. Uses setAlarmClock, the one exact alarm type
 * Doze never delays or batches, so back-to-back steps (bulk check, then shape) both fire on time.
 */
class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** False on Android 12 if the user revoked "Alarms and reminders"; alarms may then run late. */
    fun canScheduleExact(): Boolean = alarmManager.canScheduleExactAlarms()

    fun schedule(alarm: AlarmSpec, title: String, text: String) {
        val intent = intentFor(alarm.key)
            .putExtra(AlarmReceiver.EXTRA_TITLE, title)
            .putExtra(AlarmReceiver.EXTRA_TEXT, text)
        val pending = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerAt = alarm.at.toEpochMilli()
        if (canScheduleExact()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, openAppIntent()), pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(key: AlarmKey) {
        val pending = PendingIntent.getBroadcast(
            context, 0, intentFor(key), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(pending)
        pending.cancel()
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
    )

    // The data URI makes each alarm's PendingIntent distinct, so it can be cancelled on its own.
    private fun intentFor(key: AlarmKey) = Intent(context, AlarmReceiver::class.java)
        .setData(Uri.parse("sourdough://alarm/${key.stepId}/${key.occurrence}/${key.kind}"))
}
