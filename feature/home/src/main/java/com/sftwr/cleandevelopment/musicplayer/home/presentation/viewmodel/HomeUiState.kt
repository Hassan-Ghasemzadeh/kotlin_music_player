package com.sftwr.cleandevelopment.musicplayer.home.presentation.viewmodel

import com.sftwr.cleandevelopment.musicplayer.home.presentation.PlaylistUiModel

// Represents the current UI state of the home screen
data class HomeUiState(
    val playlists: List<PlaylistUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Represents one-time UI events like navigation or showing a snackbar
sealed interface HomeUiEvent {
    data class NavigateToPlayer(val songId: String) : HomeUiEvent
    data object NavigateToSearch : HomeUiEvent
    data class OpenPlaylist(val playlistTitle: String) : HomeUiEvent
}