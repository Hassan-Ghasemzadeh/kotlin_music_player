package com.sftwr.cleandevelopment.musicplayer.library.presentation.artists

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.model.Screen

fun NavGraphBuilder.artistDetailGraph(
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAlbumClick: (Long, String) -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit
) {
    composable<Screen.ArtistDetail> {
        ArtistDetailRoute(
            playerManager = playerManager,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onAlbumClick = onAlbumClick,
            onInfoClick = onInfoClick,
            onTagEditClick = onTagEditClick
        )
    }
}
