package com.sftwr.cleandevelopment.musicplayer.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs_cache ORDER BY title ASC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs_cache")
    suspend fun getAllSongsOnce(): List<SongEntity>

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs_cache WHERE path NOT IN (:activePaths)")
    suspend fun deleteStaleSongs(activePaths: List<String>)
}