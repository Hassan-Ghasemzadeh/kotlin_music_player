package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering the full-screen VisualizerSelection destination in the NavGraphBuilder.
 */
fun NavGraphBuilder.visualizerSelectionGraph(
    onBackClick: () -> Unit
) {
    composable<Screen.VisualizerSelection> {
        VisualizerSelectionRoute(
            onBackClick = onBackClick
        )
    }
}
