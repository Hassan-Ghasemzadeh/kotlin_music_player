package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album

/**
 * UI state representing the albums screen data and filters.
 */
data class AlbumsUiState(
    val albums: List<Album> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
