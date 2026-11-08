package com.sftwr.cleandevelopment.musicplayer.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining persistence operations for song playback preferences,
 * such as storing and retrieving the last played song ID across app sessions.
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
     * Clears any persisted last played song ID.
     */
    suspend fun clearLastPlayedSongId()
}
