package com.sftwr.cleandevelopment.musicplayer.library.presentation.genres

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering GenreDetail screen destination in NavGraphBuilder.
 */
fun NavGraphBuilder.genreDetailGraph(
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit
) {
    composable<Screen.GenreDetail> {
        GenreDetailRoute(
            playerManager = playerManager,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onInfoClick = onInfoClick,
            onTagEditClick = onTagEditClick
        )
    }
}
