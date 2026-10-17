package com.planday.app.feature.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planday.app.core.alarm.AlarmScheduler
import com.planday.app.core.designsystem.theme.DarkBackground
import com.planday.app.core.designsystem.theme.DarkBorder
import com.planday.app.core.designsystem.theme.DarkSurface
import com.planday.app.core.designsystem.theme.DarkTextPrimary
import com.planday.app.core.designsystem.theme.DarkTextSecondary
import com.planday.app.core.designsystem.theme.PlanDayTheme
import com.planday.app.data.local.dao.ReminderDao
import com.planday.app.data.local.dao.ReminderLogDao
import com.planday.app.data.local.dao.TaskDao
import com.planday.app.data.local.model.ReminderLogEntity
import com.planday.app.data.local.model.ReminderLogEvent
import com.planday.app.data.local.model.ReminderStatus
import com.planday.app.data.local.model.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class AlarmAlertActivity : ComponentActivity() {

    @Inject
    lateinit var reminderDao: ReminderDao

    @Inject
    lateinit var reminderLogDao: ReminderLogDao

    @Inject
    lateinit var taskDao: TaskDao

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and display over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val reminderId = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_ID) ?: ""
        val taskId = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_ID) ?: ""
        val taskTitle = intent.getStringExtra(AlarmScheduler.EXTRA_TASK_TITLE) ?: "Task Due Now"

        startAlarmSoundAndVibration()

        setContent {
            PlanDayTheme(darkTheme = true) {
                AlarmAlertScreen(
                    title = taskTitle,
                    onDone = {
                        stopSoundAndVibration()
                        markTaskDone(taskId, reminderId)
                        finish()
                    },
                    onDismiss = {
                        stopSoundAndVibration()
                        dismissAlarm(reminderId, taskId)
                        finish()
                    }
                )
            }
        }
    }

    private fun startAlarmSoundAndVibration() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }

            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            val pattern = longArrayOf(0, 800, 500, 800, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSoundAndVibration() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun markTaskDone(taskId: String, reminderId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.updateTaskStatus(taskId, TaskStatus.DONE)
            reminderDao.updateReminderStatus(reminderId, ReminderStatus.DISMISSED)
            reminderLogDao.insertLog(
                ReminderLogEntity(
                    reminderId = reminderId,
                    taskId = taskId,
                    event = ReminderLogEvent.ACKNOWLEDGED,
                    timestampUtc = Instant.now(),
                    notes = "Marked done directly from lock screen"
                )
            )
        }
    }

    private fun dismissAlarm(reminderId: String, taskId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            reminderDao.updateReminderStatus(reminderId, ReminderStatus.DISMISSED)
            reminderLogDao.insertLog(
                ReminderLogEntity(
                    reminderId = reminderId,
                    taskId = taskId,
                    event = ReminderLogEvent.ACKNOWLEDGED,
                    timestampUtc = Instant.now(),
                    notes = "Dismissed from lock screen alert"
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSoundAndVibration()
    }
}

@Composable
fun AlarmAlertScreen(
    title: String,
    onDone: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ALARM",
                color = DarkTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = DarkTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkTextPrimary,
                    contentColor = DarkBackground
                )
            ) {
                Text(
                    text = "Mark Done",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkTextSecondary
                )
            ) {
                Text(
                    text = "Dismiss",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
