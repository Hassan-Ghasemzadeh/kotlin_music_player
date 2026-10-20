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
}
sealed interface DeleteResult {
    data object Success : DeleteResult
    data class RequiresPermission(val intentSender: android.content.IntentSender) : DeleteResult
    data object Error : DeleteResult
}