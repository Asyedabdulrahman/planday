package com.planday.app.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.planday.app.core.util.DateTimeUtils
import com.planday.app.data.local.dao.ReminderDao
import com.planday.app.data.local.dao.ReminderLogDao
import com.planday.app.data.local.model.ReminderEntity
import com.planday.app.data.local.model.ReminderLogEntity
import com.planday.app.data.local.model.ReminderLogEvent
import com.planday.app.data.local.model.ReminderStatus
import com.planday.app.data.local.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao,
    private val reminderLogDao: ReminderLogDao
) {

    companion object {
        const val CHANNEL_REMINDERS = "channel_planday_reminders"
        const val CHANNEL_ALARMS = "channel_planday_alarms"

        const val ACTION_TRIGGER_REMINDER = "com.planday.app.ACTION_TRIGGER_REMINDER"
        const val ACTION_TRIGGER_ALARM = "com.planday.app.ACTION_TRIGGER_ALARM"
        const val ACTION_DISMISS_ALARM = "com.planday.app.ACTION_DISMISS_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.planday.app.ACTION_SNOOZE_ALARM"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    suspend fun schedule(
        reminderId: String,
        taskId: String,
        taskTitle: String,
        triggerInstant: Instant,
        type: ReminderType
    ) = withContext(Dispatchers.IO) {
        val action = if (type == ReminderType.FULL_SCREEN_ALARM) ACTION_TRIGGER_ALARM else ACTION_TRIGGER_REMINDER

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_TASK_TITLE, taskTitle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerEpochMs = triggerInstant.toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        }

        // Log the schedule event
        reminderLogDao.insertLog(
            ReminderLogEntity(
                reminderId = reminderId,
                taskId = taskId,
                event = ReminderLogEvent.SCHEDULED,
                timestampUtc = Instant.now(),
                notes = "Scheduled for epoch $triggerEpochMs, type $type"
            )
        )
    }

    suspend fun cancel(reminderId: String) = withContext(Dispatchers.IO) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAllActive(reason: ReminderLogEvent = ReminderLogEvent.REBOOT_RESCHEDULED) = withContext(Dispatchers.IO) {
        val now = Instant.now()
        val pendingReminders = reminderDao.getActiveScheduledReminders(fromInstant = now)

        for (reminder in pendingReminders) {
            schedule(
                reminderId = reminder.id,
                taskId = reminder.taskId,
                taskTitle = "PlanDay Reminder", // Refreshed dynamically by receiver
                triggerInstant = reminder.triggerInstantUtc,
                type = reminder.type
            )
            reminderLogDao.insertLog(
                ReminderLogEntity(
                    reminderId = reminder.id,
                    taskId = reminder.taskId,
                    event = reason,
                    timestampUtc = Instant.now(),
                    notes = "Rescheduled successfully by watchdog/receiver"
                )
            )
        }
    }
}
