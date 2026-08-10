package com.example.dartscore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import com.example.dartscore.data.MatchRepository
import com.example.dartscore.model.MatchSettings
import com.example.dartscore.model.MatchStatsDetail
import com.example.dartscore.model.TrainingMode
import com.example.dartscore.ui.screen.AccountScreen
import com.example.dartscore.ui.screen.HomeScreen
import com.example.dartscore.ui.screen.LoginScreen
import com.example.dartscore.ui.screen.RegisterScreen
import com.example.dartscore.ui.screen.local.LocalGameScreen
import com.example.dartscore.ui.screen.local.LocalPlayScreen
import com.example.dartscore.ui.screen.local.MatchSetupScreen
import com.example.dartscore.ui.screen.online.CreateLobbyScreen
import com.example.dartscore.ui.screen.online.LobbyRoomScreen
import com.example.dartscore.ui.screen.online.OnlinePlayScreen
import com.example.dartscore.ui.screen.social.FeedWallScreen
import com.example.dartscore.ui.screen.social.FriendsScreen
import com.example.dartscore.ui.screen.social.LeaguesScreen
import com.example.dartscore.ui.screen.social.StatisticsScreen
import com.example.dartscore.ui.screen.match.MatchStatsScreen
import com.example.dartscore.ui.screen.training.TrainingGameScreen
import com.example.dartscore.ui.screen.training.TrainingScreen
import com.example.dartscore.ui.theme.DartScoreTheme

