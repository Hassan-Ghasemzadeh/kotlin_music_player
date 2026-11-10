package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Playlist
import com.sftwr.cleandevelopment.musicplayer.domain.model.PlaylistType
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface defining core playlist management operations.
 */
interface PlaylistRepository {

    /**
     * Emits a reactive list of all playlists (default smart playlists + custom user playlists).
     */
    fun getPlaylists(): Flow<List<Playlist>>

    /**
     * Emits a reactive list of songs for a specific playlist.
     */
    fun getPlaylistSongs(playlistId: Long, playlistType: PlaylistType): Flow<List<Song>>

    /**
     * Emits a reactive list of favorite albums.
     */
    fun getFavoriteAlbums(): Flow<List<Album>>

    /**
     * Emits a reactive list of favorite artists.
     */
    fun getFavoriteArtists(): Flow<List<Artist>>

    /**
     * Emits a reactive list of favorite genres.
     */
    fun getFavoriteGenres(): Flow<List<GenreItem>>

    /**
     * Creates a new custom playlist with the specified name and initial track IDs in Room database.
     */
    suspend fun createCustomPlaylist(name: String, songIds: List<Long> = emptyList()): Long

    /**
     * Deletes a custom playlist by its unique identifier.
     */
    suspend fun deleteCustomPlaylist(playlistId: Long)

    /**
     * Adds a song to a custom playlist.
     */
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)

    /**
     * Removes a song from a custom playlist.
     */
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    /**
     * Toggles the favorite status of a song.
     */
    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean)

    /**
     * Bulk toggles the favorite status of all songs in an album in a single transaction.
     */
    suspend fun toggleAlbumFavorite(albumId: Long, isFavorite: Boolean)

    /**
     * Bulk toggles the favorite status of all songs belonging to an artist in a single transaction.
     */
    suspend fun toggleArtistFavorite(artistName: String, isFavorite: Boolean)

    /**
     * Bulk toggles the favorite status of all songs belonging to a genre in a single transaction.
     */
    suspend fun toggleGenreFavorite(genreName: String, isFavorite: Boolean)

    /**
     * Records a playback event for a song to update play count and last played timestamp.
     */
    suspend fun recordSongPlayed(songId: Long)
}
