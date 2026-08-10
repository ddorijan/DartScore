package com.example.dartscore.ui.screen.match

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.FeedRepository
import com.example.dartscore.game.MatchShareFormatter
import com.example.dartscore.model.MatchKeyMoment
import com.example.dartscore.model.MatchStatsDetail
import com.example.dartscore.model.PlayerMatchStats
import com.example.dartscore.model.VisitRecord
import com.example.dartscore.ui.components.safeScreenEdges
import com.example.dartscore.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MatchStatsScreen(
    detail: MatchStatsDetail,
    fromHistory: Boolean,
    onNavigateBack: () -> Unit,
    onRematch: () -> Unit,
    onInviteRematch: () -> Unit,
    feedRepository: FeedRepository = FeedRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var postingToFeed by remember { mutableStateOf(false) }
    var feedStatus by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(feedStatus) {
        feedStatus?.let {
            snackbarHostState.showSnackbar(it)
            feedStatus = null
        }
    }

    fun shareMatch() {
        val shareText = MatchShareFormatter.toShareText(detail)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "DartScore — Statistika meča")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, "Podijeli utakmicu"))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeScreenEdges()
        ) {
            MatchStatsTopBar(
                onNavigateBack = onNavigateBack,
                onShare = ::shareMatch
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "STATISTIKA MEČA",
                color = GreenAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = if (detail.isCompleted) "Utakmica završena" else "Utakmica u tijeku",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (detail.playerNames.size == 2) {
                HeadToHeadWinnerCard(detail)
                Spacer(modifier = Modifier.height(16.dp))
                StatsComparisonTable(
                    left = detail.playerStats[0],
                    right = detail.playerStats[1],
                    leftColor = GreenAccent,
                    rightColor = RedAccent
                )
            } else {
                MultiPlayerStatsSection(detail)
            }

            if (detail.keyMoments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                KeyMomentsSection(detail.keyMoments)
            }

            if (detail.visits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                AllVisitsSection(detail.visits, detail.playerNames)
            }
        }

        if (detail.isCompleted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentUser != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                postingToFeed = true
                                val result = feedRepository.createMatchPost(detail)
                                postingToFeed = false
                                feedStatus = if (result.isSuccess) {
                                    "Objava je dodana na Zid objava."
                                } else {
                                    result.exceptionOrNull()?.localizedMessage
                                        ?: "Objava nije uspjela."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !postingToFeed,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (postingToFeed) "Objavljivanje..." else "Objavi na Zid objava",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val buttonLabel = if (fromHistory) "Pozovi prijatelja na revanš" else "UZVRAT"
                val buttonAction = if (fromHistory) onInviteRematch else onRematch
                Button(
                    onClick = buttonAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (!fromHistory) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = buttonLabel,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
    }
}

private fun formatCheckoutPct(value: Double): String =
    if (value == value.toLong().toDouble()) "${value.toInt()}%" else "%.2f%%".format(value)

@Composable
private fun MatchStatsTopBar(onNavigateBack: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, TopBarBorder)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Natrag", tint = GreenAccent)
        }
        Text(
            text = "DartScore",
            color = GreenAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, "Podijeli", tint = GreenAccent)
        }
    }
}

