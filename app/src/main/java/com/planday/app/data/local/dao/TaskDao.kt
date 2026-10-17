package com.planday.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.planday.app.data.local.model.TaskEntity
import com.planday.app.data.local.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY date ASC, time ASC, priority ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date AND isDeleted = 0 ORDER BY time ASC, priority ASC")
    fun getTasksByDate(date: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date AND isDeleted = 0")
    suspend fun getTasksByDateSync(date: LocalDate): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE date IS NULL AND isDeleted = 0 ORDER BY createdAtEpochMs DESC")
    fun getInboxTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'NEEDS_NEW_DAY' AND isDeleted = 0 ORDER BY updatedAtEpochMs DESC")
    fun getNeedsNewDayTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id AND isDeleted = 0")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :newStatus, updatedAtEpochMs = :timestamp WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, newStatus: TaskStatus, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET date = :newDate, updatedAtEpochMs = :timestamp WHERE id = :taskId")
    suspend fun updateTaskDate(taskId: String, newDate: LocalDate?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET isDeleted = 1, updatedAtEpochMs = :timestamp WHERE id = :taskId")
    suspend fun softDeleteTask(taskId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM tasks WHERE date < :beforeDate AND status = 'TODO' AND isDeleted = 0")
    suspend fun getOverdueTodoTasks(beforeDate: LocalDate): List<TaskEntity>
}
