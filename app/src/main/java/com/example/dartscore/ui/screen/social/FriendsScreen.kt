package com.example.dartscore.ui.screen.social

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.dartscore.data.SocialRepository
import com.example.dartscore.model.SocialUser
import com.example.dartscore.ui.components.ScreenTopBar
import com.example.dartscore.ui.components.safeScreenBottom
import com.example.dartscore.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FriendsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    socialRepository: SocialRepository = SocialRepository()
) {
    val scope = rememberCoroutineScope()
    val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var friends by remember { mutableStateOf<List<SocialUser>>(emptyList()) }
    var following by remember { mutableStateOf<List<SocialUser>>(emptyList()) }
    var nickname by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<SocialUser?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    fun refresh() {
        scope.launch {
            friends = socialRepository.getFriends().getOrNull().orEmpty()
            following = socialRepository.getFollowing().getOrNull().orEmpty()
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) refresh()
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground).safeScreenBottom()) {
        ScreenTopBar(title = "Prijatelji", onNavigateBack = onNavigateBack)

        if (currentUser == null) {
            LoginRequiredPlaceholder("Prijavite se za prijatelje.", onNavigateToLogin)
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Dodaj prijatelja po nadimku", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Nadimak", color = TextHint) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkCardLight,
                            unfocusedContainerColor = DarkCardLight,
                            focusedBorderColor = BorderSubtle,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                error = null
                                searchResult = null
                                val result = socialRepository.findUserByNickname(nickname)
                                loading = false
                                if (result.isSuccess) {
                                    searchResult = result.getOrNull()
                                    if (result.getOrNull() == null) error = "Korisnik nije pronađen."
                                } else {
                                    error = result.exceptionOrNull()?.localizedMessage
                                }
                            }
                        },
                        enabled = !loading && nickname.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent)
                    ) {
                        Text(if (loading) "..." else "TRAŽI", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                searchResult?.let { user ->
                    Spacer(modifier = Modifier.height(12.dp))
                    SocialUserCard(
                        user = user,
                        onAddFriend = {
                            scope.launch {
                                socialRepository.addFriend(user).onSuccess { refresh(); searchResult = null; nickname = "" }
                            }
                        },
                        onFollow = {
                            scope.launch {
                                socialRepository.followUser(user).onSuccess { refresh() }
                            }
                        },
                        onUnfollow = {
                            scope.launch {
                                socialRepository.unfollowUser(user.uid).onSuccess { refresh() }
                            }
                        },
                        onRemoveFriend = {
                            scope.launch {
                                socialRepository.removeFriend(user.uid).onSuccess { refresh() }
                            }
                        }
                    )
                }

                error?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Prijatelji (${friends.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(friends, key = { it.uid }) { friend ->
                        SocialUserCard(
                            user = friend,
                            onAddFriend = {},
                            onFollow = {
                                scope.launch { socialRepository.followUser(friend).onSuccess { refresh() } }
                            },
                            onUnfollow = {
                                scope.launch { socialRepository.unfollowUser(friend.uid).onSuccess { refresh() } }
                            },
                            onRemoveFriend = {
                                scope.launch { socialRepository.removeFriend(friend.uid).onSuccess { refresh() } }
                            }
                        )
                    }
                    if (following.isNotEmpty()) {
                        item {
                            Text(
                                "Pratim (${following.count { !it.isFriend }})",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(following.filter { !it.isFriend }, key = { "f-${it.uid}" }) { user ->
                            SocialUserCard(
                                user = user,
                                onAddFriend = {
                                    scope.launch { socialRepository.addFriend(user).onSuccess { refresh() } }
                                },
                                onFollow = {},
                                onUnfollow = {
                                    scope.launch { socialRepository.unfollowUser(user.uid).onSuccess { refresh() } }
                                },
                                onRemoveFriend = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialUserCard(
    user: SocialUser,
    onAddFriend: () -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onRemoveFriend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(user.displayName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (!user.isFriend) {
            TextButton(onClick = onAddFriend) { Text("Prijatelj", color = GreenAccent, fontSize = 12.sp) }
        } else {
            TextButton(onClick = onRemoveFriend) { Text("Ukloni", color = RedAccent, fontSize = 12.sp) }
        }
        if (user.isFollowing) {
            TextButton(onClick = onUnfollow) { Text("Ne prati", color = TextSecondary, fontSize = 12.sp) }
        } else {
            TextButton(onClick = onFollow) { Text("Prati", color = GreenAccent, fontSize = 12.sp) }
        }
    }
}
