package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for managing music playback state and player control actions
 * specifically tailored for widget interactions and reactive state streaming.
 */
interface WidgetMusicRepository {

    /**
     * Observes reactive updates to the player playback state.
     */
    fun getWidgetPlaybackState(): Flow<WidgetPlaybackState>

    /**
     * Direct synchronous/suspend query of current playback state snapshot.
     */
    suspend fun getWidgetPlaybackStateDirect(): WidgetPlaybackState

    /**
     * Toggles play / pause state.
     */
    suspend fun togglePlayPause()

    /**
     * Skips to the next track.
     */
    suspend fun skipToNext()

    /**
     * Skips to the previous track.
     */
    suspend fun skipToPrevious()

    /**
     * Toggles shuffle playback mode.
     */
    suspend fun toggleShuffle()

    /**
     * Toggles favorite/like status for the active song.
     */
    suspend fun toggleLike()

    /**
     * Seeks playback to a specific timestamp in milliseconds.
     */
    suspend fun seekTo(positionMs: Long)

    /**
     * Updates playback position and triggers widget state refresh.
     */
    suspend fun updatePlaybackPosition(positionMs: Long)

    /**
     * Atomically mutates state and updates widget preferences.
     */
    suspend fun updateWidgetState(state: WidgetPlaybackState)

    /**
     * Synchronizes current playback state from the active player controller/ExoPlayer instance.
     */
    suspend fun syncPlaybackState(
        songId: Long?,
        title: String?,
        artist: String?,
        album: String?,
        albumArtUri: String?,
        isPlaying: Boolean,
        positionMs: Long,
        durationMs: Long,
        isShuffle: Boolean,
        isFavorite: Boolean,
        hasNext: Boolean = true,
        hasPrevious: Boolean = true
    )

    /**
     * Triggers lyric re-fetching and widget state refresh for the specified song ID.
     */
    suspend fun refreshLyricsForSong(songId: Long)
}
