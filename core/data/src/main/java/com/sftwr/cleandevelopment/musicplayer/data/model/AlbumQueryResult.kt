package com.sftwr.cleandevelopment.musicplayer.data.model

/**
 * Room query result data class holding album aggregation and metadata.
 */
data class AlbumQueryResult(
    val albumId: Long,
    val title: String,
    val artist: String,
    val trackCount: Int,
    val isFavorite: Boolean
)
