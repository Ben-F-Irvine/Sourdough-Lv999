package com.sourdough.companion.alarms

import android.Manifest
import android.app.PendingIntent
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
import com.sourdough.companion.data.BakeStatus
import com.sourdough.companion.data.FlavourIcons
import com.sourdough.companion.data.StepIcons
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The silent, ongoing "time left" notification shown while a bake runs. The countdown is a system
 * chronometer, so it ticks with the app closed; it only needs refreshing when the step changes,
 * which is always when an alarm fires or the plan changes.
 */
class BakeTimerNotifier(private val context: Context) {
    fun show(status: BakeStatus, flavourName: String?) {
        // POST_NOTIFICATIONS only exists from Android 13; before that notifications need no permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val step = status.step
        val builder = NotificationCompat.Builder(context, SourdoughApp.CHANNEL_BAKE_TIMER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${StepIcons.emoji(step.name)} ${step.name}")
            .setSubText("${FlavourIcons.emoji(flavourName)} ${flavourName ?: "Sourdough"}")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openApp())

        val target = status.target
        if (target != null) {
            val at = DateTimeFormatter.ofPattern("HH:mm").format(target.atZone(ZoneId.systemDefault()))
            builder.setContentText("Next: ${status.nextLabel} at $at")
                .setShowWhen(true)
                .setWhen(target.toEpochMilli())
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
        } else {
            builder.setContentText("Check the dough. Tap Done when it's ready.")
                .setShowWhen(false)
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun cancel() = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val NOTIFICATION_ID = 1
    }
}