@Composable
private fun HeadToHeadWinnerCard(detail: MatchStatsDetail) {
    val winnerIdx = detail.winnerIndex
    val leftStats = detail.playerStats.getOrNull(0)
    val rightStats = detail.playerStats.getOrNull(1)
    val leftLegs = detail.legsWon.getOrElse(0) { 0 }
    val rightLegs = detail.legsWon.getOrElse(1) { 0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(DarkCard, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (winnerIdx == 0) GreenAccent else BorderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (winnerIdx == 0) {
            WinnerPill()
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Spacer(modifier = Modifier.height(28.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatarColumn(
                name = detail.playerNames.getOrElse(0) { "?" },
                accent = GreenAccent,
                isWinner = winnerIdx == 0
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = leftLegs.toString(),
                        color = if (winnerIdx == 0) GreenAccent else TextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " : ",
                        color = TextSecondary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rightLegs.toString(),
                        color = if (winnerIdx == 1) GreenAccent else RedAccent,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("LEGOVI", color = TextHint, fontSize = 11.sp, letterSpacing = 1.sp)
            }

            PlayerAvatarColumn(
                name = detail.playerNames.getOrElse(1) { "?" },
                accent = RedAccent,
                isWinner = winnerIdx == 1
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leftStats?.threeDartAverage?.let { "%.2f".format(it) } ?: "—",
                color = GreenAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("AVG", color = TextHint, fontSize = 11.sp)
            Text(
                text = rightStats?.threeDartAverage?.let { "%.2f".format(it) } ?: "—",
                color = RedAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (winnerIdx == 1) {
            Spacer(modifier = Modifier.height(8.dp))
            WinnerPill()
        }
    }
}

@Composable
private fun WinnerPill() {
    Box(
        modifier = Modifier
            .background(GreenAccent.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .border(1.dp, GreenAccent, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text("POBJEDNIK", color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PlayerAvatarColumn(name: String, accent: Color, isWinner: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkCardLight)
                .border(1.dp, if (isWinner) GreenAccent else BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name.uppercase(),
            color = if (isWinner) GreenAccent else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        if (isWinner) {
            Text("Winner", color = GreenAccent, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatsComparisonTable(
    left: PlayerMatchStats,
    right: PlayerMatchStats,
    leftColor: Color,
    rightColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "USPOREDBA STATISTIKE",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(12.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
        ) {
            StatsTableHeader(left.name.uppercase(), right.name.uppercase(), leftColor, rightColor)
            StatsTableRow("PROSJEK (3. STR)", "%.2f".format(left.threeDartAverage), "%.2f".format(right.threeDartAverage))
            StatsTableRow("POSTOTAK ZAVRŠAVANJA", formatCheckoutPct(left.checkoutPercentage), formatCheckoutPct(right.checkoutPercentage))
            StatsTableRow("ZAVRŠENI CHECKOUT", "${left.checkoutsHit}/${left.checkoutAttempts}", "${right.checkoutsHit}/${right.checkoutAttempts}")
            StatsTableRow("NAJVEĆI ZAVRŠETAK", left.highestCheckout.toString(), right.highestCheckout.toString())
            StatsTableRow("NAJVEĆI SCORE", left.highestScore.toString(), right.highestScore.toString())
            StatsTableRow(
                "NAJBOLJI LEG (STR.)",
                left.bestLegDarts?.toString() ?: "—",
                right.bestLegDarts?.toString() ?: "—",
                showDivider = false
            )
        }
    }
}

@Composable
private fun StatsTableHeader(n1: String, n2: String, c1: Color, c2: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardLight)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(n1, color = c1, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.width(80.dp))
        Text(n2, color = c2, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
    }
    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
}

@Composable
private fun StatsTableRow(label: String, left: String, right: String, showDivider: Boolean = true) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(left, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text(label, color = TextHint, fontSize = 9.sp, modifier = Modifier.width(80.dp), textAlign = TextAlign.Center, lineHeight = 11.sp)
            Text(right, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
        if (showDivider) HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
private fun MultiPlayerStatsSection(detail: MatchStatsDetail) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = detail.winnerName.uppercase() + " — POBJEDNIK",
            color = GreenAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Legovi: ${detail.legsScoreLabel()}",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        detail.playerStats.forEach { stats ->
            PlayerStatsCard(stats, stats.playerIndex == detail.winnerIndex)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PlayerStatsCard(stats: PlayerMatchStats, isWinner: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, if (isWinner) GreenAccent else BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = stats.name.uppercase() + if (isWinner) " ★" else "",
            color = if (isWinner) GreenAccent else TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text("Prosjek: ${"%.2f".format(stats.threeDartAverage)}", color = TextSecondary, fontSize = 12.sp)
        Text("Checkout: ${stats.checkoutsHit}/${stats.checkoutAttempts} (${stats.checkoutPercentage.toInt()}%)", color = TextSecondary, fontSize = 12.sp)
        Text("Max score: ${stats.highestScore} · Max checkout: ${stats.highestCheckout}", color = TextHint, fontSize = 11.sp)
    }
}

@Composable
private fun KeyMomentsSection(moments: List<MatchKeyMoment>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "KLJUČNI TRENUTCI",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            moments.take(3).forEach { moment ->
                KeyMomentCard(moment, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun KeyMomentCard(moment: MatchKeyMoment, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(DarkCard, RoundedCornerShape(10.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(moment.title, color = TextHint, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(moment.value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(moment.playerName, color = GreenAccent, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AllVisitsSection(visits: List<VisitRecord>, playerNames: List<String>) {
    var expandedLegs by remember { mutableStateOf(setOf<Int>()) }
    val byLeg = visits.groupBy { it.leg }.toSortedMap()
    val isHeadToHead = playerNames.size == 2

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "SVI VISITI",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        byLeg.forEach { (leg, legVisits) ->
            val expanded = leg in expandedLegs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .background(DarkCard, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Leg $leg", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    TextButton(onClick = {
                        expandedLegs = if (expanded) expandedLegs - leg else expandedLegs + leg
                    }) {
                        Text(if (expanded) "Sakrij" else "Prikaži", color = GreenAccent, fontSize = 12.sp)
                    }
                }
                if (expanded) {
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    if (isHeadToHead) {
                        LegVisitsTwoColumn(
                            legVisits = legVisits,
                            leftName = playerNames[0],
                            rightName = playerNames[1]
                        )
                    } else {
                        legVisits.forEachIndexed { index, visit ->
                            VisitRow(visit, playerNames)
                            if (index < legVisits.lastIndex) {
                                HorizontalDivider(
                                    color = BorderSubtle,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegVisitsTwoColumn(
    legVisits: List<VisitRecord>,
    leftName: String,
    rightName: String
) {
    val leftVisits = legVisits.filter { it.playerIndex == 0 }
    val rightVisits = legVisits.filter { it.playerIndex == 1 }
    val rowCount = maxOf(leftVisits.size, rightVisits.size)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCardLight)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = leftName.uppercase(),
                color = GreenAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(BorderSubtle)
            )
            Text(
                text = rightName.uppercase(),
                color = RedAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

        repeat(rowCount) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisitScoreCell(
                    visit = leftVisits.getOrNull(index),
                    accentColor = GreenAccent,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(BorderSubtle)
                )
                VisitScoreCell(
                    visit = rightVisits.getOrNull(index),
                    accentColor = RedAccent,
                    modifier = Modifier.weight(1f)
                )
            }
            if (index < rowCount - 1) {
                HorizontalDivider(
                    color = BorderSubtle,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun VisitScoreCell(
    visit: VisitRecord?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (visit == null) {
            Text("—", color = TextHint, fontSize = 14.sp)
        } else {
            Text(
                text = when {
                    visit.bust -> "BUST"
                    visit.legWon -> {
                        if (visit.dartsUsed < 3) "${visit.score} ✓ (${visit.dartsUsed})"
                        else "${visit.score} ✓"
                    }
                    else -> visit.score.toString()
                },
                color = when {
                    visit.bust -> RedAccent
                    visit.legWon -> GreenAccent
                    else -> accentColor
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = visit.remainingAfter.toString(),
                color = TextHint,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VisitRow(visit: VisitRecord, playerNames: List<String>) {
    val name = playerNames.getOrElse(visit.playerIndex) { visit.playerName }
    val scoreColor = when {
        visit.bust -> RedAccent
        visit.legWon -> GreenAccent
        else -> TextPrimary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            text = when {
                visit.bust -> "BUST"
                visit.legWon -> "${visit.score} ✓"
                else -> visit.score.toString()
            },
            color = scoreColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = visit.remainingAfter.toString(),
            color = TextHint,
            fontSize = 12.sp,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}
