package com.sftwr.cleandevelopment.musicplayer.library.presentation.permissions

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension mapping the Permissions screen destination.
 */
fun NavGraphBuilder.permissionsGraph(
    onBackClick: () -> Unit
) {
    composable<Screen.Permissions> {
        PermissionsRoute(
            onBackClick = onBackClick
        )
    }
}
