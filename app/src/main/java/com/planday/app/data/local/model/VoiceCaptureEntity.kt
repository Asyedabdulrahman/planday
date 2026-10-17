package com.planday.app.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_captures")
data class VoiceCaptureEntity(
    @PrimaryKey
    val id: String,
    val rawTranscript: String,
    val parsedPayloadJson: String? = null,
    val status: String = "PENDING_CONFIRMATION", // PENDING_CONFIRMATION, COMMITTED, REJECTED
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
