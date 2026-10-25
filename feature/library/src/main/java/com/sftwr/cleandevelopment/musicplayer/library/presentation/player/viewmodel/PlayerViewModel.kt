package com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetDeviceSongsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * PlayerViewModel manages the audio playback states, transport controls (play, pause, skip, seek),
 * repeat/shuffle configurations, and periodic progress tracking for the media player screen.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase
) : ViewModel() {

    // Expose underlying ExoPlayer instance for audio session management
    val exoPlayer = playerManager.exoPlayer

    // Safely retrieve the active audio session ID for visualizers
    val audioSessionId: Int
        @OptIn(UnstableApi::class)
        get() = try {
            exoPlayer.audioSessionId
        } catch (_: Exception) {
            0
        }

    // States are directly driven by PlayerManager or connected StateFlows
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentSong: StateFlow<SongUiModel?> = playerManager.currentSong

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    // List of device songs loaded from storage repository
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    // Shuffle and repeat state flow streams
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    init {
        loadDeviceSongs()
        startPositionUpdater()
    }

    /**
     * Periodically updates the playback position of the currently playing track.
     */
    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                if (exoPlayer.isPlaying) {
                    _currentPosition.value = exoPlayer.currentPosition
                }
                delay(500L.milliseconds)
            }
        }
    }

    /**
     * Loads local audio tracks from device storage using the use case.
     */
    private fun loadDeviceSongs() {
        viewModelScope.launch {
            getDeviceSongsUseCase().collect { deviceSongs ->
                _songs.value = deviceSongs
            }
        }
    }

    /**
     * Toggles playback between play and pause states via player manager.
     */
    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    /**
     * Skips playback to the next track in the active queue.
     */
    fun skipNext() {
        playerManager.playNext()
    }

    /**
     * Skips playback to the previous track in the active queue.
     */
    fun skipPrevious() {
        playerManager.playPrevious()
    }

    /**
     * Toggles shuffle mode state across player manager and ExoPlayer instance.
     */
    fun toggleShuffle() {
        val newShuffleState = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newShuffleState
        exoPlayer.shuffleModeEnabled = newShuffleState
    }

    /**
     * Cycles through available playback repeat modes (OFF, ALL, ONE).
     */
    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> {
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                RepeatMode.ALL
            }

            RepeatMode.ALL -> {
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                RepeatMode.ONE
            }

            RepeatMode.ONE -> {
                exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
                RepeatMode.OFF
            }
        }
    }

    /**
     * Seeks playback position to the specified target time in milliseconds.
     */
    fun seekTo(position: Long) {
        playerManager.seekTo(position)
        _currentPosition.value = position
    }
}

/**
 * Playback repeat modes for the media player.
 */
enum class RepeatMode { OFF, ALL, ONE }