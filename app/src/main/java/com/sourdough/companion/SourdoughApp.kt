package com.sourdough.companion

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager

class SourdoughApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val alarms = NotificationChannel(
            CHANNEL_BAKE_ALARMS,
            getString(R.string.channel_bake_alarms),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.channel_bake_alarms_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        // Low importance: shows in the shade and on the lock screen without sound or a pop-up.
        val timer = NotificationChannel(
            CHANNEL_BAKE_TIMER,
            getString(R.string.channel_bake_timer),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.channel_bake_timer_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannels(listOf(alarms, timer))
    }

    companion object {
        const val CHANNEL_BAKE_ALARMS = "bake_alarms"
        const val CHANNEL_BAKE_TIMER = "bake_timer"
    }
}
