package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing a music artist with metadata properties.
 */
data class Artist(
    val name: String,
    val trackCount: Int,
    val albumCount: Int,
    val artistArtUri: String? = null,
    val isFavorite: Boolean = false
)
