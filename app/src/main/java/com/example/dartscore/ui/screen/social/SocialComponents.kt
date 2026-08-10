package com.example.dartscore.ui.screen.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.model.ActivityItem
import com.example.dartscore.model.MatchHistoryItem
import com.example.dartscore.model.UserStatsSummary
import com.example.dartscore.ui.theme.*

@Composable
fun LoginRequiredPlaceholder(
    message: String,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToLogin,
            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
        ) {
            Text("Prijava", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatsSummaryCard(stats: UserStatsSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(DarkCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text("3-dart prosjek", color = TextSecondary, fontSize = 12.sp)
        Text(
            text = "%.1f".format(stats.threeDartAverage),
            color = GreenAccent,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("Utakmice", stats.matchesPlayed.toString(), Modifier.weight(1f))
            StatChip("Pobjede", stats.matchesWon.toString(), Modifier.weight(1f))
            StatChip("Online", stats.onlineRecord, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip("Visiti", stats.totalVisits.toString(), Modifier.weight(1f))
            StatChip("Max checkout", stats.highestCheckout.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkCardLight, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = TextHint, fontSize = 10.sp)
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActivityItemRow(activity: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activity.userName.take(1).uppercase(),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activity.userName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " ${activity.actionText}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Text(text = activity.timeAgo, color = TextHint, fontSize = 11.sp)
            if (activity.detail.isNotEmpty() || activity.score.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCardLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column {
                        if (activity.detail.isNotEmpty()) {
                            Text(
                                text = activity.detail,
                                color = TextHint,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        if (activity.score.isNotEmpty()) {
                            Text(
                                text = activity.score,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            if (activity.detail.isEmpty() && activity.score.isEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
        Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun MatchHistoryList(
    matches: List<MatchHistoryItem>,
    emptyMessage: String,
    onMatchClick: (MatchHistoryItem) -> Unit = {}
) {
    if (matches.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(matches, key = { it.id }) { match ->
                MatchHistoryRow(match, onClick = { onMatchClick(match) })
                HorizontalDivider(
                    color = Color(0xFF2A2A2A),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun MatchHistoryRow(
    match: MatchHistoryItem,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = match.playerNames.joinToString(" vs "),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${match.startScore} · ${match.status.replace('_', ' ')}",
            color = TextSecondary,
            fontSize = 12.sp
        )
        if (match.legsWon.isNotEmpty()) {
            Text(
                text = "Legovi: ${match.legsWon.joinToString(" | ")}",
                color = TextHint,
                fontSize = 11.sp
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            match.winnerName?.let {
                Text("Pobjednik: $it", color = GreenAccent, fontSize = 12.sp)
            }
            Text(
                text = "Avg: ${"%.1f".format(match.threeDartAverage)}",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ActivitiesFeedList(
    activities: List<ActivityItem>,
    emptyMessage: String,
    modifier: Modifier = Modifier
) {
    if (activities.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    } else {
        Column(modifier = modifier) {
            activities.forEachIndexed { index, activity ->
                ActivityItemRow(activity = activity)
                if (index < activities.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFF2A2A2A),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}
