package com.planday.app.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    indices = [Index(value = ["taskId"])],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String,
    val taskId: String,
    val type: String, // LINK, NOTE, IMAGE, FILE, VOICE_NOTE
    val uriOrPath: String,
    val name: String? = null,
    val sizeBytes: Long = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
