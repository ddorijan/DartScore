package com.example.dartscore.ui.screen.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.FeedRepository
import com.example.dartscore.model.FeedPost
import com.example.dartscore.model.FeedPostType
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.components.dismissKeyboardOnTap
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FeedWallScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    feedRepository: FeedRepository = FeedRepository()
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var postType by remember { mutableStateOf(FeedPostType.GAME) }
    var message by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var scoreHighlight by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var posting by remember { mutableStateOf(false) }
    var myPosts by remember { mutableStateOf<List<FeedPost>>(emptyList()) }
    var postsRefreshKey by remember { mutableIntStateOf(0) }

    DisposableEffect(auth) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(currentUser, postsRefreshKey) {
        if (currentUser != null) {
            myPosts = feedRepository.getMyPosts(30).getOrNull().orEmpty()
        } else {
            myPosts = emptyList()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground).safeScreenBottom()) {
        ScreenTopBar(title = "Zid objava", onNavigateBack = onNavigateBack)

        if (currentUser == null) {
            LoginRequiredPlaceholder("Prijavite se za objave.", onNavigateToLogin)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .dismissKeyboardOnTap()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Nova objava", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Tvoje objave i objave prijatelja pojavljuju se i u Aktivnostima.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                FeedPostType.entries.forEach { type ->
                    FilterChip(
                        selected = postType == type,
                        onClick = { postType = type },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1A3320),
                            selectedLabelColor = GreenAccent,
                            labelColor = TextSecondary
                        )
                    )
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Poruka") },
                    placeholder = { Text("Npr. Odigrao sjajnu utakmicu!") },
                    minLines = 2,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Detalj (opcionalno)") },
                    placeholder = { Text("501 - DOUBLE OUT") },
                    colors = fieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = scoreHighlight,
                    onValueChange = { scoreHighlight = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Istakni rezultat (opcionalno)") },
                    placeholder = { Text("121 Checkout") },
                    colors = fieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                status?.let { Text(it, color = GreenAccent, fontSize = 12.sp) }
                error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp) }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        scope.launch {
                            posting = true
                            error = null
                            status = null
                            val result = feedRepository.createPost(postType, message, detail, scoreHighlight)
                            posting = false
                            if (result.isSuccess) {
                                message = ""
                                detail = ""
                                scoreHighlight = ""
                                status = "Objava objavljena!"
                                postsRefreshKey++
                            } else {
                                error = result.exceptionOrNull()?.localizedMessage ?: "Objava nije spremljena."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !posting && message.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text(if (posting) "OBJAVLJIVANJE..." else "OBJAVI", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = BorderSubtle)
                Text("Moje objave", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                if (myPosts.isEmpty()) {
                    Text(
                        "Još nemaš objava.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                } else {
                    myPosts.forEach { post ->
                        ActivityItemRow(activity = post.toActivityItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DarkCardLight,
    unfocusedContainerColor = DarkCardLight,
    focusedBorderColor = BorderSubtle,
    unfocusedBorderColor = BorderSubtle,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
