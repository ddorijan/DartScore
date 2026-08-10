package com.example.dartscore.ui.screen.local

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.MatchRepository
import com.example.dartscore.game.CheckoutChart
import com.example.dartscore.game.CheckoutDartOptions
import com.example.dartscore.game.LocalGameEngine
import com.example.dartscore.game.MatchStatsCalculator
import com.example.dartscore.game.VisitOutcome
import com.example.dartscore.model.LegVisitEntry
import com.example.dartscore.model.LocalGameState
import com.example.dartscore.model.MatchSettings
import com.example.dartscore.model.MatchStatsDetail
import com.example.dartscore.model.PlayerGameState
import com.example.dartscore.ui.components.safeScreenEdges
import com.example.dartscore.ui.screen.DartboardCanvas
import com.example.dartscore.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun LocalGameScreen(
    settings: MatchSettings,
    onNavigateBack: () -> Unit,
    onMatchFinished: (MatchStatsDetail) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val matchRepository = remember { MatchRepository() }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    var gameState by remember(settings) { mutableStateOf<LocalGameState?>(null) }
    var inputText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var pendingCheckoutScore by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(settings) {
        val matchId = try {
            if (isLoggedIn) matchRepository.createMatch(settings) else null
        } catch (_: Exception) {
            null
        }
        gameState = LocalGameEngine.createInitialState(settings, matchId)
    }

    val state = gameState ?: return

    val leftPlayer = state.players[state.leftPlayerIndex]
    val rightPlayer = state.players[state.rightPlayerIndex]

    fun persistVisit(newState: LocalGameState) {
        val matchId = newState.matchId ?: return
        val visit = newState.matchVisits.lastOrNull() ?: return
        scope.launch {
            try {
                matchRepository.recordVisit(matchId, visit, newState)
                if (newState.isFinished && newState.winnerIndex != null) {
                    matchRepository.completeMatch(
                        matchId = matchId,
                        winnerIndex = newState.winnerIndex,
                        winnerName = newState.players[newState.winnerIndex].name,
                        gameState = newState
                    )
                }
            } catch (_: Exception) {
                // Game continues even if cloud save fails
            }
        }
    }

    var matchCompletedNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(state.isFinished, state.winnerIndex) {
        if (state.isFinished && state.winnerIndex != null && !matchCompletedNavigated) {
            matchCompletedNavigated = true
            onMatchFinished(MatchStatsCalculator.fromGameState(state))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .safeScreenEdges()
    ) {
        GameTopBar(onNavigateBack = onNavigateBack)

        MatchStatusBar(
            modeLabel = settings.modeLabel,
            progressLabel = state.progressLabel,
            playerCount = settings.playerNames.size,
            currentPlayerIndex = state.currentPlayerIndex,
            currentPlayerName = state.currentPlayer.name
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                PlayerPanel(
                    player = leftPlayer,
                    accentColor = GreenAccent,
                    isActive = state.currentPlayerIndex == state.leftPlayerIndex,
                    outRule = settings.outRule,
                    modifier = Modifier.weight(1f)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(120.dp)
                        .padding(top = 4.dp)
                ) {
                    CenterInfoBox(
                        topLabel = "PRVI DO",
                        value = settings.startScore.toString(),
                        bottomLabel = if (settings.unit.name == "LEGS") "LEGOVI" else "SETOVI",
                        bottomValue = state.legsDisplay
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    DartboardCanvas(modifier = Modifier.size(90.dp))
                }

                PlayerPanel(
                    player = rightPlayer,
                    accentColor = RedAccent,
                    isActive = state.currentPlayerIndex == state.rightPlayerIndex,
                    outRule = settings.outRule,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VisitHistoryBox(
                    visits = leftPlayer.visitHistory,
                    accentColor = GreenAccent,
                    modifier = Modifier.weight(1f)
                )
                VisitHistoryBox(
                    visits = rightPlayer.visitHistory,
                    accentColor = RedAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            if (settings.playerNames.size > 2) {
                MultiPlayerIndicator(
                    players = state.players,
                    currentIndex = state.currentPlayerIndex,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            feedback?.let {
                Text(
                    text = it,
                    color = if (it.contains("pobjeđuje") || it.contains("osvojen")) GreenAccent else RedAccent,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }

        ScoreInputSection(
            inputText = inputText,
            inputAccentColor = if (state.players.size == 2 && state.currentPlayerIndex == 1) {
                RedAccent
            } else {
                GreenAccent
            },
            enabled = !state.isFinished,
            onDigit = { digit ->
                if (inputText.length >= 3) return@ScoreInputSection
                val candidate = inputText + digit
                val value = candidate.toIntOrNull() ?: return@ScoreInputSection
                if (value > 180) {
                    feedback = "Neispravan rezultat (max 180)."
                } else {
                    inputText = candidate
                    if (feedback?.contains("max 180") == true) feedback = null
                }
            },
            onBackspace = {
                if (inputText.isNotEmpty()) {
                    inputText = inputText.dropLast(1)
                    if (feedback?.contains("max 180") == true) feedback = null
                }
            },
            onBust = {
                val typed = inputText.toIntOrNull()
                if (typed != null && typed > 180) {
                    feedback = "Neispravan rezultat (max 180)."
                    return@ScoreInputSection
                }
                val newState = LocalGameEngine.bust(state)
                gameState = newState
                persistVisit(newState)
                inputText = ""
                feedback = "Bust!"
            },
            onUndo = {
                gameState = LocalGameEngine.undoLastVisit(state)
                inputText = ""
                feedback = null
            },
            onConfirm = {
                val score = inputText.toIntOrNull()
                if (score == null) {
                    feedback = "Unesi rezultat."
                    return@ScoreInputSection
                }
                if (score > 180) {
                    feedback = "Neispravan rezultat (max 180)."
                    return@ScoreInputSection
                }
                if (CheckoutDartOptions.requiresPrompt(
                        score = score,
                        remainingBefore = state.currentPlayer.remaining,
                        outRule = settings.outRule
                    )
                ) {
                    pendingCheckoutScore = score
                    inputText = ""
                    return@ScoreInputSection
                }
                val result = LocalGameEngine.submitVisit(state, score)
                gameState = result.state
                persistVisit(result.state)
                inputText = ""
                feedback = when (result.outcome) {
                    VisitOutcome.BUST -> "Bust!"
                    VisitOutcome.NO_OPEN -> "Moraš otvoriti s double/master in!"
                    VisitOutcome.INVALID, VisitOutcome.INVALID_SCORE -> "Neispravan rezultat (max 180)."
                    VisitOutcome.LEG_WON -> if (result.state.isFinished) {
                        "${result.state.players[result.state.winnerIndex!!].name} pobjeđuje!"
                    } else {
                        "${result.state.players[result.state.currentPlayerIndex].name} osvaja leg!"
                    }
                    VisitOutcome.SCORED, VisitOutcome.CHECKOUT_PENDING -> null
                }
            }
        )

        pendingCheckoutScore?.let { checkoutScore ->
            CheckoutDartsDialog(
                checkoutScore = checkoutScore,
                playerName = state.currentPlayer.name,
                outRule = settings.outRule,
                onDismiss = { pendingCheckoutScore = null },
                onSelect = { dartsUsed ->
                    val result = LocalGameEngine.confirmCheckout(state, checkoutScore, dartsUsed)
                    gameState = result.state
                    persistVisit(result.state)
                    pendingCheckoutScore = null
                    feedback = if (result.state.isFinished) {
                        "${result.state.players[result.state.winnerIndex!!].name} pobjeđuje!"
                    } else {
                        "${state.currentPlayer.name} osvaja leg!"
                    }
                }
            )
        }
    }
}

@Composable
private fun CheckoutDartsDialog(
    checkoutScore: Int,
    playerName: String,
    outRule: com.example.dartscore.model.OutRule,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = CheckoutDartOptions.optionsFor(outRule)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = {
            Text(
                text = "Checkout $checkoutScore",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Koliko strijela je $playerName koristio za završetak?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { darts ->
                    Button(
                        onClick = { onSelect(darts) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (darts == 1) "1 strijela" else "$darts strijele",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Odustani", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun GameTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(1.dp, TopBarBorder)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Natrag",
                tint = GreenAccent
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dart ", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Score",
                color = GreenAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            Text(" 🎯", fontSize = 14.sp)
        }
        IconButton(onClick = { }) {
            Icon(Icons.Default.Settings, contentDescription = "Postavke", tint = GreenAccent)
        }
    }
}

@Composable
private fun MatchStatusBar(
    modeLabel: String,
    progressLabel: String,
    playerCount: Int,
    currentPlayerIndex: Int,
    currentPlayerName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCardLight, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = modeLabel,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(RedAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("UŽIVO", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = progressLabel,
                color = GreenAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (playerCount == 2) {
                    "Na redu: $currentPlayerName"
                } else {
                    "Igrač ${currentPlayerIndex + 1}/$playerCount · $currentPlayerName"
                },
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun PlayerPanel(
    player: PlayerGameState,
    accentColor: Color,
    isActive: Boolean,
    outRule: com.example.dartscore.model.OutRule,
    modifier: Modifier = Modifier
) {
    val checkoutSuggestions = if (isActive) {
        CheckoutChart.suggestions(player.remaining, outRule)
    } else {
        listOf("---", "---", "---")
    }

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A))
                .then(
                    if (isActive) Modifier.border(2.dp, BorderNeutral, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.name.take(1).uppercase(),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = player.name.uppercase(),
            color = if (isActive) accentColor else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text("3-DART AVG.", color = TextHint, fontSize = 7.sp, letterSpacing = 0.5.sp)
        Text(
            text = String.format("%.2f", player.threeDartAverage),
            color = accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = player.remaining.toString(),
            color = accentColor,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                checkoutSuggestions.forEach { dart ->
                    Text(
                        text = dart,
                        color = if (dart == "---") TextHint else accentColor,
                        fontSize = 10.sp,
                        fontWeight = if (dart == "---") FontWeight.Normal else FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterInfoBox(
    topLabel: String,
    value: String,
    bottomLabel: String,
    bottomValue: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(topLabel, color = TextHint, fontSize = 7.sp, letterSpacing = 0.5.sp)
        Text(value, color = GreenAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        HorizontalDivider(
            color = Color(0xFF333333),
            modifier = Modifier.padding(vertical = 2.dp)
        )
        Text(bottomLabel, color = TextHint, fontSize = 7.sp, letterSpacing = 0.5.sp)
        Text(
            text = bottomValue,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VisitHistoryBox(
    visits: List<LegVisitEntry>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DarkCard, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ZADNJI HITCI", color = TextHint, fontSize = 7.sp, letterSpacing = 0.5.sp)
        visits.takeLast(5).forEach { visit ->
            Text(
                text = when (visit) {
                    is LegVisitEntry.Scored -> visit.points.toString()
                    LegVisitEntry.Bust -> "Bust"
                },
                color = when (visit) {
                    is LegVisitEntry.Scored -> accentColor
                    LegVisitEntry.Bust -> RedAccent
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        repeat((5 - visits.takeLast(5).size).coerceAtLeast(0)) {
            Text("--", color = TextHint, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MultiPlayerIndicator(
    players: List<PlayerGameState>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        players.forEachIndexed { index, player ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (index == currentIndex) Color(0xFF1A3320) else DarkCard,
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        BorderNeutral,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = player.name.take(6),
                    color = if (index == currentIndex) GreenAccent else TextSecondary,
                    fontSize = 8.sp,
                    maxLines = 1
                )
                Text(
                    text = player.remaining.toString(),
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScoreInputSection(
    inputText: String,
    inputAccentColor: Color,
    enabled: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBust: () -> Unit,
    onUndo: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCard, RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (inputText.isEmpty()) "UPIŠI REZULTAT" else inputText,
                color = if (inputText.isEmpty()) TextHint else inputAccentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val keys = listOf(
            listOf("1", "2", "3", "BUST"),
            listOf("4", "5", "6", "⌫"),
            listOf("7", "8", "9", "0")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        label = key,
                        isAccent = key == "BUST",
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (key) {
                                "BUST" -> onBust()
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onUndo,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444))
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PONIŠTI HITAC", fontSize = 9.sp)
            }
            Button(
                onClick = onConfirm,
                enabled = enabled,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
            ) {
                Text("POTVRDI", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("→", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    isAccent: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                when {
                    isAccent -> Color(0xFF3A1010)
                    else -> DarkCard
                },
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                BorderNeutral,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == "⌫") {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Obriši",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = label,
                color = if (isAccent) RedAccent else TextPrimary,
                fontSize = if (isAccent) 11.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
