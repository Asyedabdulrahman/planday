package com.planday.app.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "day_settings")
data class DaySettingsEntity(
    @PrimaryKey
    val dayOfWeek: DayOfWeek,
    val capacityMin: Int = 150, // Default weekday 150m, weekend 300m
    val protectedBlocksJson: String = "[]" // JSON representation of protected blocks
)
