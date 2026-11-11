package com.sftwr.cleandevelopment.musicplayer.data.source.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sftwr.cleandevelopment.musicplayer.data.model.AlbumQueryResult
import com.sftwr.cleandevelopment.musicplayer.data.model.ArtistQueryResult
import com.sftwr.cleandevelopment.musicplayer.data.model.GenreWithCount
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs_cache ORDER BY title ASC")
    fun getAllSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs_cache")
    suspend fun getAllSongsOnce(): List<SongEntity>

    @Query("SELECT albumId, album AS title, artist, COUNT(*) AS trackCount, MIN(albumId) AS albumId, MIN(isFavorite) AS isFavorite FROM songs_cache GROUP BY albumId ORDER BY title ASC")
    fun getAlbumsFlow(): Flow<List<AlbumQueryResult>>

    @Query("SELECT artist AS name, COUNT(*) AS trackCount, COUNT(DISTINCT albumId) AS albumCount, MIN(albumId) AS albumId, MIN(isFavorite) AS isFavorite FROM songs_cache GROUP BY artist ORDER BY name ASC")
    fun getArtistsFlow(): Flow<List<ArtistQueryResult>>

    @Query("SELECT genre, COUNT(*) AS trackCount FROM songs_cache WHERE genre != '' AND genre IS NOT NULL GROUP BY genre ORDER BY genre ASC")
    fun getGenresWithCountFlow(): Flow<List<GenreWithCount>>

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("DELETE FROM songs_cache WHERE path NOT IN (:activePaths)")
    suspend fun deleteStaleSongs(activePaths: List<String>)
}
