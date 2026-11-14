package com.sftwr.cleandevelopment.musicplayer.library.presentation.drive_mode.viewmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
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

    fun launchVoiceAssistant(context: Context) {
        _isListening.value = true
        val currentLanguage = settingsState.value.language
        val languageCode = currentLanguage.code // "en", "fa", "de", "tr"

        val promptText = when (currentLanguage) {
            AppLanguage.PERSIAN -> "فرمان صوتی (پخش، توقف، بعدی، قبلی)..."
            AppLanguage.GERMAN -> "Sprachbefehl (Wiedergabe, Pause, Weiter, Zurück)..."
            AppLanguage.TURKISH -> "Sesli komut (Oynat, Duraklat, Sonraki, Önceki)..."
            else -> "Play music or command (Play, Pause, Next, Previous)..."
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val errorMessage = when (currentLanguage) {
                AppLanguage.PERSIAN -> "جستجوی صوتی روی این دستگاه پشتیبانی نمی‌شود"
                AppLanguage.GERMAN -> "Sprachsuche wird auf diesem Gerät nicht unterstützt"
                AppLanguage.TURKISH -> "Sesli arama bu cihazda desteklenmiyor"
                else -> "Voice Search not supported on this device"
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            _isListening.value = false
        }
    }
}
