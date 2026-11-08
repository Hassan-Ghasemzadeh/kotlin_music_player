package com.sftwr.cleandevelopment.musicplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs_cache")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val size: Long,
    val path: String,
    val year: String = "",
    val genre: String = "",
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis()
)
