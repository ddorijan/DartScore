package com.example.dartscore.ui.screen.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.theme.*

@Composable
fun LeaguesScreen(onNavigateBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(DarkBackground).safeScreenBottom()) {
        ScreenTopBar(title = "Lige", onNavigateBack = onNavigateBack)
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Uskoro dostupno", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Online lige i ljestvice dolaze uskoro.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
