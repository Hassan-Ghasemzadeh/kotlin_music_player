package com.sftwr.cleandevelopment.musicplayer.library.presentation.soundeffect

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering the Sound Effect destination in the NavGraphBuilder.
 */
fun NavGraphBuilder.soundEffectScreen(
    onBackClick: () -> Unit
) {
    composable<Screen.SoundEffect> {
        SoundEffectScreen(
            onBackClick = onBackClick
        )
    }
}
