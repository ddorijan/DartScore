package com.example.dartscore.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dartscore.data.FeedRepository
import com.example.dartscore.data.MatchRepository
import com.example.dartscore.model.FeedPost
import com.example.dartscore.model.ActivityItem
import com.example.dartscore.model.AppNotification
import com.example.dartscore.ui.screen.social.ActivitiesFeedList
import com.example.dartscore.ui.screen.social.MatchHistoryList
import com.example.dartscore.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

// ─── Sample data removed — activities load from Firestore feed ───────────────

// ─── Dartboard Canvas ───────────────────────────────────────────────────────

@Composable
fun DartboardCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val maxR = size.minDimension / 2

        val rings = listOf(
            maxR to Color(0xFF1A1A1A),
            maxR * 0.97f to Color(0xFF2A2A2A),
            maxR * 0.88f to Color(0xFFCC2222),
            maxR * 0.82f to Color(0xFF1A1A1A),
            maxR * 0.65f to Color(0xFF228822),
            maxR * 0.60f to Color(0xFF1A1A1A),
            maxR * 0.44f to Color(0xFF228822),
            maxR * 0.38f to Color(0xFF1A1A1A),
            maxR * 0.15f to Color(0xFFCC2222),
            maxR * 0.08f to Color(0xFF33CC33),
        )

        rings.forEach { (r, color) ->
            drawCircle(color = color, radius = r, center = Offset(cx, cy))
        }

        // Segment lines
        val segmentCount = 20
        for (i in 0 until segmentCount) {
            val angle = Math.toRadians((i * 360.0 / segmentCount) - 90)
            drawLine(
                color = Color(0xFF333333),
                start = Offset(cx, cy),
                end = Offset(
                    cx + (maxR * 0.95f * Math.cos(angle)).toFloat(),
                    cy + (maxR * 0.95f * Math.sin(angle)).toFloat()
                ),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Numbers hint ring
        drawCircle(
            color = Color(0x33FFFFFF),
            radius = maxR * 0.93f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

// ─── Top App Bar ─────────────────────────────────────────────────────────────

@Composable
fun DartScoreTopBar(
    onNavigateToAccount: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    notifications: List<AppNotification> = emptyList()
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var notificationsExpanded by remember { mutableStateOf(false) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { currentUser = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .statusBarsPadding()
            .border(width = 1.dp, color = TopBarBorder, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Dart ",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Score",
                color = GreenAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            Text(text = "🎯", fontSize = 16.sp, modifier = Modifier.padding(start = 2.dp))
        }
        Spacer(modifier = Modifier.weight(1f))

        Box {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Obavijesti",
                tint = TextPrimary,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { notificationsExpanded = true }
            )
            DropdownMenu(
                expanded = notificationsExpanded,
                onDismissRequest = { notificationsExpanded = false },
                modifier = Modifier
                    .width(300.dp)
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Obavijesti",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = TextHint,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nema novih obavijesti",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    notifications.forEach { notification ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = notification.title,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = notification.message,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = notification.timeAgo,
                                        color = TextHint,
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            onClick = { notificationsExpanded = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Box {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (currentUser != null) Color(0xFF1A3320) else Color(0xFF3A3A3A))
                    .clickable {
                        if (currentUser != null) {
                            onNavigateToAccount()
                        } else {
                            accountMenuExpanded = true
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profil",
                    tint = if (currentUser != null) GreenAccent else TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                )
            }
            DropdownMenu(
                expanded = accountMenuExpanded,
                onDismissRequest = { accountMenuExpanded = false },
                modifier = Modifier
                    .width(220.dp)
                    .background(DarkCard, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Račun",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prijavite se ili registrirajte",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                DropdownMenuItem(
                    text = {
                        Text("Prijava", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    },
                    onClick = {
                        accountMenuExpanded = false
                        onNavigateToLogin()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text("Registracija", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    },
                    onClick = {
                        accountMenuExpanded = false
                        onNavigateToRegister()
                    }
                )
            }
        }
    }
}

// ─── Hero Section ─────────────────────────────────────────────────────────────

@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0D1F0D), Color(0xFF0E0E0E))
                )
            )
    ) {
        // Dartboard on the right
        DartboardCanvas(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )

        // Fade overlay so text is readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xEE0E0E0E),
                            Color(0x880E0E0E),
                            Color(0x000E0E0E)
                        )
                    )
                )
        )

        // Text overlay
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp, top = 16.dp)
        ) {
            Text(
                text = "SPREMAN ZA",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "IGRU?",
                color = GreenAccent,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Odaberi način i",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Text(
                text = "pogodi svoj najbolji rezultat",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

// ─── Main Game Cards ──────────────────────────────────────────────────────────

@Composable
fun MainGameCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(DarkCard, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = TextSecondary,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "→",
            color = accentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MainGameCards(
    onPlayLocal: () -> Unit = {},
    onPlayOnline: () -> Unit = {},
    onPlayTraining: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // IGRAJ LOKALNO
        MainGameCard(
            title = "IGRAJ\nLOKALNO",
            subtitle = "Igraj s prijateljima\nna istom uređaju",
            accentColor = GreenAccent,
            modifier = Modifier.weight(1f),
            onClick = onPlayLocal,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF1A3320), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎯", fontSize = 22.sp)
                }
            }
        )

        // IGRAJ ONLINE
        MainGameCard(
            title = "IGRAJ\nONLINE",
            subtitle = "Igraj protiv igrača\niz cijelog svijeta",
            accentColor = RedAccent,
            modifier = Modifier.weight(1f),
            onClick = onPlayOnline,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF3A1010), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = RedAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        )

        // TRENING
        MainGameCard(
            title = "TRENING",
            subtitle = "Vježbaj i poboljšaj\nsvoje vještine",
            accentColor = OliveAccent,
            modifier = Modifier.weight(1f),
            onClick = onPlayTraining,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2A2E15), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = Color(0xFFB8CC3A),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        )
    }
}

