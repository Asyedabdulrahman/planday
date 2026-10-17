package com.planday.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.planday.app.data.local.dao.DaySettingsDao
import com.planday.app.data.local.dao.ProjectDao
import com.planday.app.data.local.dao.ReminderDao
import com.planday.app.data.local.dao.ReminderLogDao
import com.planday.app.data.local.dao.TaskDao
import com.planday.app.data.local.model.AttachmentEntity
import com.planday.app.data.local.model.DaySettingsEntity
import com.planday.app.data.local.model.ProjectEntity
import com.planday.app.data.local.model.ReminderEntity
import com.planday.app.data.local.model.ReminderLogEntity
import com.planday.app.data.local.model.ResourceEntity
import com.planday.app.data.local.model.TaskEntity
import com.planday.app.data.local.model.VoiceCaptureEntity

@Database(
    entities = [
        TaskEntity::class,
        ReminderEntity::class,
        ReminderLogEntity::class,
        ProjectEntity::class,
        DaySettingsEntity::class,
        AttachmentEntity::class,
        ResourceEntity::class,
        VoiceCaptureEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PlanDayDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun reminderLogDao(): ReminderLogDao
    abstract fun projectDao(): ProjectDao
    abstract fun daySettingsDao(): DaySettingsDao
}
