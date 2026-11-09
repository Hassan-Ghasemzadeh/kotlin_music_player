package com.sftwr.cleandevelopment.musicplayer.library.presentation.favorites

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

/**
 * UI State for the dedicated Favorites screen containing favorite tracks, albums, and future placeholders.
 */
data class FavoritesUiState(
    val selectedTabIndex: Int = 0,
    val favoriteSongs: List<SongUiModel> = emptyList(),
    val favoriteAlbums: List<Album> = emptyList(),
    val favoriteArtists: List<String> = emptyList(),
    val favoriteGenres: List<String> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val targetSongForPlaylist: SongUiModel? = null,
    val isLoading: Boolean = false
)
