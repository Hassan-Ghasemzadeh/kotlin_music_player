package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

/**
 * UI state for the Album Detail screen.
 */
data class AlbumDetailUiState(
    val albumId: Long = -1L,
    val albumTitle: String = "",
    val artistName: String = "",
    val songs: List<SongUiModel> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val targetSongForPlaylist: SongUiModel? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false
)
