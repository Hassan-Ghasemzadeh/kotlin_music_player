package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing a music album with metadata properties.
 */
data class Album(
    val albumId: Long,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val albumArtUri: String? = null,
    val isFavorite: Boolean = false
)
