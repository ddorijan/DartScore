package com.example.dartscore.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/** Status bar, navigation bar — for full-screen roots with a custom top bar. */
fun Modifier.safeScreenEdges(): Modifier = statusBarsPadding().navigationBarsPadding()

/** Bottom inset only — use with [ScreenTopBar], which already applies the top inset. */
fun Modifier.safeScreenBottom(): Modifier = navigationBarsPadding()

/** Top inset only — e.g. embedded profile tab when the main top bar is hidden. */
fun Modifier.safeScreenTop(): Modifier = statusBarsPadding()

/** Clears text-field focus and hides the soft keyboard when tapping outside inputs. */
fun Modifier.dismissKeyboardOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    pointerInput(focusManager, keyboardController) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
            keyboardController?.hide()
        })
    }
}
