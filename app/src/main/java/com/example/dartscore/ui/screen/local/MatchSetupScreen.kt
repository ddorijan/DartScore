package com.example.dartscore.ui.screen.local

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.UserRepository
import com.example.dartscore.model.*
import com.example.dartscore.ui.components.safeScreenEdges
import com.example.dartscore.ui.components.*
import com.example.dartscore.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MatchSetupScreen(
    onNavigateBack: () -> Unit,
    onStartGame: (MatchSettings) -> Unit
) {
    val userRepository = remember { UserRepository() }
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    var playerNames by remember {
        mutableStateOf(listOf("Igrač 1", "Igrač 2"))
    }
    var format by remember { mutableStateOf(MatchFormat.FIRST_TO) }
    var unit by remember { mutableStateOf(MatchUnit.LEGS) }
    var count by remember { mutableIntStateOf(5) }
    var startScore by remember { mutableIntStateOf(501) }
    var inRule by remember { mutableStateOf(InRule.STRAIGHT) }
    var outRule by remember { mutableStateOf(OutRule.DOUBLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) return@LaunchedEffect
        val nickname = userRepository.getCurrentUserDisplayName() ?: return@LaunchedEffect
        playerNames = playerNames.toMutableList().apply { this[0] = nickname }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .safeScreenEdges()
    ) {
        LocalPlayTopBar(onNavigateBack = onNavigateBack, title = "Utakmica")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MatchSetupSection(title = "Igrači") {
                playerNames.forEachIndexed { index, name ->
                    PlayerNameField(
                        label = "Igrač ${index + 1}",
                        value = name,
                        onValueChange = { newName ->
                            playerNames = playerNames.toMutableList().apply {
                                this[index] = newName
                            }
                        },
                        canRemove = playerNames.size > 2,
                        onRemove = {
                            if (playerNames.size > 2) {
                                playerNames = playerNames.toMutableList().apply { removeAt(index) }
                            }
                        }
                    )
                    if (index < playerNames.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                }

                if (playerNames.size < 8) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { playerNames = playerNames + "Igrač ${playerNames.size + 1}" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = GreenAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dodaj igrača (${playerNames.size}/8)")
                    }
                }
            }

            MatchSetupSection(title = "Postavke") {
                MatchSettingsForm(
                    format = format,
                    onFormatChange = { format = it },
                    unit = unit,
                    onUnitChange = { unit = it },
                    count = count,
                    onCountChange = { count = it },
                    startScore = startScore,
                    onStartScoreChange = { startScore = it },
                    inRule = inRule,
                    onInRuleChange = { inRule = it },
                    outRule = outRule,
                    onOutRuleChange = { outRule = it }
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = RedAccent,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val cleanNames = playerNames.map { it.trim() }
                when {
                    cleanNames.any { it.isBlank() } ->
                        errorMessage = "Svi igrači moraju imati ime."
                    cleanNames.distinct().size != cleanNames.size ->
                        errorMessage = "Imena igrača moraju biti jedinstvena."
                    else -> {
                        errorMessage = null
                        onStartGame(
                            MatchSettings(
                                startScore = startScore,
                                playerNames = cleanNames,
                                format = format,
                                unit = unit,
                                count = count,
                                inRule = inRule,
                                outRule = outRule
                            )
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
        ) {
            Text(
                text = "POČNI IGRU",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun PlayerNameField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        MatchSetupTextField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f)
        )
        if (canRemove) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Remove, contentDescription = "Ukloni", tint = RedAccent)
            }
        }
    }
}
