package com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.viewmodel

import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

/**
 * UI State representing the tracks screen data, playlists for dialog, and loading status.
 */
data class TracksUiState(
    val songs: List<SongUiModel> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val targetSongForPlaylist: SongUiModel? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
