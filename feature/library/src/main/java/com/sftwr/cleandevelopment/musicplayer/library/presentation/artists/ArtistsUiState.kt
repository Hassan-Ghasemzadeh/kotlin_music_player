package com.sftwr.cleandevelopment.musicplayer.library.presentation.artists

import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist

data class ArtistsUiState(
    val artists: List<Artist> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
