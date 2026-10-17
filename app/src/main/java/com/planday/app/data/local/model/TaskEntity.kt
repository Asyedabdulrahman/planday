package com.planday.app.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["date", "status"]),
        Index(value = ["projectId"]),
        Index(value = ["updatedAtEpochMs"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val notes: String? = null,
    val date: LocalDate?,
    val time: LocalTime? = null,
    val zoneId: String = "Asia/Kolkata",
    @ColumnInfo(name = "estimate_min")
    val estimateMin: Int? = null,
    @ColumnInfo(name = "actual_min")
    val actualMin: Int? = null,
    val priority: TaskPriority = TaskPriority.P2,
    val projectId: String? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val repeatRule: String? = null,
    val deadlineDate: LocalDate? = null,
    val sourceType: String = "MANUAL",
    val rawVoiceTranscript: String? = null,
    val isDeleted: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
