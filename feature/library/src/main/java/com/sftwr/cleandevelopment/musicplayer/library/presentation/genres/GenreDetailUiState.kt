package com.sftwr.cleandevelopment.musicplayer.library.presentation.genres

import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

data class GenreDetailUiState(
    val genreName: String = "Genre",
    val songs: List<SongUiModel> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val targetSongForPlaylist: SongUiModel? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false
)
