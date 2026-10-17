package com.planday.app.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "reminder_logs",
    indices = [
        Index(value = ["reminderId"]),
        Index(value = ["timestampUtc"])
    ]
)
data class ReminderLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reminderId: String,
    val taskId: String,
    val event: ReminderLogEvent,
    val timestampUtc: Instant,
    val batteryOptimizedState: Boolean = false,
    val notes: String? = null
)
