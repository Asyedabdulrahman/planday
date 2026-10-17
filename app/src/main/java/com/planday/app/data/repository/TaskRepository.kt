package com.planday.app.data.repository

import com.planday.app.core.alarm.AlarmScheduler
import com.planday.app.core.util.DateTimeUtils
import com.planday.app.data.local.dao.DaySettingsDao
import com.planday.app.data.local.dao.ProjectDao
import com.planday.app.data.local.dao.ReminderDao
import com.planday.app.data.local.dao.ReminderLogDao
import com.planday.app.data.local.dao.TaskDao
import com.planday.app.data.local.model.DaySettingsEntity
import com.planday.app.data.local.model.ProjectEntity
import com.planday.app.data.local.model.ReminderEntity
import com.planday.app.data.local.model.ReminderLogEntity
import com.planday.app.data.local.model.ReminderStatus
import com.planday.app.data.local.model.ReminderType
import com.planday.app.data.local.model.TaskEntity
import com.planday.app.data.local.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val reminderDao: ReminderDao,
    private val reminderLogDao: ReminderLogDao,
    private val projectDao: ProjectDao,
    private val daySettingsDao: DaySettingsDao,
    private val alarmScheduler: AlarmScheduler
) {

    fun getTasksForDate(date: LocalDate): Flow<List<TaskEntity>> = taskDao.getTasksByDate(date)

    fun getInboxTasks(): Flow<List<TaskEntity>> = taskDao.getInboxTasks()

    fun getNeedsNewDayTasks(): Flow<List<TaskEntity>> = taskDao.getNeedsNewDayTasks()

    fun getRecentReminderLogs(): Flow<List<ReminderLogEntity>> = reminderLogDao.getRecentLogs()

    suspend fun addTask(
        title: String,
        date: LocalDate?,
        time: java.time.LocalTime?,
        estimateMin: Int?,
        priority: com.planday.app.data.local.model.TaskPriority,
        projectId: String? = null,
        reminderType: ReminderType? = null
    ): TaskEntity {
        val taskId = UUID.randomUUID().toString()
        val task = TaskEntity(
            id = taskId,
            title = title,
            date = date,
            time = time,
            estimateMin = estimateMin,
            priority = priority,
            projectId = projectId,
            status = TaskStatus.TODO
        )
        taskDao.insertTask(task)

        // Automatically schedule an alarm if a specific date and time is provided
        if (date != null && time != null) {
            val triggerInstant = DateTimeUtils.toUtcInstant(date, time)
            val reminderId = UUID.randomUUID().toString()
            val chosenType = reminderType ?: ReminderType.HEADS_UP_NOTIFICATION

            val reminder = ReminderEntity(
                id = reminderId,
                taskId = taskId,
                triggerInstantUtc = triggerInstant,
                type = chosenType,
                status = ReminderStatus.SCHEDULED
            )
            reminderDao.insertReminder(reminder)
            alarmScheduler.schedule(
                reminderId = reminderId,
                taskId = taskId,
                taskTitle = title,
                triggerInstant = triggerInstant,
                type = chosenType
            )
        }

        return task
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        taskDao.updateTaskStatus(taskId, status)
        if (status == TaskStatus.DONE || status == TaskStatus.SKIPPED) {
            val reminders = reminderDao.getRemindersForTask(taskId)
            for (r in reminders) {
                alarmScheduler.cancel(r.id)
                reminderDao.updateReminderStatus(r.id, ReminderStatus.DISMISSED)
            }
        }
    }

    suspend fun rescheduleTask(taskId: String, newDate: LocalDate) {
        taskDao.updateTaskDate(taskId, newDate)
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.softDeleteTask(taskId)
        val reminders = reminderDao.getRemindersForTask(taskId)
        for (r in reminders) {
            alarmScheduler.cancel(r.id)
        }
    }
}
