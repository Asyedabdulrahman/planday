package com.planday.app.feature.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun CaptureScreen(
    onMicClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Voice Capture",
            style = MaterialTheme.typography.headlineLarge,
            color = DarkTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Tap to speak a brain dump of multiple tasks. AI parses into editable cards in M2.",
            style = MaterialTheme.typography.bodyMedium,
            color = DarkTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Giant Mic Button
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .border(2.dp, DarkTextPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onMicClick,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Tap to Record",
                    tint = DarkTextPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}
