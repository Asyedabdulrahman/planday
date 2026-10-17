package com.planday.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.planday.app.data.local.model.DaySettingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface DaySettingsDao {

    @Query("SELECT * FROM day_settings")
    fun getAllDaySettings(): Flow<List<DaySettingsEntity>>

    @Query("SELECT * FROM day_settings WHERE dayOfWeek = :dayOfWeek")
    suspend fun getSettingsForDay(dayOfWeek: DayOfWeek): DaySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: DaySettingsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaults(settings: List<DaySettingsEntity>)
}
