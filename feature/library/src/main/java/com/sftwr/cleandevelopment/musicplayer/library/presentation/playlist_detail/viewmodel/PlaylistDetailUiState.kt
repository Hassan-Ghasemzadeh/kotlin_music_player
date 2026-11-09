package com.sftwr.cleandevelopment.musicplayer.library.presentation.playlist_detail.viewmodel

import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

/**
 * UI state for the detailed playlist view.
 */
data class PlaylistDetailUiState(
    val playlistId: Long = -1L,
    val playlistName: String = "",
    val playlistType: PlaylistType = PlaylistType.CUSTOM,
    val songs: List<SongUiModel> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val customPlaylists: List<Playlist> = emptyList(),
    val targetSongForPlaylist: SongUiModel? = null,
    val isLoading: Boolean = false,
    val isCustomPlaylist: Boolean = false,
    val error: String? = null
)
