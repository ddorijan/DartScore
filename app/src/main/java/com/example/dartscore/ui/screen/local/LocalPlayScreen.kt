package com.example.dartscore.ui.screen.local

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.ui.components.safeScreenEdges
import com.example.dartscore.ui.theme.*

@Composable
fun LocalPlayScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMatchSetup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .safeScreenEdges()
    ) {
        LocalPlayTopBar(onNavigateBack = onNavigateBack, title = "Igraj lokalno")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Odaberi način igre",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            LocalModeCard(
                title = "Utakmica",
                subtitle = "Do 8 igrača · Legovi ili setovi · Prilagodljive postavke",
                borderColor = GreenAccent,
                enabled = true,
                onClick = onNavigateToMatchSetup,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Sports,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            )

            LocalModeCard(
                title = "Kriket",
                subtitle = "Uskoro dostupno",
                borderColor = TextHint,
                enabled = false,
                onClick = {},
                icon = { Text(text = "🦗", fontSize = 24.sp) }
            )

            LocalModeCard(
                title = "Killer",
                subtitle = "Uskoro dostupno",
                borderColor = TextHint,
                enabled = false,
                onClick = {},
                icon = { Text(text = "💀", fontSize = 24.sp) }
            )
        }
    }
}

@Composable
fun LocalPlayTopBar(
    onNavigateBack: () -> Unit,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(width = 1.dp, color = TopBarBorder)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Natrag",
                tint = GreenAccent
            )
        }
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun LocalModeCard(
    title: String,
    subtitle: String,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(14.dp))
            .border(1.5.dp, BorderNeutral, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    if (enabled) Color(0xFF1A3320) else DarkCardLight,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) TextPrimary else TextHint,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Text(
            text = "→",
            color = if (enabled) borderColor else TextHint,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
