package com.planday.app.data.local.db

import androidx.room.TypeConverter
import com.planday.app.data.local.model.ReminderLogEvent
import com.planday.app.data.local.model.ReminderStatus
import com.planday.app.data.local.model.ReminderType
import com.planday.app.data.local.model.TaskPriority
import com.planday.app.data.local.model.TaskStatus
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority): Int = value.level

    @TypeConverter
    fun toTaskPriority(value: Int): TaskPriority = when (value) {
        1 -> TaskPriority.P1
        3 -> TaskPriority.P3
        else -> TaskPriority.P2
    }

    @TypeConverter
    fun fromReminderType(value: ReminderType): String = value.name

    @TypeConverter
    fun toReminderType(value: String): ReminderType = ReminderType.valueOf(value)

    @TypeConverter
    fun fromReminderStatus(value: ReminderStatus): String = value.name

    @TypeConverter
    fun toReminderStatus(value: String): ReminderStatus = ReminderStatus.valueOf(value)

    @TypeConverter
    fun fromReminderLogEvent(value: ReminderLogEvent): String = value.name

    @TypeConverter
    fun toReminderLogEvent(value: String): ReminderLogEvent = ReminderLogEvent.valueOf(value)

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek): Int = value.value

    @TypeConverter
    fun toDayOfWeek(value: Int): DayOfWeek = DayOfWeek.of(value)
}
