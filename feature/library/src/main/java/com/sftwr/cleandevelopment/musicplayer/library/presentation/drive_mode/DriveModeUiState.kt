package com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode

import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

/**
 * UI State data model for Drive Mode screen.
 */
data class DriveModeUiState(
    val currentSong: SongUiModel? = null,
    val isPlaying: Boolean = false,
    val isFavorite: Boolean = false,
    val isListening: Boolean = false
)
