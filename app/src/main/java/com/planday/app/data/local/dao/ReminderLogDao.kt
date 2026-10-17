package com.planday.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.planday.app.data.local.model.ReminderLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReminderLogEntity)

    @Query("SELECT * FROM reminder_logs ORDER BY timestampUtc DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<ReminderLogEntity>>

    @Query("SELECT * FROM reminder_logs WHERE event = 'MISSED' ORDER BY timestampUtc DESC")
    fun getMissedLogs(): Flow<List<ReminderLogEntity>>
}
