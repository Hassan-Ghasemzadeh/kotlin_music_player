package com.sftwr.cleandevelopment.musicplayer.library.presentation.genres

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful route wrapper for Genres screen.
 */
@Composable
fun GenresRoute(
    viewModel: GenresViewModel = hiltViewModel(),
    onGenreClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GenresScreen(
        genres = uiState.genres,
        isLoading = uiState.isLoading,
        onGenreClick = onGenreClick,
        onBackClick = onBackClick,
        onSearchClick = onSearchClick
    )
}
