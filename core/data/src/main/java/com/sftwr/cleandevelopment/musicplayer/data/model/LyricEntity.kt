package com.sftwr.cleandevelopment.musicplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class LyricEntity(
    @PrimaryKey val songId: Long,
    val title: String,
    val artist: String,
    val content: String,
    val isSynced: Boolean,
    val lastModified: Long = System.currentTimeMillis()
)
