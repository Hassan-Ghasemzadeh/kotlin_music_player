package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.model.FolderItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.GenreItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface defining core music data operations,
 * including media retrieval, searching, deletion, and tag updates.
 */
interface MusicRepository {

    /**
     * Observes and returns a reactive stream of device audio songs.
     */
    fun getDeviceSongs(): Flow<List<Song>>

    /**
     * Observes and returns a reactive stream of music albums derived from device songs.
     */
    fun getAlbums(): Flow<List<Album>>

    /**
     * Observes and returns songs belonging to a specific album ID.
     */
    fun getAlbumSongs(albumId: Long): Flow<List<Song>>

    /**
     * Observes and returns a reactive stream of music artists derived from device songs.
     */
    fun getArtists(): Flow<List<Artist>>

    /**
     * Observes and returns songs belonging to a specific artist name.
     */
    fun getArtistSongs(artistName: String): Flow<List<Song>>

    /**
     * Observes and returns albums belonging to a specific artist name.
     */
    fun getArtistAlbums(artistName: String): Flow<List<Album>>

    /**
     * Observes and returns a reactive stream of music genres derived from device songs.
     */
    fun getGenres(): Flow<List<GenreItem>>

    /**
     * Observes and returns songs belonging to a specific genre name.
     */
    fun getGenreSongs(genreName: String): Flow<List<Song>>

    /**
     * Observes and returns a reactive stream of music folders derived from song file paths.
     */
    fun getFolders(): Flow<List<FolderItem>>

    /**
     * Observes and returns songs belonging to a specific folder path.
     */
    fun getFolderSongs(folderPath: String): Flow<List<Song>>

    /**
     * Searches for songs matching the given query string.
     */
    suspend fun searchSongs(query: String): List<Song>

    /**
     * Deletes a specific song by its unique identifier.
     */
    suspend fun deleteSong(songId: Long): DeleteResult

    /**
     * Updates metadata tags for a specific song by its ID.
     */
    suspend fun updateSongTags(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        year: String,
        genre: String
    ): UpdateTagResult

    suspend fun syncWithDevice()
}

/**
 * Represents the outcome of a song deletion operation.
 */
sealed interface DeleteResult {
    data object Success : DeleteResult
    data class RequiresPermission(val intentSender: android.content.IntentSender) : DeleteResult
    data object Error : DeleteResult
}

/**
 * Represents the outcome of an audio tag metadata update operation.
 */
sealed interface UpdateTagResult {
    data object Success : UpdateTagResult
    data class RequiresPermission(val intentSender: android.content.IntentSender) : UpdateTagResult
    data class Error(val message: String) : UpdateTagResult
}