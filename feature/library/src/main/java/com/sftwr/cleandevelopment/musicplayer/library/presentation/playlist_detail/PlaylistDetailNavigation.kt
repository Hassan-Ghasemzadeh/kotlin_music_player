package com.sftwr.cleandevelopment.musicplayer.library.presentation.playlist_detail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.model.Screen

fun NavGraphBuilder.playlistDetailGraph(
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit,
) {
    composable<Screen.PlaylistDetail> {
        PlaylistDetailRoute(
            playerManager = playerManager,
            onBackClick = onBackClick,
            onNavigateToSearch = onNavigateToSearch,
            onInfoClick = onInfoClick,
            onTagEditClick = onTagEditClick
        )
    }
}
