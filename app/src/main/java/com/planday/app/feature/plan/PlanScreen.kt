package com.planday.app.feature.plan

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planday.app.core.designsystem.theme.DarkBackground
import com.planday.app.core.designsystem.theme.DarkBorder
import com.planday.app.core.designsystem.theme.DarkSurface
import com.planday.app.core.designsystem.theme.DarkTextPrimary
import com.planday.app.core.designsystem.theme.DarkTextSecondary
import com.planday.app.core.util.DateTimeUtils
import com.planday.app.data.local.model.TaskEntity
import com.planday.app.feature.today.TaskItemCard
import com.planday.app.feature.today.TodayViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanScreen(
    viewModel: TodayViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val today = DateTimeUtils.today()
    val next7Days = (0..6).map { today.plusDays(it.toLong()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Plan (Week)",
            style = MaterialTheme.typography.headlineLarge,
            color = DarkTextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Week Strip selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(next7Days) { day ->
                val isSelected = day == state.selectedDate
                DayStripItem(
                    date = day,
                    isSelected = isSelected,
                    onClick = { viewModel.onDateSelected(day) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateTimeUtils.formatPrettyDate(state.selectedDate).uppercase(Locale.ENGLISH),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = DarkTextSecondary,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "${state.tasks.size} tasks",
                style = MaterialTheme.typography.labelSmall,
                color = DarkTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks for the selected day in Plan
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks planned for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkTextSecondary
                        )
                    }
                }
            } else {
                items(state.tasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        isNow = false,
                        onComplete = { viewModel.markTaskDone(task) },
                        onReschedule = { viewModel.rescheduleTask(task.id, state.selectedDate.plusDays(1)) }
                    )
                }
            }
        }
    }
}

@Composable
fun DayStripItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
    val dayNum = date.dayOfMonth.toString()

    Box(
        modifier = Modifier
            .width(62.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DarkTextPrimary else DarkSurface)
            .border(1.dp, if (isSelected) DarkTextPrimary else DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) DarkBackground else DarkTextSecondary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dayNum,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) DarkBackground else DarkTextPrimary
            )
        }
    }
}
