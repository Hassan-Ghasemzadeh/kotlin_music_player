package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering the Albums screen destination in the NavGraphBuilder.
 */
fun NavGraphBuilder.albumsGraph(
    onAlbumClick: (Long, String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    composable<Screen.Albums> {
        AlbumsRoute(
            onAlbumClick = onAlbumClick,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick
        )
    }
}
