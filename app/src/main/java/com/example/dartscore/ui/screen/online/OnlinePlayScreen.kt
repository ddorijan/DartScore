package com.example.dartscore.ui.screen.online

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.LobbyRepository
import com.example.dartscore.model.OnlineLobby
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun OnlinePlayScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToCreateLobby: () -> Unit,
    onNavigateToLobby: (String) -> Unit,
    lobbyRepository: LobbyRepository = LobbyRepository()
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    val scope = rememberCoroutineScope()
    var lobbies by remember { mutableStateOf<List<OnlineLobby>>(emptyList()) }
    var joinError by remember { mutableStateOf<String?>(null) }
    var joiningId by remember { mutableStateOf<String?>(null) }
    var lobbyQuery by remember { mutableStateOf("") }
    var isJoiningByCode by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            lobbyRepository.observeOpenLobbies().collect { lobbies = it }
        }
    }

    val filteredLobbies = remember(lobbies, lobbyQuery) {
        val query = lobbyQuery.trim()
        if (query.isEmpty()) lobbies
        else lobbies.filter { lobby ->
            lobby.hostName.contains(query, ignoreCase = true) ||
                lobby.code.contains(query, ignoreCase = true) ||
                LobbyRepository.formatCodeForDisplay(lobby.code).contains(query, ignoreCase = true)
        }
    }

    val joinCode = lobbyQuery.uppercase().filter { it.isLetterOrDigit() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .safeScreenBottom()
    ) {
        ScreenTopBar(title = "Igraj online", onNavigateBack = onNavigateBack)

        if (currentUser == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = RedAccent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Prijavite se za online igru",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kreirajte lobby ili se pridružite postojećem.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text("Prijava", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onNavigateToCreateLobby,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kreiraj novi lobby", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kod lobija",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unesi kod za ulaz ili pretraži otvorene lobbye",
                    color = TextHint,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lobbyQuery,
                        onValueChange = {
                            if (it.length <= 12) lobbyQuery = it
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Kod ili ime domaćina", color = TextHint) },
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
                    Button(
                        onClick = {
                            scope.launch {
                                isJoiningByCode = true
                                joinError = null
                                val result = lobbyRepository.joinLobbyByCode(joinCode)
                                isJoiningByCode = false
                                if (result.isSuccess) {
                                    onNavigateToLobby(result.getOrThrow())
                                } else {
                                    joinError = result.exceptionOrNull()?.localizedMessage
                                        ?: "Pridruživanje nije uspjelo."
                                }
                            }
                        },
                        enabled = joinCode.length in 4..12 && !isJoiningByCode,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text(
                            text = if (isJoiningByCode) "..." else "ULAZ",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Otvoreni lobbyi",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                joinError?.let {
                    Text(text = it, color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                if (filteredLobbies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lobbies.isEmpty()) {
                                "Nema otvorenih lobbyja.\nKreirajte novi i podijelite kod s prijateljem."
                            } else {
                                "Nema lobbyja za \"$lobbyQuery\"."
                            },
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLobbies, key = { it.id }) { lobby ->
                            LobbyListItem(
                                lobby = lobby,
                                isJoining = joiningId == lobby.id,
                                onJoin = {
                                    scope.launch {
                                        joiningId = lobby.id
                                        joinError = null
                                        val result = lobbyRepository.joinLobby(lobby.id)
                                        joiningId = null
                                        if (result.isSuccess) {
                                            onNavigateToLobby(lobby.id)
                                        } else {
                                            joinError = result.exceptionOrNull()?.localizedMessage
                                                ?: "Pridruživanje nije uspjelo."
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbyListItem(
    lobby: OnlineLobby,
    isJoining: Boolean,
    onJoin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(enabled = !isJoining, onClick = onJoin)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = lobby.hostName,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = lobby.settingsSummary,
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = "Prosjek: ${lobby.minAvg}–${lobby.maxAvg} · Kod: ${LobbyRepository.formatCodeForDisplay(lobby.code)}",
                color = TextHint,
                fontSize = 11.sp
            )
        }
        if (isJoining) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = GreenAccent,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Pridruži se →",
                color = GreenAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
