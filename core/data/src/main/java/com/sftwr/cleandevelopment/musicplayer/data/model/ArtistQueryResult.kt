package com.sftwr.cleandevelopment.musicplayer.data.model

/**
 * Room query result data class holding artist aggregation and metadata.
 */
data class ArtistQueryResult(
    val name: String,
    val trackCount: Int,
    val albumCount: Int,
    val albumId: Long,
    val isFavorite: Boolean
)
