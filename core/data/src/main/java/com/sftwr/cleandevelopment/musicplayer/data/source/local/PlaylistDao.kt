package com.sftwr.cleandevelopment.musicplayer.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sftwr.cleandevelopment.musicplayer.data.model.CustomPlaylistWithCount
import com.sftwr.cleandevelopment.musicplayer.data.model.PlaylistEntity
import com.sftwr.cleandevelopment.musicplayer.data.model.PlaylistSongCrossRef
import com.sftwr.cleandevelopment.musicplayer.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("""
        SELECT p.id AS id, p.name AS name, p.createdAt AS createdAt, COUNT(ref.songId) AS trackCount
        FROM playlists p
        LEFT JOIN playlist_song_cross_ref ref ON p.id = ref.playlistId
        GROUP BY p.id
        ORDER BY p.createdAt DESC
    """)
    fun getCustomPlaylistsWithCountFlow(): Flow<List<CustomPlaylistWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deletePlaylistSongCrossRef(playlistId: Long, songId: Long)

    @Query("SELECT COUNT(*) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    fun getPlaylistSongCountFlow(playlistId: Long): Flow<Int>

    @Query("""
        SELECT s.* FROM songs_cache s
        INNER JOIN playlist_song_cross_ref ref ON s.id = ref.songId
        WHERE ref.playlistId = :playlistId
        ORDER BY ref.addedAt ASC
    """)
    fun getPlaylistSongsFlow(playlistId: Long): Flow<List<SongEntity>>

    // Smart / Default Playlist Queries
    @Query("SELECT * FROM songs_cache ORDER BY dateAdded DESC, id DESC")
    fun getRecentlyAddedSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs_cache")
    fun getRecentlyAddedCountFlow(): Flow<Int>

    @Query("SELECT * FROM songs_cache WHERE lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC")
    fun getRecentlyPlayedSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs_cache WHERE lastPlayedTimestamp > 0")
    fun getRecentlyPlayedCountFlow(): Flow<Int>

    @Query("SELECT * FROM songs_cache WHERE playCount > 0 ORDER BY playCount DESC")
    fun getMostPlayedSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs_cache WHERE playCount > 0")
    fun getMostPlayedCountFlow(): Flow<Int>

    @Query("SELECT * FROM songs_cache WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongsFlow(): Flow<List<SongEntity>>

    @Query("SELECT COUNT(*) FROM songs_cache WHERE isFavorite = 1")
    fun getFavoriteCountFlow(): Flow<Int>

    @Query("UPDATE songs_cache SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun setFavorite(songId: Long, isFavorite: Boolean)

    @Query("UPDATE songs_cache SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :songId")
    suspend fun recordSongPlayed(songId: Long, timestamp: Long = System.currentTimeMillis())
}
