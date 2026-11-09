package com.sftwr.cleandevelopment.musicplayer.library.presentation.home

// Encapsulate all user actions and callbacks for the Home screen
data class HomeActions(
    val onPlaylistClick: (String) -> Unit = {},
    val onPlaylistCardClick: (id: Long, title: String, typeName: String) -> Unit = { _, _, _ -> },
    val onSearchClick: () -> Unit = {},
    val onNavigateToTracks: () -> Unit = {},
    val onNavigateToAlbums: () -> Unit = {},
    val onNavigateToFavorites: () -> Unit = {},
    val onSongClick: (String) -> Unit = {},
    val onAddPlaylistClick: () -> Unit = {},
    val onDismissDialog: () -> Unit = {},
    val onPlaylistNameChanged: (String) -> Unit = {},
    val onProceedToTrackSelection: () -> Unit = {},
    val onToggleSongSelection: (Long) -> Unit = {},
    val onToggleSelectAll: (List<Long>) -> Unit = {},
    val onTrackSearchQueryChanged: (String) -> Unit = {},
    val onConfirmCreate: () -> Unit = {},
    val onPermissionGranted: () -> Unit = {}
)
