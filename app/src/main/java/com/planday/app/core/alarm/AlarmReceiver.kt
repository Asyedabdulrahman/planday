package com.planday.app.core.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.planday.app.MainActivity
import com.planday.app.data.local.dao.ReminderDao
import com.planday.app.data.local.dao.ReminderLogDao
import com.planday.app.data.local.dao.TaskDao
import com.planday.app.data.local.model.ReminderLogEntity
import com.planday.app.data.local.model.ReminderLogEvent
import com.planday.app.data.local.model.ReminderStatus
import com.planday.app.feature.alarm.AlarmAlertActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var reminderLogDao: ReminderLogDao

    @Inject
    lateinit var taskDao: TaskDao

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: return
        val taskId = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task Reminder"

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "planday:alarm_receiver_wakelock"
        )
        wakeLock.acquire(10 * 1000L) // 10 seconds hold

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    AlarmScheduler.ACTION_TRIGGER_REMINDER -> {
                        postHeadsUpNotification(context, reminderId, taskId, taskTitle)
                    }
                    AlarmScheduler.ACTION_TRIGGER_ALARM -> {
                        triggerFullScreenAlarm(context, reminderId, taskId, taskTitle)
                    }
                    AlarmScheduler.ACTION_DISMISS_ALARM -> {
                        dismissAlarm(context, reminderId, taskId)
                    }
                }
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }
    }

    private suspend fun postHeadsUpNotification(
        context: Context,
        reminderId: String,
        taskId: String,
        taskTitle: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AlarmScheduler.CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("PlanDay Reminder")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)

        // Update database and audit log
        reminderDao.updateReminderStatus(reminderId, ReminderStatus.FIRED)
        reminderLogDao.insertLog(
            ReminderLogEntity(
                reminderId = reminderId,
                taskId = taskId,
                event = ReminderLogEvent.FIRED,
                timestampUtc = Instant.now(),
                notes = "Heads-up notification posted"
            )
        )
    }

    private suspend fun triggerFullScreenAlarm(
        context: Context,
        reminderId: String,
        taskId: String,
        taskTitle: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Full Screen Intent to wake up and display lock-screen activity
        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
            putExtra(AlarmScheduler.EXTRA_TASK_TITLE, taskTitle)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmScheduler.ACTION_DISMISS_ALARM
            putExtra(AlarmScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(AlarmScheduler.EXTRA_TASK_ID, taskId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            ("dismiss_" + reminderId).hashCode(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notification = NotificationCompat.Builder(context, AlarmScheduler.CHANNEL_ALARMS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("ALARM: $taskTitle")
            .setContentText("Tap to open or dismiss below")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setSound(alarmSound)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)

        reminderDao.updateReminderStatus(reminderId, ReminderStatus.FIRED)
        reminderLogDao.insertLog(
            ReminderLogEntity(
                reminderId = reminderId,
                taskId = taskId,
                event = ReminderLogEvent.FIRED,
                timestampUtc = Instant.now(),
                notes = "Full-screen alarm dispatched"
            )
        )
    }

    private suspend fun dismissAlarm(context: Context, reminderId: String, taskId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(reminderId.hashCode())

        reminderDao.updateReminderStatus(reminderId, ReminderStatus.DISMISSED)
        reminderLogDao.insertLog(
            ReminderLogEntity(
                reminderId = reminderId,
                taskId = taskId,
                event = ReminderLogEvent.ACKNOWLEDGED,
                timestampUtc = Instant.now(),
                notes = "Dismissed by user action"
            )
        )
    }
}