// ─── Secondary Cards ─────────────────────────────────────────────────────────

data class SecondaryCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val onClick: () -> Unit = {}
)

@Composable
fun SecondaryCards(
    onStatistics: () -> Unit = {},
    onLeagues: () -> Unit = {},
    onFriends: () -> Unit = {},
    onFeedWall: () -> Unit = {}
) {
    val cards = listOf(
        SecondaryCardData("STATISTIKA", "Prati svoj\nnapredak", Icons.Default.BarChart, GreenAccent, onStatistics),
        SecondaryCardData("LIGE", "Online lige\ni ljestvice", Icons.Default.EmojiEvents, GreenAccent, onLeagues),
        SecondaryCardData("PRIJATELJI", "Dodaj prijatelja\ni izazovi ih", Icons.Default.Group, GreenAccent, onFriends),
        SecondaryCardData("ZID OBJAVA", "Podijeli rezultate\ni izazove", Icons.Outlined.ChatBubbleOutline, GreenAccent, onFeedWall),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cards.forEach { card ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(DarkCard, RoundedCornerShape(10.dp))
                    .clickable(onClick = card.onClick)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = card.iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.title,
                    color = TextPrimary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 10.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.subtitle,
                    color = TextSecondary,
                    fontSize = 7.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 9.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "→", color = GreenAccent, fontSize = 11.sp)
            }
        }
    }
}

// ─── Activities Section ───────────────────────────────────────────────────────

@Composable
fun ActivitiesSection(
    activities: List<ActivityItem>,
    onShowAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AKTIVNOSTI",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "PRIKAŽI SVE >",
                color = GreenAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onShowAll() }
            )
        }

        ActivitiesFeedList(
            activities = activities.take(5),
            emptyMessage = "Nema objava od prijatelja.\nDodaj prijatelje ili prati igrače."
        )
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ─── Bottom Navigation ────────────────────────────────────────────────────────

