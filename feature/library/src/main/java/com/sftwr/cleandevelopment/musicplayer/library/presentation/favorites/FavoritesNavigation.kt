package com.sftwr.cleandevelopment.musicplayer.library.presentation.favorites

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.model.Screen
import kotlinx.serialization.InternalSerializationApi

/**
 * Extension function to register the Favorites screen and its navigation graph.
 */
@OptIn(InternalSerializationApi::class)
fun NavGraphBuilder.favoritesGraph(
    playerManager: PlayerManager,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onAlbumClick: (Long, String) -> Unit,
    onArtistClick: (String) -> Unit,
    onGenreClick: (String) -> Unit = {},
    onInfoClick: (Long) -> Unit,
    onTagEditClick: (Long) -> Unit
) {
    composable<Screen.Favorites> {
        FavoritesRoute(
            playerManager = playerManager,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick,
            onGenreClick = onGenreClick,
            onInfoClick = onInfoClick,
            onTagEditClick = onTagEditClick
        )
    }
}
