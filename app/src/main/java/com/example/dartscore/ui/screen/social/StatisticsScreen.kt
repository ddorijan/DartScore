package com.example.dartscore.ui.screen.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dartscore.data.MatchRepository
import com.example.dartscore.model.UserStatsSummary
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.theme.*

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onOpenMatch: (String) -> Unit = {},
    matchRepository: MatchRepository = MatchRepository()
) {
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var stats by remember { mutableStateOf<UserStatsSummary?>(null) }
    var matches by remember { mutableStateOf(emptyList<com.example.dartscore.model.MatchHistoryItem>()) }
    var loading by remember { mutableStateOf(true) }

    DisposableEffect(auth) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            loading = false
            return@LaunchedEffect
        }
        loading = true
        stats = matchRepository.getUserStats().getOrNull()
        matches = matchRepository.getMatchHistory(10).getOrNull().orEmpty()
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground).safeScreenBottom()) {
        ScreenTopBar(title = "Statistika", onNavigateBack = onNavigateBack)

        when {
            currentUser == null -> LoginRequiredPlaceholder("Prijavite se za statistiku.", onNavigateToLogin)
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenAccent)
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                stats?.let { StatsSummaryCard(it) }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nedavne utakmice",
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                matches.forEach { match ->
                    MatchHistoryRow(match, onClick = { onOpenMatch(match.id) })
                }
                if (matches.isEmpty()) {
                    Text(
                        text = "Još nema spremljenih utakmica.",
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
