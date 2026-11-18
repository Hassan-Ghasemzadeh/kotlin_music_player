package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.LastPlayedSongState
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining persistence operations for song playback preferences,
 * such as storing and retrieving the last played song and position across app sessions.
 */
interface SongPreferencesRepository {

    /**
     * Persists the given song ID as the last played song.
     */
    suspend fun saveLastPlayedSongId(songId: String)

    /**
     * Retrieves the persisted last played song ID, or null if no song ID is stored.
     */
    suspend fun getLastPlayedSongId(): String?

    /**
     * Observes changes to the last played song ID as a reactive Flow.
     */
    fun getLastPlayedSongIdFlow(): Flow<String?>

    /**
     * Persists full details and playback position of the last played song.
     */
    suspend fun saveLastPlayedSong(songState: LastPlayedSongState)

    /**
     * Retrieves full details and playback position of the last played song.
     */
    suspend fun getLastPlayedSong(): LastPlayedSongState?

    /**
     * Observes changes to the last played song state as a reactive Flow.
     */
    fun getLastPlayedSongFlow(): Flow<LastPlayedSongState?>

    /**
     * Clears any persisted last played song data.
     */
    suspend fun clearLastPlayedSongId()
}

