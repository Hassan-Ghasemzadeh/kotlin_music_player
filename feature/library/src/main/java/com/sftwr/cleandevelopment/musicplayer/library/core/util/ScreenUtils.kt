package com.sftwr.cleandevelopment.musicplayer.library.core.util


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

/**
 * Calculates the standard bottom padding required to prevent content
 * from being obscured by the floating mini-player and system navigation bar.
 */
@Composable
fun rememberPlayerAwareBottomPadding(miniPlayerHeight: Dp = 80.dp): Dp {
    val navigationBarBottomPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return miniPlayerHeight + navigationBarBottomPadding + 16.dp
}