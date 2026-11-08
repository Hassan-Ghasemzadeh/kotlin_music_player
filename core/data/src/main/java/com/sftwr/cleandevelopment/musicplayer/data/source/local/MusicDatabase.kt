package com.sftwr.cleandevelopment.musicplayer.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sftwr.cleandevelopment.musicplayer.data.model.PlaylistEntity
import com.sftwr.cleandevelopment.musicplayer.data.model.PlaylistSongCrossRef
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "music_player_db"
    }
}
