package com.sftwr.cleandevelopment.musicplayer.library.presentation.artists

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

data class ArtistDetailUiState(
    val artistName: String,
    val albums: List<Album> = emptyList(),
    val songs: List<SongUiModel> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val targetSongForPlaylist: SongUiModel? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false
)
