package com.sftwr.cleandevelopment.musicplayer.data.model

/**
 * Room query result data class holding distinct genre name and track count.
 */
data class GenreWithCount(
    val genre: String,
    val trackCount: Int
)
