package com.planday.app.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val colorHex: String,
    val icon: String? = null,
    val isArchived: Boolean = false,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)
