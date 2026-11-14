package com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode.viewmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppSettings
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ToggleFavoriteUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.GetCurrentSongUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.GetIsPlayingUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.SkipNextUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.SkipPreviousUseCase
import com.sftwr.cleandevelopment.musicplayer.library.domain.usecase.TogglePlayPauseUseCase
import com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode.DriveModeUiState
import com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode.CommandMatcher
import com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode.PlayerCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the Drive Mode full-screen distraction-free interface.
 * Implements clean architecture by delegating playback operations to domain Use Cases.
 */
@HiltViewModel
class DriveModeViewModel @Inject constructor(
    private val togglePlayPauseUseCase: TogglePlayPauseUseCase,
    private val skipNextUseCase: SkipNextUseCase,
    private val skipPreviousUseCase: SkipPreviousUseCase,
    private val getCurrentSongUseCase: GetCurrentSongUseCase,
    private val getIsPlayingUseCase: GetIsPlayingUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
) : ViewModel() {

    val currentSong = getCurrentSongUseCase()
    val isPlaying = getIsPlayingUseCase()

    private val settingsState: StateFlow<AppSettings> = getSettingsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val _isListening = MutableStateFlow(false)

    val uiState: StateFlow<DriveModeUiState> = combine(
        currentSong,
        isPlaying,
        _isListening
    ) { song, playing, listening ->
        DriveModeUiState(
            currentSong = song,
            isPlaying = playing,
            isFavorite = song?.isFavorite == true,
            isListening = listening
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriveModeUiState()
    )

    fun togglePlayPause() {
        togglePlayPauseUseCase()
    }

    fun skipNext() {
        skipNextUseCase()
    }

    fun skipPrevious() {
        skipPreviousUseCase()
    }

    fun toggleFavorite() {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            val songId = song.id.toLongOrNull() ?: return@launch
            toggleFavoriteUseCase(songId, !song.isFavorite)
        }
    }

    val currentLanguage: AppLanguage
        get() = settingsState.value.language

    fun setListening(listening: Boolean) {
        _isListening.value = listening
    }

    fun executeVoiceCommand(command: String) {
        val commandType = CommandMatcher.match(command, currentLanguage)
        when (commandType) {
            PlayerCommand.PLAY -> if (!isPlaying.value) togglePlayPause()
            PlayerCommand.PAUSE -> if (isPlaying.value) togglePlayPause()
            PlayerCommand.NEXT -> skipNext()
            PlayerCommand.PREVIOUS -> skipPrevious()
            PlayerCommand.UNKNOWN -> {}
        }
    }

    fun launchVoiceAssistant(context: Context) {
        // Handled via SpeechRecognizer in UI layer
    }
}
