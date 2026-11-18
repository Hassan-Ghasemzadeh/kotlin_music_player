package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Data class holding state information for the last played song,
 * including track metadata and playback position in milliseconds.
 */
data class LastPlayedSongState(
    val songId: String,
    val title: String,
    val artist: String,
    val uri: String,
    val duration: String,
    val durationInMs: Long,
    val positionMs: Long,
    val album: String = "",
    val albumId: Long = 0L,
    val albumArtUri: String = "",
    val year: String = "",
    val genre: String = ""
)
