package com.planday.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.planday.app.data.local.model.ReminderEntity
import com.planday.app.data.local.model.ReminderStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE taskId = :taskId")
    suspend fun getRemindersForTask(taskId: String): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE status = 'SCHEDULED' AND triggerInstantUtc >= :fromInstant ORDER BY triggerInstantUtc ASC")
    suspend fun getActiveScheduledReminders(fromInstant: Instant): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET status = :status WHERE id = :id")
    suspend fun updateReminderStatus(id: String, status: ReminderStatus)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersForTask(taskId: String)
}
