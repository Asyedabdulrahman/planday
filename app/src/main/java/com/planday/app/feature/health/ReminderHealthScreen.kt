package com.planday.app.feature.health

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.planday.app.core.designsystem.theme.DarkBackground
import com.planday.app.core.designsystem.theme.DarkBorder
import com.planday.app.core.designsystem.theme.DarkSurface
import com.planday.app.core.designsystem.theme.DarkTextPrimary
import com.planday.app.core.designsystem.theme.DarkTextSecondary

@Composable
fun ReminderHealthScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var healthItems by remember { mutableStateOf(emptyList<HealthStatus>()) }

    fun refresh() {
        healthItems = ReminderHealthChecker.checkHealth(context)
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reminder Health",
                style = MaterialTheme.typography.headlineLarge,
                color = DarkTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurface,
                    contentColor = DarkTextPrimary
                ),
                modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            ) {
                Text("Done", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "PlanDay relies on exact timing. Every item below should be verified so your alarms never get delayed or silently killed.",
            style = MaterialTheme.typography.labelSmall,
            color = DarkTextSecondary,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(healthItems) { item ->
                HealthItemCard(
                    item = item,
                    onFix = {
                        item.fixIntent?.let { intent ->
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun HealthItemCard(
    item: HealthStatus,
    onFix: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status Indicator dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (item.isHealthy) DarkTextPrimary else DarkTextSecondary)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (item.isHealthy) "OK" else "ACTION NEEDED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isHealthy) DarkTextPrimary else DarkTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.labelSmall,
                color = DarkTextSecondary
            )

            if (!item.isHealthy && item.fixIntent != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onFix,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkTextPrimary,
                        contentColor = DarkBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.actionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
