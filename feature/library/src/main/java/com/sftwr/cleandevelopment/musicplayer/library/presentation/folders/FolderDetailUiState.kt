package com.sftwr.cleandevelopment.musicplayer.library.presentation.folders

import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel

data class FolderDetailUiState(
    val folderPath: String = "",
    val folderName: String = "Folder",
    val songs: List<SongUiModel> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val targetSongForPlaylist: SongUiModel? = null,
    val isLoading: Boolean = false
)
