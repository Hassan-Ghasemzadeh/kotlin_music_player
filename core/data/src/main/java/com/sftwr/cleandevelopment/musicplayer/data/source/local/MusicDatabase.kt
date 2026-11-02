package com.sftwr.cleandevelopment.musicplayer.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity

@Database(entities = [SongEntity::class], version = 1, exportSchema = false)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        const val DATABASE_NAME = "music_player_db"
    }
}