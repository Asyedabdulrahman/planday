package com.planday.app.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planday.app.core.designsystem.theme.DarkBackground
import com.planday.app.core.designsystem.theme.DarkBorder
import com.planday.app.core.designsystem.theme.DarkSurface
import com.planday.app.core.designsystem.theme.DarkTextPrimary
import com.planday.app.core.designsystem.theme.DarkTextSecondary
import com.planday.app.core.util.DateTimeUtils
import com.planday.app.data.local.model.TaskEntity
import com.planday.app.data.local.model.TaskPriority
import com.planday.app.data.local.model.TaskStatus
import java.time.LocalDate

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToHealth: () -> Unit,
    onNavigateToCapture: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TodayUiEffect.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = effect.message,
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        effect.undoAction()
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top Header: Date + Health Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = DateTimeUtils.formatPrettyDate(state.selectedDate),
                        style = MaterialTheme.typography.headlineLarge,
                        color = DarkTextPrimary
                    )
                    Text(
                        text = "${state.totalPlannedMinutes}m planned · ${state.totalCapacityMin}m capacity",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTextSecondary
                    )
                }

                IconButton(
                    onClick = onNavigateToHealth,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Reminder Health",
                        tint = DarkTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Discrete Load Bar
            LoadBar(percentage = state.loadPercentage)

            Spacer(modifier = Modifier.height(18.dp))

            // Quick-Add Input field
            OutlinedTextField(
                value = state.quickAddText,
                onValueChange = viewModel::onQuickAddTextChanged,
                placeholder = {
                    Text(
                        "Quick add (e.g. DSA tomorrow 9pm 60m)",
                        color = DarkTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { viewModel.submitQuickAdd() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkTextPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = DarkTextPrimary,
                    unfocusedTextColor = DarkTextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Task Sections
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: NOW (Highlighted primary task)
                state.nowTask?.let { nowTask ->
                    item {
                        SectionHeader("NOW")
                        Spacer(modifier = Modifier.height(8.dp))
                        TaskItemCard(
                            task = nowTask,
                            isNow = true,
                            onComplete = { viewModel.markTaskDone(nowTask) },
                            onReschedule = { viewModel.rescheduleTask(nowTask.id, state.selectedDate.plusDays(1)) }
                        )
                    }
                }

                // Section: NEXT
                if (state.nextTasks.isNotEmpty()) {
                    item {
                        SectionHeader("NEXT")
                    }
                    items(state.nextTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            isNow = false,
                            onComplete = { viewModel.markTaskDone(task) },
                            onReschedule = { viewModel.rescheduleTask(task.id, state.selectedDate.plusDays(1)) }
                        )
                    }
                }

                // Section: LATER
                if (state.laterTasks.isNotEmpty()) {
                    item {
                        SectionHeader("LATER")
                    }
                    items(state.laterTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            isNow = false,
                            onComplete = { viewModel.markTaskDone(task) },
                            onReschedule = { viewModel.rescheduleTask(task.id, state.selectedDate.plusDays(1)) }
                        )
                    }
                }

                // Section: DONE
                if (state.doneTasks.isNotEmpty()) {
                    item {
                        SectionHeader("DONE (${state.doneTasks.size})")
                    }
                    items(state.doneTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            isNow = false,
                            onComplete = {},
                            onReschedule = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = DarkTextSecondary,
        letterSpacing = 1.5.sp
    )
}

@Composable
fun LoadBar(percentage: Int) {
    val clamped = percentage.coerceIn(0, 100) / 100f
    val label = if (percentage > 100) "Full ($percentage%)" else "$percentage%"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DAY LOAD",
                style = MaterialTheme.typography.labelSmall,
                color = DarkTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (percentage > 100) DarkTextPrimary else DarkTextSecondary,
                fontWeight = if (percentage > 100) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DarkBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
                    .height(4.dp)
                    .background(DarkTextPrimary)
            )
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    isNow: Boolean,
    onComplete: () -> Unit,
    onReschedule: () -> Unit
) {
    val isDone = task.status == TaskStatus.DONE
    val borderWidth = if (isNow) 2.dp else 1.dp
    val borderColor = if (isNow) DarkTextPrimary else DarkBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Checkbox Circle
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, if (isDone) DarkTextPrimary else DarkTextSecondary, CircleShape)
                    .background(if (isDone) DarkTextPrimary else Color.Transparent)
                    .clickable { onComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = DarkBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Task Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isNow) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isDone) DarkTextSecondary else DarkTextPrimary
                )

                // Sub-row: Time, Estimate, Priority
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    task.time?.let {
                        Text(
                            text = DateTimeUtils.formatTime(it),
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    task.estimateMin?.let {
                        Text(
                            text = "${it}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (task.priority == TaskPriority.P1) {
                        Text(
                            text = "P1",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                    }
                }
            }

            // Quick Reschedule action
            if (!isDone) {
                IconButton(onClick = onReschedule) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Reschedule",
                        tint = DarkTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
