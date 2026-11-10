package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing a music genre with its track count.
 */
data class Genre(
    val name: String,
    val trackCount: Int
)

typealias GenreItem = Genre
