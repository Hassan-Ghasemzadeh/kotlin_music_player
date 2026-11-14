package com.sftwr.cleandevelopment.musicplayer.library.core.manager

import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain interface defining basic audio playback control contract
 * following Clean Architecture and Dependency Inversion principles.
 */
interface PlayerController {
    val currentSong: StateFlow<SongUiModel?>
    val isPlaying: StateFlow<Boolean>
    fun togglePlayPause()
    fun playNext()
    fun playPrevious()
}
