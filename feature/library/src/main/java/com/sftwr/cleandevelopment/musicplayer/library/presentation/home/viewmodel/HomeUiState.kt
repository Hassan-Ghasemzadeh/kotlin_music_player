package com.sftwr.cleandevelopment.musicplayer.library.presentation.home.viewmodel

// Represents the current UI state of the home screen
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tracksCount: String = "",
)

// Represents one-time UI events like navigation or showing a snackbar
sealed interface HomeUiEvent {
    data class NavigateToPlayer(val songId: String) : HomeUiEvent
    data object NavigateToSearch : HomeUiEvent
    data class OpenPlaylist(val playlistTitle: String) : HomeUiEvent
}