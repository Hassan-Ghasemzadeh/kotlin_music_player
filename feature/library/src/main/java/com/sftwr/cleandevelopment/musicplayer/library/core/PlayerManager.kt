package com.sftwr.cleandevelopment.musicplayer.library.core


import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor() {

    // Currently playing song state
    private val _currentSong = MutableStateFlow<SongUiModel?>(null)
    val currentSong: StateFlow<SongUiModel?> = _currentSong.asStateFlow()

    // Play/Pause state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /**
     * Play a selected song and update the global player state.
     */
    fun playSong(song: SongUiModel) {
        _currentSong.value = song
        _isPlaying.value = true
        // TODO: Trigger actual audio playback service/player here
    }

    /**
     * Toggle play/pause state.
     */
    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }
}