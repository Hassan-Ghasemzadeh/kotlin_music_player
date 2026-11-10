package com.sftwr.cleandevelopment.musicplayer.library.presentation.genres

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.sftwr.cleandevelopment.musicplayer.model.Screen

/**
 * Navigation extension registering Genres screen destination in NavGraphBuilder.
 */
fun NavGraphBuilder.genresGraph(
    onGenreClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    composable<Screen.Genres> {
        GenresRoute(
            onGenreClick = onGenreClick,
            onBackClick = onBackClick,
            onSearchClick = onSearchClick
        )
    }
}
