package com.planday.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.planday.app.core.designsystem.theme.DarkBackground
import com.planday.app.core.designsystem.theme.DarkBorder
import com.planday.app.core.designsystem.theme.DarkSurface
import com.planday.app.core.designsystem.theme.DarkTextPrimary
import com.planday.app.core.designsystem.theme.DarkTextSecondary
import com.planday.app.core.designsystem.theme.PlanDayTheme
import com.planday.app.feature.capture.CaptureScreen
import com.planday.app.feature.health.ReminderHealthScreen
import com.planday.app.feature.plan.PlanScreen
import com.planday.app.feature.today.TodayScreen
import com.planday.app.feature.today.TodayViewModel
import dagger.hilt.android.AndroidEntryPoint

enum class PlanDayTab {
    TODAY,
    PLAN,
    CAPTURE,
    HEALTH
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlanDayTheme(darkTheme = true) {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    var currentTab by remember { mutableStateOf(PlanDayTab.TODAY) }
    var previousTab by remember { mutableStateOf(PlanDayTab.TODAY) }
    val todayViewModel: TodayViewModel = hiltViewModel()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            if (currentTab != PlanDayTab.HEALTH) {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(1.dp, DarkBorder)
                ) {
                    NavigationBarItem(
                        selected = currentTab == PlanDayTab.TODAY,
                        onClick = { currentTab = PlanDayTab.TODAY },
                        icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                        label = { Text("Today", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkTextPrimary,
                            selectedTextColor = DarkTextPrimary,
                            unselectedIconColor = DarkTextSecondary,
                            unselectedTextColor = DarkTextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == PlanDayTab.PLAN,
                        onClick = { currentTab = PlanDayTab.PLAN },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Plan") },
                        label = { Text("Plan", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkTextPrimary,
                            selectedTextColor = DarkTextPrimary,
                            unselectedIconColor = DarkTextSecondary,
                            unselectedTextColor = DarkTextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == PlanDayTab.CAPTURE,
                        onClick = { currentTab = PlanDayTab.CAPTURE },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkTextPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Capture",
                                    tint = DarkBackground,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        label = { Text("Capture", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkTextPrimary,
                            selectedTextColor = DarkTextPrimary,
                            unselectedIconColor = DarkTextSecondary,
                            unselectedTextColor = DarkTextSecondary,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                PlanDayTab.TODAY -> TodayScreen(
                    viewModel = todayViewModel,
                    onNavigateToHealth = {
                        previousTab = PlanDayTab.TODAY
                        currentTab = PlanDayTab.HEALTH
                    },
                    onNavigateToCapture = { currentTab = PlanDayTab.CAPTURE }
                )
                PlanDayTab.PLAN -> PlanScreen(viewModel = todayViewModel)
                PlanDayTab.CAPTURE -> CaptureScreen(onMicClick = { /* Will hook into voice in M2 */ })
                PlanDayTab.HEALTH -> ReminderHealthScreen(onBack = { currentTab = previousTab })
            }
        }
    }
}
