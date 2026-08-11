package com.example.dartscore.ui.screen.training

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.TrainingBestScoresStore
import com.example.dartscore.game.CheckoutChart
import com.example.dartscore.game.TrainingEngine
import com.example.dartscore.model.*
import com.example.dartscore.ui.components.DartboardCanvas
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.components.SinglesPointsPicker
import com.example.dartscore.ui.components.TrainingKeypad
import com.example.dartscore.ui.theme.*

@Composable
fun TrainingGameScreen(
    mode: TrainingMode,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val bestScoresStore = remember { TrainingBestScoresStore(context) }
    var gameState by remember(mode) { mutableStateOf(TrainingEngine.initialState(mode)) }
    var inputText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    val latestState by rememberUpdatedState(gameState)

    DisposableEffect(mode) {
        onDispose {
            bestScoresStore.recordIfBetter(latestState)
        }
    }

    fun persistBest(state: TrainingGameState = gameState) {
        bestScoresStore.recordIfBetter(state)
    }

    LaunchedEffect(gameState) {
        feedback = when (val state = gameState) {
            is TrainingGameState.Checkout121 -> state.lastMessage
            is TrainingGameState.RandomCheckout -> state.lastMessage
            is TrainingGameState.Singles -> state.lastMessage
            is TrainingGameState.ScoreTraining -> state.lastMessage
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .safeScreenBottom()
    ) {
        ScreenTopBar(title = mode.title, onNavigateBack = onNavigateBack)

        when (val state = gameState) {
            is TrainingGameState.Checkout121 -> Checkout121Content(state)
            is TrainingGameState.RandomCheckout -> RandomCheckoutContent(state)
            is TrainingGameState.Singles -> SinglesContent(state)
            is TrainingGameState.ScoreTraining -> ScoreTrainingContent(state)
        }

        feedback?.let {
            Text(
                text = it,
                color = if (it.contains("Bust", ignoreCase = true)) RedAccent else TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        when (val state = gameState) {
            is TrainingGameState.Singles -> {
                if (state.isFinished) {
                    FinishedPanel(
                        title = "Trening završen",
                        summary = "${state.totalPoints} / 180 bodova",
                        onRestart = {
                            persistBest(state)
                            gameState = TrainingEngine.initialState(mode)
                            feedback = null
                        },
                        onExit = {
                            persistBest(state)
                            onNavigateBack()
                        }
                    )
                } else {
                    SinglesPointsPicker(enabled = true) { points ->
                        val result = TrainingEngine.submitSinglesRound(state, points)
                        gameState = result.state
                        if (result.outcome == TrainingVisitOutcome.FINISHED) {
                            persistBest(result.state)
                        }
                    }
                }
            }

            else -> {
                TrainingKeypad(
                    inputText = inputText,
                    placeholder = if (mode == TrainingMode.SCORE) "UPIŠI VISIT" else "UPIŠI REZULTAT",
                    enabled = true,
                    showBust = mode != TrainingMode.SCORE,
                    onDigit = { digit ->
                        val next = inputText + digit
                        if (next.length <= 3) inputText = next
                    },
                    onBackspace = { if (inputText.isNotEmpty()) inputText = inputText.dropLast(1) },
                    onBust = {
                        val score = inputText.toIntOrNull() ?: 0
                        val visitScore = if (score > 0) score else 0
                        val result = when (state) {
                            is TrainingGameState.Checkout121,
                            is TrainingGameState.RandomCheckout ->
                                TrainingEngine.submitCheckoutVisit(state, visitScore)
                            is TrainingGameState.ScoreTraining ->
                                TrainingEngine.submitScoreRound(state, 0)
                            else -> null
                        }
                        result?.let {
                            gameState = it.state
                            inputText = ""
                            persistBest(it.state)
                        }
                    },
                    onUndo = {
                        gameState = when (val s = gameState) {
                            is TrainingGameState.Checkout121 -> TrainingEngine.undo121(s)
                            is TrainingGameState.RandomCheckout -> TrainingEngine.undoRandom(s)
                            is TrainingGameState.Singles -> TrainingEngine.undoSingles(s)
                            is TrainingGameState.ScoreTraining -> TrainingEngine.undoScore(s)
                        }
                        inputText = ""
                    },
                    onConfirm = {
                        val score = inputText.toIntOrNull() ?: return@TrainingKeypad
                        val result = when (state) {
                            is TrainingGameState.Checkout121,
                            is TrainingGameState.RandomCheckout ->
                                TrainingEngine.submitCheckoutVisit(state, score)
                            is TrainingGameState.ScoreTraining ->
                                TrainingEngine.submitScoreRound(state, score)
                            else -> null
                        }
                        result?.let {
                            if (it.outcome != TrainingVisitOutcome.INVALID) {
                                gameState = it.state
                                inputText = ""
                                persistBest(it.state)
                            } else {
                                feedback = "Neispravan rezultat (max 180)."
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Checkout121Content(state: TrainingGameState.Checkout121) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatsRow(
            items = listOf(
                "Cilj" to state.target.toString(),
                "Visit" to "${state.visitsUsed}/3",
                "Najviše" to state.highestReached.toString(),
                "Checkouti" to state.successfulCheckouts.toString()
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        RemainingScoreDisplay(state.remaining)
        CheckoutSuggestions(state.remaining)
        Spacer(modifier = Modifier.height(8.dp))
        DartboardCanvas(modifier = Modifier.size(120.dp))
        Text(
            text = "9 lotki · double out · uspjeh +1 · neuspjeh −1",
            color = TextHint,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun RandomCheckoutContent(state: TrainingGameState.RandomCheckout) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rate = if (state.attempts == 0) 0 else (state.successes * 100 / state.attempts)
        StatsRow(
            items = listOf(
                "Pokušaji" to state.attempts.toString(),
                "Uspjesi" to state.successes.toString(),
                "Postotak" to "$rate%"
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text("Checkout cilj", color = TextSecondary, fontSize = 12.sp)
        RemainingScoreDisplay(state.remaining)
        CheckoutSuggestions(state.remaining)
        Spacer(modifier = Modifier.height(8.dp))
        DartboardCanvas(modifier = Modifier.size(120.dp))
        Text(
            text = "3 lotke · double out · novi nasumični cilj svaki pokušaj",
            color = TextHint,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SinglesContent(state: TrainingGameState.Singles) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatsRow(
            items = listOf(
                "Broj" to state.currentNumber.toString(),
                "Runda" to "${state.currentNumber}/20",
                "Bodovi" to "${state.totalPoints}/180"
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ciljaj broj",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Text(
            text = state.currentNumber.toString(),
            color = GreenAccent,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Single = 1 · Double = 2 · Treble = 3",
            color = TextHint,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        DartboardCanvas(modifier = Modifier.size(120.dp))
    }
}

@Composable
private fun ScoreTrainingContent(state: TrainingGameState.ScoreTraining) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatsRow(
            items = listOf(
                "Runde" to state.roundCount.toString(),
                "Ukupno" to state.totalScore.toString(),
                "Prosjek" to "%.1f".format(state.threeDartAverage)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Upiši visit rezultat", color = TextSecondary, fontSize = 12.sp)
        if (state.roundScores.isNotEmpty()) {
            Text(
                text = "Zadnji: ${state.roundScores.last()}",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        DartboardCanvas(modifier = Modifier.size(120.dp))
        Text(
            text = "Ciljaj T20 i visoke segmente · neograničeno rundi",
            color = TextHint,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun StatsRow(items: List<Pair<String, String>>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (label, value) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(DarkCard, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = label, color = TextHint, fontSize = 10.sp)
                Text(text = value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RemainingScoreDisplay(remaining: Int) {
    Text(
        text = remaining.toString(),
        color = TextPrimary,
        fontSize = 56.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CheckoutSuggestions(remaining: Int) {
    val suggestions = CheckoutChart.suggestions(remaining)
    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { dart ->
            Box(
                modifier = Modifier
                    .background(DarkCard, RoundedCornerShape(6.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(text = dart, color = GreenAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FinishedPanel(
    title: String,
    summary: String,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = GreenAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(summary, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                modifier = Modifier.weight(1f)
            ) {
                Text("PONOVI", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f)
            ) {
                Text("IZLAZ", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
