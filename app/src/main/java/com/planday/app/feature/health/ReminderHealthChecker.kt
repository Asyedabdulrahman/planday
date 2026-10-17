package com.planday.app.feature.health

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

data class HealthStatus(
    val title: String,
    val description: String,
    val isHealthy: Boolean,
    val actionLabel: String,
    val fixIntent: Intent?
)

object ReminderHealthChecker {

    fun checkHealth(context: Context): List<HealthStatus> {
        val list = mutableListOf<HealthStatus>()

        // 1. Notification Permission (Android 13+)
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val notifIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        list.add(
            HealthStatus(
                title = "Notifications",
                description = "Required to post heads-up task reminders.",
                isHealthy = notificationsEnabled,
                actionLabel = "Enable Notifications",
                fixIntent = notifIntent
            )
        )

        // 2. Exact Alarm Permission (Android 12+)
        var exactAlarmAllowed = true
        var exactIntent: Intent? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            exactAlarmAllowed = alarmManager.canScheduleExactAlarms()
            exactIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        list.add(
            HealthStatus(
                title = "Exact Alarms",
                description = "Guarantees alarms fire on the exact minute even in deep sleep.",
                isHealthy = exactAlarmAllowed,
                actionLabel = "Allow Exact Alarms",
                fixIntent = exactIntent
            )
        )

        // 3. Battery Optimization (Doze Exemption)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
        val batteryIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else null

        list.add(
            HealthStatus(
                title = "Battery Unrestricted",
                description = "Prevents Android Doze mode from deferring scheduled alarms.",
                isHealthy = isIgnoringBatteryOptimizations,
                actionLabel = "Disable Optimization",
                fixIntent = batteryIntent
            )
        )

        // 4. OEM Autostart & Background Execution Detection (Xiaomi, OnePlus, Oppo, Vivo, Samsung)
        val manufacturer = Build.MANUFACTURER.lowercase()
        val oemIntent = getOemAutostartIntent(context, manufacturer)
        if (oemIntent != null) {
            list.add(
                HealthStatus(
                    title = "OEM Background Protection (${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }})",
                    description = "Prevents aggressive vendor task-killers from terminating background alarm receivers.",
                    isHealthy = false, // Always prompt user to verify OEM autostart switch
                    actionLabel = "Open OEM Settings",
                    fixIntent = oemIntent
                )
            )
        }

        return list
    }

    private fun getOemAutostartIntent(context: Context, manufacturer: String): Intent? {
        val intent = Intent()
        when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            }
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                intent.setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            }
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                intent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            }
            manufacturer.contains("oneplus") -> {
                intent.setClassName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            }
            else -> return null
        }
        return if (context.packageManager.resolveActivity(intent, 0) != null) intent else null
    }
}