enum class BottomNavItem(val label: String, val icon: ImageVector) {
    Home("POČETNA", Icons.Filled.Home),
    Results("REZULTATI", Icons.Outlined.CalendarMonth),
    Activities("AKTIVNOSTI", Icons.Outlined.People),
    Profile("PROFIL", Icons.Outlined.Person)
}

@Composable
fun DartScoreBottomNav(
    selected: BottomNavItem,
    onSelect: (BottomNavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BottomNavBackground)
            .border(
                width = 0.5.dp,
                color = Color(0xFF333333),
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val isSelected = selected == item
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onSelect(item) }
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) GreenAccent else TextHint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = if (isSelected) GreenAccent else TextHint,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(GreenAccent, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

// ─── Home Screen ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToLocalPlay: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToOnlinePlay: () -> Unit = {},
    onNavigateToTraining: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToLeagues: () -> Unit = {},
    onNavigateToFriends: () -> Unit = {},
    onNavigateToFeedWall: () -> Unit = {},
    onOpenMatchStats: (String) -> Unit = {}
) {
    var selectedNav by remember { mutableStateOf(BottomNavItem.Home) }
    val feedRepository = remember { FeedRepository() }
    val matchRepository = remember { MatchRepository() }
    var feedActivities by remember { mutableStateOf<List<ActivityItem>>(emptyList()) }
    var matchHistory by remember { mutableStateOf<List<com.example.dartscore.model.MatchHistoryItem>>(emptyList()) }
    var feedRefreshKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(feedRefreshKey) {
        feedActivities = feedRepository.getNetworkFeed(30).getOrNull()
            ?.map { it.toActivityItem() }
            .orEmpty()
        matchHistory = matchRepository.getMatchHistory(30).getOrNull().orEmpty()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (selectedNav != BottomNavItem.Profile) {
                DartScoreTopBar(
                    onNavigateToAccount = {
                        selectedNav = BottomNavItem.Profile
                        onNavigateToAccount()
                    },
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister
                )
            }
        },
        bottomBar = {
            DartScoreBottomNav(
                selected = selectedNav,
                onSelect = { item ->
                    selectedNav = item
                    if (item == BottomNavItem.Home || item == BottomNavItem.Activities) {
                        feedRefreshKey++
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        when (selectedNav) {
                BottomNavItem.Home -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    item { HeroSection() }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        MainGameCards(
                            onPlayLocal = onNavigateToLocalPlay,
                            onPlayOnline = onNavigateToOnlinePlay,
                            onPlayTraining = onNavigateToTraining
                        )
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    item {
                        SecondaryCards(
                            onStatistics = onNavigateToStatistics,
                            onLeagues = onNavigateToLeagues,
                            onFriends = onNavigateToFriends,
                            onFeedWall = onNavigateToFeedWall
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    item {
                        ActivitiesSection(
                            activities = feedActivities,
                            onShowAll = { selectedNav = BottomNavItem.Activities }
                        )
                    }
                }

                BottomNavItem.Results -> Box(Modifier.fillMaxSize().padding(innerPadding)) {
                    Column(Modifier.fillMaxSize()) {
                        Text(
                            text = "Povijest utakmica",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                        MatchHistoryList(
                            matches = matchHistory,
                            emptyMessage = "Nema spremljenih utakmica.\nIgraj lokalno dok si prijavljen.",
                            onMatchClick = { onOpenMatchStats(it.id) }
                        )
                    }
                }

                BottomNavItem.Activities -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                ) {
                    item {
                        Text(
                            text = "Aktivnosti",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = "Objave prijatelja i igrača koje pratiš",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    item {
                        ActivitiesFeedList(
                            activities = feedActivities,
                            emptyMessage = "Nema objava.\nDodaj prijatelje, prati igrače ili objavi na Zid objava."
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }

            BottomNavItem.Profile -> Box(Modifier.fillMaxSize().padding(innerPadding)) {
                AccountScreen(
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    embedded = true
                )
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0E0E0E)
@Composable
fun HomeScreenPreview() {
    DartScoreTheme {
        HomeScreen()
    }
}


