package com.sftwr.cleandevelopment.musicplayer.data.model

data class CustomPlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val trackCount: Int
)
