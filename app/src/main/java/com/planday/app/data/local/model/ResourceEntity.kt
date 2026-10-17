package com.planday.app.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val url: String? = null,
    val mediaUri: String? = null,
    val type: String = "LINK", // LINK, VIDEO, ARTICLE, IMAGE, NOTE
    val durationMin: Int? = null,
    val isProcessed: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
