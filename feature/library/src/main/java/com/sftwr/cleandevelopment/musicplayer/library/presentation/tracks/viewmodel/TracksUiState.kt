package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel


import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

/**
 * UI State representing the tracks screen data and loading status.
 */
data class TracksUiState(
    val songs: List<SongUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)