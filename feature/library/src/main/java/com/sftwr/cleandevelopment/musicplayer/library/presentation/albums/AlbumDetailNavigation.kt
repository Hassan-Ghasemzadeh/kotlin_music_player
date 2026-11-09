package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering the AlbumDetail screen destination in NavGraphBuilder.
 */
fun NavGraphBuilder.albumDetailGraph(
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit
) {
    composable<Screen.AlbumDetail> {
        AlbumDetailRoute(
            playerManager = playerManager,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onInfoClick = onInfoClick,
            onTagEditClick = onTagEditClick
        )
    }
}
