package com.sftwr.cleandevelopment.musicplayer.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sftwr.cleandevelopment.musicplayer.data.model.LyricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricDao {

    @Query("SELECT * FROM lyrics WHERE songId = :songId LIMIT 1")
    fun getLyricBySongId(songId: Long): Flow<LyricEntity?>

    @Query("SELECT * FROM lyrics WHERE songId = :songId LIMIT 1")
    suspend fun getLyricBySongIdDirect(songId: Long): LyricEntity?

    @Query("SELECT * FROM lyrics ORDER BY lastModified DESC")
    fun getAllLyrics(): Flow<List<LyricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLyric(lyric: LyricEntity)

    @Query("DELETE FROM lyrics WHERE songId = :songId")
    suspend fun deleteLyricBySongId(songId: Long)

    @Query("UPDATE lyrics SET title = :newTitle, artist = :newArtist, lastModified = :lastModified WHERE songId = :songId")
    suspend fun renameLyric(songId: Long, newTitle: String, newArtist: String, lastModified: Long = System.currentTimeMillis())
}
