package com.sftwr.cleandevelopment.musicplayer.domain.repository


import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun getDeviceSongs(): Flow<List<Song>>

    /**
     * Searches for songs matching the given query string.
     */
    suspend fun searchSongs(query: String): List<Song>
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

sealed interface DeleteResult {
    data object Success : DeleteResult
    data class RequiresPermission(val intentSender: android.content.IntentSender) : DeleteResult
    data object Error : DeleteResult
}

sealed interface UpdateTagResult {
    object Success : UpdateTagResult
    data class RequiresPermission(val intentSender: android.content.IntentSender) : UpdateTagResult
    data class Error(val message: String) : UpdateTagResult
}