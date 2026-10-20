package com.sftwr.cleandevelopment.musicplayer.library.presentation.search

import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

// Represents the UI state for the Search screen
data class SearchUiState(
    val songs: List<SongUiModel> = emptyList(),
    val query: String = "",
    val searchResults: List<SongUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false
)