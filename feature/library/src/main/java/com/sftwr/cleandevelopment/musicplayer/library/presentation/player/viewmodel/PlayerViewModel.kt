package com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
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

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager,
    private val getDeviceSongsUseCase: GetDeviceSongsUseCase
) : ViewModel() {

    val exoPlayer = playerManager.exoPlayer

    val audioSessionId: Int
        @OptIn(UnstableApi::class)
        get() = try {
            exoPlayer.audioSessionId
        } catch (e: Exception) {
            0
        }

    // States are directly driven by PlayerManager or connected StateFlows
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying
    val currentSong: StateFlow<SongUiModel?> = playerManager.currentSong

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    // List of device songs
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    // Shuffle and repeat states
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

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun skipNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.play()
        }
    }

    fun skipPrevious() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
            exoPlayer.play()
        }
    }

    fun toggleShuffle() {
        val newShuffleState = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newShuffleState
        exoPlayer.shuffleModeEnabled = newShuffleState
    }

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

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        _currentPosition.value = position
    }
}

/**
 * Playback repeat modes for the media player.
 */
enum class RepeatMode { OFF, ALL, ONE }