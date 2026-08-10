package com.example.dartscore.ui.screen.training

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.model.TrainingMode
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.theme.*

@Composable
fun TrainingScreen(
    onNavigateBack: () -> Unit,
    onStartMode: (TrainingMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .safeScreenBottom()
    ) {
        ScreenTopBar(title = "Trening", onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Odaberi trening",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            TrainingMode.entries.forEach { mode ->
                TrainingModeCard(mode = mode, onClick = { onStartMode(mode) })
            }
        }
    }
}

@Composable
private fun TrainingModeCard(
    mode: TrainingMode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(14.dp))
            .border(1.5.dp, BorderNeutral, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(0xFF2A2E15), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                tint = Color(0xFFB8CC3A),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mode.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mode.subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Text(
            text = "→",
            color = GreenAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
