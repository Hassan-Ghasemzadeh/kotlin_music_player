package com.sftwr.cleandevelopment.musicplayer.domain.repository

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