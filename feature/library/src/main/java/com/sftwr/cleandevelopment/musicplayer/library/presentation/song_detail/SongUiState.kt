package com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail

// در SongUiModel.kt یا SongDetailUiModel.kt
data class SongDetailUiModel(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: String,
    val size: String,
    val path: String,
    val albumArtUri: Any?,
    val genre: String,
    val year: String,
    val bitrate: String,
    val sampleRate: String,
    val mimeType: String
)

data class SongDetailUiState(
    val isLoading: Boolean = false,
    val song: SongDetailUiModel? = null,
    val error: String? = null
)