package com.planday.app.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.planday.app.core.util.DateTimeUtils
import com.planday.app.data.local.model.ReminderType
import com.planday.app.data.local.model.TaskEntity
import com.planday.app.data.local.model.TaskPriority
import com.planday.app.data.local.model.TaskStatus
import com.planday.app.data.repository.TaskRepository
import com.planday.app.domain.parser.QuickAddParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TodayUiState(
    val selectedDate: LocalDate = DateTimeUtils.today(),
    val totalCapacityMin: Int = 150,
    val tasks: List<TaskEntity> = emptyList(),
    val nowTask: TaskEntity? = null,
    val nextTasks: List<TaskEntity> = emptyList(),
    val laterTasks: List<TaskEntity> = emptyList(),
    val doneTasks: List<TaskEntity> = emptyList(),
    val quickAddText: String = "",
    val isLoading: Boolean = false
) {
    val totalPlannedMinutes: Int get() = tasks.filter { it.status == TaskStatus.TODO }.sumOf { it.estimateMin ?: 0 }
    val loadPercentage: Int get() = if (totalCapacityMin > 0) (totalPlannedMinutes * 100) / totalCapacityMin else 0
}

sealed interface TodayUiEffect {
    data class ShowUndoSnackbar(val message: String, val undoAction: () -> Unit) : TodayUiEffect
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<TodayUiEffect>()
    val effects: SharedFlow<TodayUiEffect> = _effects.asSharedFlow()

    init {
        loadTasksForDate(_uiState.value.selectedDate)
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadTasksForDate(date)
    }

    private fun loadTasksForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.getTasksForDate(date).collect { tasks ->
                val todoTasks = tasks.filter { it.status == TaskStatus.TODO }
                val doneTasks = tasks.filter { it.status == TaskStatus.DONE }

                val now = DateTimeUtils.currentTime()
                // Now task: The first open task with a time <= now, or the first P1, or just the first open task
                val nowTask = todoTasks.firstOrNull { it.time != null && it.time <= now }
                    ?: todoTasks.firstOrNull { it.priority == TaskPriority.P1 }
                    ?: todoTasks.firstOrNull()

                val remainingTodos = todoTasks.filter { it.id != nowTask?.id }
                val nextTasks = remainingTodos.take(3)
                val laterTasks = remainingTodos.drop(3)

                _uiState.update { state ->
                    state.copy(
                        tasks = tasks,
                        nowTask = nowTask,
                        nextTasks = nextTasks,
                        laterTasks = laterTasks,
                        doneTasks = doneTasks
                    )
                }
            }
        }
    }

    fun onQuickAddTextChanged(text: String) {
        _uiState.update { it.copy(quickAddText = text) }
    }

    fun submitQuickAdd() {
        val text = _uiState.value.quickAddText
        if (text.isBlank()) return

        val parsed = QuickAddParser.parse(text, baseDate = _uiState.value.selectedDate)

        viewModelScope.launch {
            val created = repository.addTask(
                title = parsed.title,
                date = parsed.date ?: _uiState.value.selectedDate,
                time = parsed.time,
                estimateMin = parsed.estimateMin,
                priority = parsed.priority,
                projectId = null,
                reminderType = if (parsed.time != null) ReminderType.HEADS_UP_NOTIFICATION else null
            )
            _uiState.update { it.copy(quickAddText = "") }

            _effects.emit(
                TodayUiEffect.ShowUndoSnackbar(
                    message = "Added: ${created.title}",
                    undoAction = {
                        viewModelScope.launch { repository.deleteTask(created.id) }
                    }
                )
            )
        }
    }

    fun markTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTaskStatus(task.id, TaskStatus.DONE)
            _effects.emit(
                TodayUiEffect.ShowUndoSnackbar(
                    message = "Completed: ${task.title}",
                    undoAction = {
                        viewModelScope.launch { repository.updateTaskStatus(task.id, TaskStatus.TODO) }
                    }
                )
            )
        }
    }

    fun rescheduleTask(taskId: String, newDate: LocalDate) {
        val originalDate = _uiState.value.selectedDate
        viewModelScope.launch {
            repository.rescheduleTask(taskId, newDate)
            _effects.emit(
                TodayUiEffect.ShowUndoSnackbar(
                    message = "Rescheduled to ${DateTimeUtils.formatPrettyDate(newDate)}",
                    undoAction = {
                        viewModelScope.launch { repository.rescheduleTask(taskId, originalDate) }
                    }
                )
            )
        }
    }
}
