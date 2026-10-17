package com.planday.app.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.planday.app.data.local.model.ReminderLogEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootAndRebootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        val reason = when (action) {
            Intent.ACTION_BOOT_COMPLETED -> ReminderLogEvent.REBOOT_RESCHEDULED
            Intent.ACTION_MY_PACKAGE_REPLACED -> ReminderLogEvent.REBOOT_RESCHEDULED
            Intent.ACTION_TIMEZONE_CHANGED -> ReminderLogEvent.TIMEZONE_RESCHEDULED
            Intent.ACTION_TIME_SET -> ReminderLogEvent.TIMEZONE_RESCHEDULED
            else -> ReminderLogEvent.REBOOT_RESCHEDULED
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmScheduler.rescheduleAllActive(reason)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
