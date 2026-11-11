package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering the Settings destination in the NavGraphBuilder.
 */
fun NavGraphBuilder.settingsGraph(
    onBackClick: () -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    composable<Screen.Settings> {
        SettingsRoute(
            onBackClick = onBackClick,
            onNavigateToPermissions = onNavigateToPermissions
        )
    }
}
