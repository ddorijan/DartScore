package com.example.dartscore.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.UserRepository
import com.example.dartscore.model.OnlineStats
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.components.safeScreenTop
import com.example.dartscore.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    embedded: Boolean = false,
    userRepository: UserRepository = UserRepository()
) {
    val scope = rememberCoroutineScope()
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var onlineStats by remember { mutableStateOf(OnlineStats()) }

    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            userRepository.getCurrentUserProfile()?.let { profile ->
                displayName = profile.displayName
                email = profile.email
            }
            onlineStats = userRepository.getOnlineStats()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .then(
                if (embedded) Modifier.safeScreenTop()
                else Modifier.safeScreenBottom()
            )
    ) {
        if (!embedded) {
            ScreenTopBar(title = "Moj račun", onNavigateBack = onNavigateBack)
        }

        if (currentUser == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Prijavite se ili registrirajte kako biste upravljali svojim računom.",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text("Prijava", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Registracija", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OnlineStatsCard(stats = onlineStats)

                Text(
                    text = "Postavke računa",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                AccountField(
                    label = "Korisničko ime",
                    value = displayName,
                    onValueChange = { displayName = it }
                )
                AccountField(
                    label = "E-mail",
                    value = email,
                    onValueChange = { email = it },
                    keyboardType = KeyboardType.Email
                )
                AccountField(
                    label = "Trenutna lozinka",
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    isPassword = true,
                    passwordVisible = currentPasswordVisible,
                    onTogglePassword = { currentPasswordVisible = !currentPasswordVisible }
                )
                AccountField(
                    label = "Nova lozinka (opcionalno)",
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    isPassword = true,
                    passwordVisible = newPasswordVisible,
                    onTogglePassword = { newPasswordVisible = !newPasswordVisible }
                )

                Text(
                    text = "Trenutna lozinka je potrebna za promjenu e-maila ili lozinke.",
                    color = TextHint,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            statusMessage = null

                            val nameResult = userRepository.updateDisplayName(displayName)
                            if (nameResult.isFailure) {
                                isLoading = false
                                errorMessage = nameResult.exceptionOrNull()?.localizedMessage
                                    ?: "Korisničko ime nije spremljeno."
                                return@launch
                            }

                            val profile = userRepository.getCurrentUserProfile()
                            val savedEmail = profile?.email.orEmpty()
                            if (email.trim() != savedEmail) {
                                if (currentPassword.isBlank()) {
                                    isLoading = false
                                    errorMessage = "Unesite trenutnu lozinku za promjenu e-maila."
                                    return@launch
                                }
                                val emailResult = userRepository.updateEmail(email, currentPassword)
                                if (emailResult.isFailure) {
                                    isLoading = false
                                    errorMessage = emailResult.exceptionOrNull()?.localizedMessage
                                        ?: "E-mail nije spremljen."
                                    return@launch
                                }
                            }

                            if (newPassword.isNotBlank()) {
                                if (currentPassword.isBlank()) {
                                    isLoading = false
                                    errorMessage = "Unesite trenutnu lozinku za promjenu lozinke."
                                    return@launch
                                }
                                val passwordResult = userRepository.updatePassword(newPassword, currentPassword)
                                if (passwordResult.isFailure) {
                                    isLoading = false
                                    errorMessage = passwordResult.exceptionOrNull()?.localizedMessage
                                        ?: "Lozinka nije spremljena."
                                    return@launch
                                }
                                newPassword = ""
                            }

                            currentPassword = ""
                            isLoading = false
                            statusMessage = "Promjene su spremljene."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                ) {
                    Text(
                        text = if (isLoading) "SPREMANJE..." else "SPREMI PROMJENE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                statusMessage?.let {
                    Text(text = it, color = GreenAccent, fontSize = 12.sp)
                }
                errorMessage?.let {
                    Text(text = it, color = Color(0xFFFF6B6B), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        auth.signOut()
                        displayName = ""
                        email = ""
                        currentPassword = ""
                        newPassword = ""
                        statusMessage = null
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Odjava", color = RedAccent, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OnlineStatsCard(stats: OnlineStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(14.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Online utakmice",
            color = TextSecondary,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stats.recordLabel,
            color = GreenAccent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AccountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextHint
                        )
                    }
                }
            } else null,
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
}
