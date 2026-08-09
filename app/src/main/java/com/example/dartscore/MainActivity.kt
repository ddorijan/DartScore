package com.example.dartscore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import com.example.dartscore.ui.screen.HomeScreen
import com.example.dartscore.ui.screen.LoginScreen
import com.example.dartscore.ui.screen.RegisterScreen
import com.example.dartscore.ui.theme.DartScoreTheme

enum class AppScreen { HOME, LOGIN, REGISTER }

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
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = AppScreen.HOME
                }

                when (currentScreen) {
                    AppScreen.HOME -> HomeScreen(
                        onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                        onNavigateToRegister = { currentScreen = AppScreen.REGISTER }
                    )
                    AppScreen.LOGIN -> LoginScreen(
                        onNavigateToRegister = { currentScreen = AppScreen.REGISTER },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onAuthSuccess = { currentScreen = AppScreen.HOME }
                    )
                    AppScreen.REGISTER -> RegisterScreen(
                        onNavigateToLogin = { currentScreen = AppScreen.LOGIN },
                        onNavigateBack = { currentScreen = AppScreen.HOME },
                        onAuthSuccess = { currentScreen = AppScreen.HOME }
                    )
                }
            }
        }
    }
}
