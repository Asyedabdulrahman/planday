package com.planday.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.planday.app.core.alarm.AlarmScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PlanDayApplication : Application() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // 1. Reminders Channel (Heads-up, high importance)
            val reminderChannel = NotificationChannel(
                AlarmScheduler.CHANNEL_REMINDERS,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "PlanDay task reminders and notifications"
                enableVibration(true)
                enableLights(true)
            }

            // 2. Alarms Channel (Persistent, maximum importance, alarm audio stream)
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                AlarmScheduler.CHANNEL_ALARMS,
                "Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "PlanDay full-screen persistent alarms"
                setSound(alarmSound, audioAttributes)
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, alarmChannel))
        }
    }
}
