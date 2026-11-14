package com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Type-safe navigation destination graph for Drive Mode screen.
 */
fun NavGraphBuilder.driveModeGraph(
    onBackClick: () -> Unit
) {
    composable<Screen.DriveMode> {
        DriveModeRoute(
            onBackClick = onBackClick
        )
    }
}