private sealed class AppScreen {
    data object Home : AppScreen()
    data object Login : AppScreen()
    data object Register : AppScreen()
    data object Account : AppScreen()
    data object Statistics : AppScreen()
    data object Leagues : AppScreen()
    data object Friends : AppScreen()
    data object FeedWall : AppScreen()
    data object LocalPlay : AppScreen()
    data object MatchSetup : AppScreen()
    data object OnlinePlay : AppScreen()
    data object CreateLobby : AppScreen()
    data class LobbyRoom(val lobbyId: String) : AppScreen()
    data object Training : AppScreen()
    data class TrainingGame(val mode: TrainingMode) : AppScreen()
    data class LocalGame(val settings: MatchSettings) : AppScreen()
    data class MatchStats(
        val detail: MatchStatsDetail,
        val fromHistory: Boolean = false
    ) : AppScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            DartScoreTheme {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
                val matchRepository = remember { MatchRepository() }
                val scope = rememberCoroutineScope()

                fun openMatchFromHistory(matchId: String) {
                    scope.launch {
                        matchRepository.getMatchDetail(matchId).getOrNull()?.let { detail ->
                            currentScreen = AppScreen.MatchStats(detail = detail, fromHistory = true)
                        }
                    }
                }

                BackHandler(enabled = currentScreen !is AppScreen.Home) {
                    currentScreen = when (currentScreen) {
                        is AppScreen.LocalGame -> AppScreen.MatchSetup
                        is AppScreen.MatchStats -> {
                            val screen = currentScreen as AppScreen.MatchStats
                            if (screen.fromHistory) AppScreen.Home else AppScreen.MatchSetup
                        }
                        AppScreen.MatchSetup -> AppScreen.LocalPlay
                        AppScreen.LocalPlay -> AppScreen.Home
                        AppScreen.OnlinePlay -> AppScreen.Home
                        AppScreen.Training -> AppScreen.Home
                        is AppScreen.TrainingGame -> AppScreen.Training
                        AppScreen.CreateLobby -> AppScreen.OnlinePlay
                        is AppScreen.LobbyRoom -> AppScreen.OnlinePlay
                        AppScreen.Statistics, AppScreen.Leagues, AppScreen.Friends, AppScreen.FeedWall,
                        AppScreen.Login, AppScreen.Register, AppScreen.Account -> AppScreen.Home
                        AppScreen.Home -> AppScreen.Home
                    }
                }

                when (val screen = currentScreen) {
                    AppScreen.Home -> HomeScreen(
                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                        onNavigateToRegister = { currentScreen = AppScreen.Register },
                        onNavigateToLocalPlay = { currentScreen = AppScreen.LocalPlay },
                        onNavigateToAccount = { currentScreen = AppScreen.Account },
                        onNavigateToOnlinePlay = { currentScreen = AppScreen.OnlinePlay },
                        onNavigateToTraining = { currentScreen = AppScreen.Training },
                        onNavigateToStatistics = { currentScreen = AppScreen.Statistics },
                        onNavigateToLeagues = { currentScreen = AppScreen.Leagues },
                        onNavigateToFriends = { currentScreen = AppScreen.Friends },
                        onNavigateToFeedWall = { currentScreen = AppScreen.FeedWall },
                        onOpenMatchStats = ::openMatchFromHistory
                    )
                    AppScreen.Account -> AccountScreen(
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                        onNavigateToRegister = { currentScreen = AppScreen.Register }
                    )
                    AppScreen.Statistics -> StatisticsScreen(
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                        onOpenMatch = ::openMatchFromHistory
                    )
                    AppScreen.Leagues -> LeaguesScreen(onNavigateBack = { currentScreen = AppScreen.Home })
                    AppScreen.Friends -> FriendsScreen(
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onNavigateToLogin = { currentScreen = AppScreen.Login }
                    )
                    AppScreen.FeedWall -> FeedWallScreen(
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onNavigateToLogin = { currentScreen = AppScreen.Login }
                    )
                    AppScreen.Login -> LoginScreen(
                        onNavigateToRegister = { currentScreen = AppScreen.Register },
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onAuthSuccess = { currentScreen = AppScreen.Home }
                    )
                    AppScreen.Register -> RegisterScreen(
                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onAuthSuccess = { currentScreen = AppScreen.Home }
                    )
                    AppScreen.LocalPlay -> LocalPlayScreen(
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onNavigateToMatchSetup = { currentScreen = AppScreen.MatchSetup }
                    )
                    AppScreen.MatchSetup -> MatchSetupScreen(
                        onNavigateBack = { currentScreen = AppScreen.LocalPlay },
                        onStartGame = { settings -> currentScreen = AppScreen.LocalGame(settings) }
                    )
                    is AppScreen.LocalGame -> LocalGameScreen(
                        settings = screen.settings,
                        onNavigateBack = { currentScreen = AppScreen.MatchSetup },
                        onMatchFinished = { detail ->
                            currentScreen = AppScreen.MatchStats(detail = detail, fromHistory = false)
                        }
                    )
                    AppScreen.OnlinePlay -> OnlinePlayScreen(
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onNavigateToLogin = { currentScreen = AppScreen.Login },
                        onNavigateToCreateLobby = { currentScreen = AppScreen.CreateLobby },
                        onNavigateToLobby = { lobbyId -> currentScreen = AppScreen.LobbyRoom(lobbyId) }
                    )
                    AppScreen.CreateLobby -> CreateLobbyScreen(
                        onNavigateBack = { currentScreen = AppScreen.OnlinePlay },
                        onLobbyCreated = { lobbyId -> currentScreen = AppScreen.LobbyRoom(lobbyId) }
                    )
                    is AppScreen.LobbyRoom -> LobbyRoomScreen(
                        lobbyId = screen.lobbyId,
                        onNavigateBack = { currentScreen = AppScreen.OnlinePlay }
                    )
                    AppScreen.Training -> TrainingScreen(
                        onNavigateBack = { currentScreen = AppScreen.Home },
                        onStartMode = { mode -> currentScreen = AppScreen.TrainingGame(mode) }
                    )
                    is AppScreen.TrainingGame -> TrainingGameScreen(
                        mode = screen.mode,
                        onNavigateBack = { currentScreen = AppScreen.Training }
                    )
                    is AppScreen.MatchStats -> MatchStatsScreen(
                        detail = screen.detail,
                        fromHistory = screen.fromHistory,
                        onNavigateBack = {
                            currentScreen = if (screen.fromHistory) AppScreen.Home else AppScreen.MatchSetup
                        },
                        onRematch = {
                            screen.detail.settings?.let { settings ->
                                currentScreen = AppScreen.LocalGame(settings)
                            } ?: run {
                                currentScreen = AppScreen.MatchSetup
                            }
                        },
                        onInviteRematch = { currentScreen = AppScreen.Friends }
                    )
                }
            }
        }
    }
}
