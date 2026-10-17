package com.planday.app.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["triggerInstantUtc", "status"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val triggerInstantUtc: Instant,
    val type: ReminderType = ReminderType.HEADS_UP_NOTIFICATION,
    val snoozeCount: Int = 0,
    val status: ReminderStatus = ReminderStatus.SCHEDULED,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
