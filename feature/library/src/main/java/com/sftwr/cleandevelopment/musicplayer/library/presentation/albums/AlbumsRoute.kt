package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful route wrapper for the Albums screen, collecting ViewModel states and passing to AlbumsScreen.
 */
@Composable
fun AlbumsRoute(
    viewModel: AlbumsViewModel = hiltViewModel(),
    onAlbumClick: (Long, String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumsScreen(
        albums = uiState.albums,
        isLoading = uiState.isLoading,
        onAlbumClick = onAlbumClick,
        onBackClick = onBackClick,
        onSearchClick = onSearchClick
    )
}
