package com.sftwr.cleandevelopment.musicplayer.library.presentation.artists

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

fun NavGraphBuilder.artistsGraph(
    onArtistClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    composable<Screen.Artists> {
        ArtistsRoute(
            onArtistClick = onArtistClick,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick
        )
    }
}
