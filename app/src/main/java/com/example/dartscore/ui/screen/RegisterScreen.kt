package com.example.dartscore.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.ui.theme.*
import com.example.dartscore.ui.components.DartboardCanvas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onAuthSuccess: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Dartboard in top-right background
        DartboardCanvas(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-10).dp),
            rotationDegrees = -12f
        )

        // Gradient overlay over dartboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .align(Alignment.TopStart)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0E0E0E),
                            Color(0xAA0E0E0E),
                            Color(0x220E0E0E)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo
            AuthLogo()

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Kreiraj svoj račun",
                    color = GreenAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pridruži se zajednici pikado igrača i počni bilježiti svoje rezultate",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Username
            AuthInputField(
                value = username,
                onValueChange = { username = it },
                placeholder = "Korisničko ime",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email
            AuthInputField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email adresa",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            AuthInputField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Lozinka",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm password
            AuthInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Ponovi lozinku",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Birth date
            AuthInputField(
                value = birthDate,
                onValueChange = { birthDate = it },
                placeholder = "Datum rođenja (opcionalno)",
                leadingIcon = Icons.Outlined.CalendarMonth,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Terms checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            if (termsAccepted) GreenAccent else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.5.dp,
                            BorderSubtle,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { termsAccepted = !termsAccepted },
                    contentAlignment = Alignment.Center
                ) {
                    if (termsAccepted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextSecondary, fontSize = 12.sp)) {
                            append("Prihvaćam ")
                        }
                        withStyle(SpanStyle(color = GreenAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                            append("Uvjete korištenja")
                        }
                        withStyle(SpanStyle(color = TextSecondary, fontSize = 12.sp)) {
                            append(" i ")
                        }
                        withStyle(SpanStyle(color = GreenAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)) {
                            append("politiku privatnosti")
                        }
                    },
                    modifier = Modifier.clickable { termsAccepted = !termsAccepted }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register button
            Button(
                onClick = {
                    val cleanUsername = username.trim()
                    val cleanEmail = email.trim()

                    when {
                        cleanUsername.isBlank() -> {
                            errorMessage = "Unesite korisničko ime."
                            return@Button
                        }
                        cleanEmail.isBlank() -> {
                            errorMessage = "Unesite email adresu."
                            return@Button
                        }
                        password.length < 6 -> {
                            errorMessage = "Lozinka mora imati najmanje 6 znakova."
                            return@Button
                        }
                        password != confirmPassword -> {
                            errorMessage = "Lozinke se ne podudaraju."
                            return@Button
                        }
                        !termsAccepted -> {
                            errorMessage = "Potvrdite uvjete korištenja."
                            return@Button
                        }
                    }

                    isLoading = true
                    errorMessage = null
                    firebaseAuth
                        .createUserWithEmailAndPassword(cleanEmail, password)
                        .addOnCompleteListener { authTask ->
                            if (!authTask.isSuccessful) {
                                isLoading = false
                                errorMessage = authTask.exception?.localizedMessage
                                    ?: "Registracija nije uspjela."
                                return@addOnCompleteListener
                            }

                            val uid = firebaseAuth.currentUser?.uid
                            if (uid == null) {
                                isLoading = false
                                errorMessage = "Korisnik nije ispravno kreiran."
                                return@addOnCompleteListener
                            }

                            val profile = hashMapOf(
                                "displayName" to cleanUsername,
                                "displayNameLower" to cleanUsername.lowercase(),
                                "email" to cleanEmail,
                                "birthDate" to birthDate.trim(),
                                "avatarUrl" to "",
                                "country" to "",
                                "createdAt" to FieldValue.serverTimestamp(),
                                "defaultGameSettings" to mapOf(
                                    "startScore" to 501,
                                    "doubleIn" to false,
                                    "doubleOut" to true,
                                    "setsOrLegs" to "legs"
                                ),
                                "onlineStats" to mapOf(
                                    "wins" to 0,
                                    "losses" to 0
                                )
                            )

                            firestore
                                .collection("users")
                                .document(uid)
                                .set(profile)
                                .addOnSuccessListener {
                                    isLoading = false
                                    onAuthSuccess()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = e.localizedMessage
                                        ?: "Profil nije spremljen u bazu."
                                }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                enabled = termsAccepted && !isLoading
            ) {
                Text(
                    text = if (isLoading) "REGISTRACIJA..." else "REGISTRIRAJ SE",
                    color = if (termsAccepted) Color.Black else TextHint,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Login link
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextSecondary, fontSize = 13.sp)) {
                        append("Već imaš račun? ")
                    }
                    withStyle(SpanStyle(color = GreenAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)) {
                        append("Prijavi se")
                    }
                },
                modifier = Modifier.clickable { onNavigateToLogin() }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0E0E)
@Composable
fun RegisterScreenPreview() {
    DartScoreTheme {
        RegisterScreen()
    }
}

