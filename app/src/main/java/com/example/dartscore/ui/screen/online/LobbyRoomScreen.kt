package com.example.dartscore.ui.screen.online

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
fun LobbyRoomScreen(
    lobbyId: String,
    onNavigateBack: () -> Unit,
    lobbyRepository: LobbyRepository = LobbyRepository()
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUid = auth.currentUser?.uid
    val scope = rememberCoroutineScope()
    var lobby by remember { mutableStateOf<OnlineLobby?>(null) }
    var isLeaving by remember { mutableStateOf(false) }
    var copyMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(copyMessage) {
        copyMessage?.let {
            snackbarHostState.showSnackbar(it)
            copyMessage = null
        }
    }

    fun copyLobbyCode(code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Kod lobija", code))
        copyMessage = "Kod je kopiran."
    }

    fun shareLobbyCode(lobby: OnlineLobby) {
        val text = LobbyRepository.lobbyShareText(lobby.code, lobby.hostName)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Podijeli kod lobija"))
    }

    LaunchedEffect(lobbyId) {
        lobbyRepository.observeLobby(lobbyId).collect { lobby = it }
    }

    val isHost = lobby?.hostUid == currentUid
    val isGuest = lobby?.guestUid == currentUid
    val isReady = lobby?.status == "ready" && lobby?.guestUid != null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .safeScreenBottom()
        ) {
            ScreenTopBar(title = "Lobby", onNavigateBack = {
            scope.launch {
                isLeaving = true
                lobbyRepository.leaveLobby(lobbyId)
                isLeaving = false
                onNavigateBack()
            }
        })

        if (lobby == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Kod lobija",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = LobbyRepository.formatCodeForDisplay(lobby!!.code),
                    color = GreenAccent,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { copyLobbyCode(lobby!!.code) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kopiraj", color = TextPrimary, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = { shareLobbyCode(lobby!!) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, null, tint = GreenAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Podijeli", color = TextPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = lobby!!.settingsSummary,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Prosjek: ${lobby!!.minAvg}–${lobby!!.maxAvg}",
                    color = TextHint,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                PlayerSlot(name = lobby!!.hostName, label = "Domaćin", isFilled = true)

                Spacer(modifier = Modifier.height(12.dp))

                PlayerSlot(
                    name = lobby!!.guestName ?: "Čeka protivnika...",
                    label = "Gost",
                    isFilled = lobby!!.guestUid != null
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isReady) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A3320), RoundedCornerShape(12.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Protivnik pronađen!",
                            color = GreenAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Online utakmica uskoro.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                } else if (isHost) {
                    Text(
                        text = "Čekamo protivnika...\nPodijelite kod lobija ili pričekajte da se netko pridruži.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                } else if (isGuest) {
                    Text(
                        text = "Spremni ste! Čekamo da domaćin pokrene utakmicu.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLeaving = true
                            lobbyRepository.leaveLobby(lobbyId)
                            isLeaving = false
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLeaving,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(if (isLeaving) "IZLAZ..." else "Napusti lobby")
                }
            }
        }
    }
    }
}

@Composable
private fun PlayerSlot(name: String, label: String, isFilled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (isFilled) Color(0xFF1A3320) else Color(0xFF2A2A2A),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFilled) name.take(1).uppercase() else "?",
                color = if (isFilled) GreenAccent else TextHint,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, color = TextHint, fontSize = 11.sp)
            Text(
                text = name,
                color = if (isFilled) TextPrimary else TextHint,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
