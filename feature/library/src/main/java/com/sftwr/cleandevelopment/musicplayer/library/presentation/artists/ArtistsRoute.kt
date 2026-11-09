package com.sftwr.cleandevelopment.musicplayer.library.presentation.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ArtistsRoute(
    viewModel: ArtistsViewModel = hiltViewModel(),
    onArtistClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArtistsScreen(
        artists = uiState.artists,
        searchQuery = uiState.searchQuery,
        isLoading = uiState.isLoading,
        onArtistClick = onArtistClick,
        onBackClick = onBackClick,
        onSearchClick = onSearchClick,
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) }
    )
}
