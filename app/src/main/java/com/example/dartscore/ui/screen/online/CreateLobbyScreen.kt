package com.example.dartscore.ui.screen.online

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.LobbyRepository
import com.example.dartscore.model.*
import com.example.dartscore.ui.components.MatchNumberStepper
import com.example.dartscore.ui.components.MatchSetupSection
import com.example.dartscore.ui.components.MatchSettingsForm
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CreateLobbyScreen(
    onNavigateBack: () -> Unit,
    onLobbyCreated: (String) -> Unit,
    lobbyRepository: LobbyRepository = LobbyRepository()
) {
    val scope = rememberCoroutineScope()
    var minAvg by remember { mutableIntStateOf(45) }
    var maxAvg by remember { mutableIntStateOf(55) }
    var format by remember { mutableStateOf(MatchFormat.FIRST_TO) }
    var unit by remember { mutableStateOf(MatchUnit.LEGS) }
    var count by remember { mutableIntStateOf(5) }
    var startScore by remember { mutableIntStateOf(501) }
    var inRule by remember { mutableStateOf(InRule.STRAIGHT) }
    var outRule by remember { mutableStateOf(OutRule.DOUBLE) }
    var lobbyCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .safeScreenBottom()
    ) {
        ScreenTopBar(title = "Novi lobby", onNavigateBack = onNavigateBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MatchSetupSection(title = "Postavke utakmice") {
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

            MatchSetupSection(title = "Kod lobija (opcionalno)") {
                OutlinedTextField(
                    value = lobbyCode,
                    onValueChange = {
                        if (it.length <= 12) lobbyCode = it.uppercase().filter { c -> c.isLetterOrDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Npr. DART42", color = TextHint) },
                    label = { Text("Kod za prijatelje") },
                    supportingText = {
                        Text(
                            "4–12 znakova, slova i brojevi. Ostavi prazno za automatski kod.",
                            color = TextHint,
                            fontSize = 11.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardLight,
                        unfocusedContainerColor = DarkCardLight,
                        focusedBorderColor = BorderSubtle,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = GreenAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            MatchSetupSection(title = "Raspon prosjeka protivnika") {
                MatchNumberStepper(
                    label = "Minimalni 3-dart prosjek",
                    value = minAvg,
                    onValueChange = { minAvg = it },
                    range = 20..100
                )
                Spacer(modifier = Modifier.height(12.dp))
                MatchNumberStepper(
                    label = "Maksimalni 3-dart prosjek",
                    value = maxAvg,
                    onValueChange = { maxAvg = it },
                    range = 20..100
                )
            }

            errorMessage?.let {
                Text(text = it, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    val settings = LobbySettings(
                        minAvg = minAvg,
                        maxAvg = maxAvg,
                        startScore = startScore,
                        format = format,
                        unit = unit,
                        count = count,
                        inRule = inRule,
                        outRule = outRule,
                        customCode = lobbyCode
                    )
                    val result = lobbyRepository.createLobby(settings)
                    isLoading = false
                    if (result.isSuccess) {
                        onLobbyCreated(result.getOrThrow())
                    } else {
                        errorMessage = result.exceptionOrNull()?.localizedMessage
                            ?: "Lobby nije kreiran."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(52.dp),
            enabled = !isLoading && minAvg <= maxAvg &&
                (lobbyCode.isEmpty() || lobbyCode.length in 4..12),
            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
        ) {
            Text(
                text = if (isLoading) "KREIRANJE..." else "KREIRAJ LOBBY",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
