package com.sftwr.cleandevelopment.musicplayer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistBackupContainer(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val playlists: List<ExportedPlaylist>
)

@Serializable
data class ExportedPlaylist(
    val name: String,
    val createdAt: Long,
    val tracks: List<ExportedSong>
)

@Serializable
data class ExportedSong(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val size: Long,
    val path: String,
    val year: String = "",
    val genre: String = ""
)

data class CustomPlaylistBackupInfo(
    val id: Long,
    val name: String,
    val trackCount: Int,
    val createdAt: Long
)
