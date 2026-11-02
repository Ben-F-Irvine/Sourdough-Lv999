package com.sourdough.companion

import android.app.Application
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
        val channel = NotificationChannel(
            CHANNEL_BAKE_ALARMS,
            getString(R.string.channel_bake_alarms),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.channel_bake_alarms_description)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_BAKE_ALARMS = "bake_alarms"
    }
}
