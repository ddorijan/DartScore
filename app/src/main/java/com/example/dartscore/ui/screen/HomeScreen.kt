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
import com.example.dartscore.ui.theme.*
import kotlinx.coroutines.launch

// ─── Data classes ───────────────────────────────────────────────────────────

data class ActivityItem(
    val userName: String,
    val actionText: String,
    val timeAgo: String,
    val detail: String,
    val score: String
)

// ─── Sample data ────────────────────────────────────────────────────────────

private val sampleActivities = listOf(
    ActivityItem("Marko D.", "je objavio novi rezultat", "prije 2h", "301 - SINGLE HIT", "106  Checkout"),
    ActivityItem("Luka P.", "je pobijedio Ivana K.", "prije 4h", "Luka P. 3  vs  Ivan K. 1", ""),
    ActivityItem("Petar T.", "je postavio novi osobni rekord", "prije 6h", "301 - DOUBLE IN / DOUBLE OUT", "78  Checkout")
)

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
fun DartScoreTopBar(onMenuClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(width = 1.dp, color = TopBarBorder, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = TextPrimary,
            modifier = Modifier
                .size(26.dp)
                .clickable { onMenuClick() }
        )
        Spacer(modifier = Modifier.weight(1f))
        // Logo
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
            // Dart icon decoration
            Text(text = "🎯", fontSize = 16.sp, modifier = Modifier.padding(start = 2.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Obavijesti",
            tint = TextPrimary,
            modifier = Modifier
                .size(26.dp)
                .padding(end = 0.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A))
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profil",
                tint = TextSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
            )
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
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DarkCard, RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "→",
            color = borderColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MainGameCards() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // IGRAJ LOKALNO
        MainGameCard(
            title = "IGRAJ\nLOKALNO",
            subtitle = "Igraj s prijateljima\nna istom uređaju",
            borderColor = GreenAccent,
            modifier = Modifier.weight(1f),
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
            borderColor = RedAccent,
            modifier = Modifier.weight(1f),
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
            borderColor = OliveAccent,
            modifier = Modifier.weight(1f),
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
    val iconTint: Color
)

@Composable
fun SecondaryCards() {
    val cards = listOf(
        SecondaryCardData("STATISTIKA", "Prati svoj\nnapredak", Icons.Default.BarChart, GreenAccent),
        SecondaryCardData("LIGE", "Online lige\ni ljestvice", Icons.Default.EmojiEvents, GreenAccent),
        SecondaryCardData("PRIJATELJI", "Dodaj prijatelja\ni izazovi ih", Icons.Default.Group, GreenAccent),
        SecondaryCardData("ZID OBJAVA", "Podijeli rezultate\ni izazove", Icons.Outlined.ChatBubbleOutline, GreenAccent),
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

// ─── Activity item ────────────────────────────────────────────────────────────

@Composable
fun ActivityItemRow(activity: ActivityItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activity.userName.take(1),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activity.userName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " ${activity.actionText}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Text(
                text = activity.timeAgo,
                color = TextHint,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Detail box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardLight, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    if (activity.detail.isNotEmpty()) {
                        Text(
                            text = activity.detail,
                            color = TextHint,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    }
                    if (activity.score.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activity.score,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = "🎯", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Više",
            tint = TextHint,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ─── Activities Section ───────────────────────────────────────────────────────

@Composable
fun ActivitiesSection() {
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
                fontWeight = FontWeight.Medium
            )
        }

        sampleActivities.forEach { activity ->
            ActivityItemRow(activity = activity)
            if (activity != sampleActivities.last()) {
                HorizontalDivider(
                    color = Color(0xFF2A2A2A),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ─── Bottom Navigation ────────────────────────────────────────────────────────

enum class BottomNavItem(val label: String, val icon: ImageVector) {
    Home("POČETNA", Icons.Filled.Home),
    Results("REZULTATI", Icons.Outlined.CalendarMonth),
    NewGame("NOVA IGRA", Icons.Default.Add),
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
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                if (item == BottomNavItem.NewGame) {
                    // Center FAB-style button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSelect(item) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(GreenAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = item.label,
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = GreenAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val isSelected = selected == item
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelect(item) }
                            .padding(horizontal = 4.dp)
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
}

// ─── App Drawer ───────────────────────────────────────────────────────────────

@Composable
fun AppDrawerContent(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(DarkSurface)
            .padding(vertical = 48.dp, horizontal = 20.dp)
    ) {
        // Logo u draweru
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Dart ",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Score",
                color = GreenAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(text = " 🎯", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF2E6B3E), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Drawer menu items
        DrawerMenuItem(
            icon = Icons.Default.Login,
            title = "Prijava",
            subtitle = "Prijavi se na svoj račun",
            onClick = { onNavigateToLogin(); onClose() }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DrawerMenuItem(
            icon = Icons.Default.PersonAdd,
            title = "Registracija",
            subtitle = "Kreiraj novi račun",
            onClick = { onNavigateToRegister(); onClose() }
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        DrawerMenuItem(
            icon = Icons.Default.Home,
            title = "Početna",
            subtitle = "Vrati se na početni zaslon",
            onClick = { onClose() }
        )

        DrawerMenuItem(
            icon = Icons.Filled.BarChart,
            title = "Statistika",
            subtitle = "Prati svoj napredak",
            onClick = { onClose() }
        )

        DrawerMenuItem(
            icon = Icons.Default.EmojiEvents,
            title = "Lige",
            subtitle = "Online lige i ljestvice",
            onClick = { onClose() }
        )

        DrawerMenuItem(
            icon = Icons.Default.Group,
            title = "Prijatelji",
            subtitle = "Dodaj i izazovi prijatelje",
            onClick = { onClose() }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Version info
        Text(
            text = "DartScore v1.0",
            color = TextHint,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(DarkCard, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = GreenAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// ─── Home Screen ──────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {}
) {
    var selectedNav by remember { mutableStateOf(BottomNavItem.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                onNavigateToLogin = onNavigateToLogin,
                onNavigateToRegister = onNavigateToRegister,
                onClose = { scope.launch { drawerState.close() } }
            )
        },
        scrimColor = Color(0xAA000000)
    ) {
        Scaffold(
            topBar = {
                DartScoreTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                DartScoreBottomNav(
                    selected = selectedNav,
                    onSelect = { selectedNav = it }
                )
            },
            containerColor = DarkBackground
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item { HeroSection() }
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item { MainGameCards() }
                item { Spacer(modifier = Modifier.height(4.dp)) }
                item { SecondaryCards() }
                item { Spacer(modifier = Modifier.height(8.dp)) }
                item { ActivitiesSection() }
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


