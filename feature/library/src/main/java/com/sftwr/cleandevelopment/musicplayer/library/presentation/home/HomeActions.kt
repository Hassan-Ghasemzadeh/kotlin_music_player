package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

// Encapsulate all user actions and callbacks for the Home screen
data class HomeActions(
    val onPlaylistClick: (String) -> Unit = {},
    val onSearchClick: () -> Unit = {},
    val onNavigateToTracks: () -> Unit = {},
    val onSongClick: (String) -> Unit = {},
    val onAddPlaylistClick: () -> Unit = {},
    val onDismissDialog: () -> Unit = {},
    val onPlaylistNameChanged: (String) -> Unit = {},
    val onConfirmCreate: () -> Unit = {}
)