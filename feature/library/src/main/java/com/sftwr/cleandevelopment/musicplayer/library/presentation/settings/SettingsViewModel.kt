package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import com.sftwr.cleandevelopment.musicplayer.data.util.LocaleHelper
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.CircularVisualizerType
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.ScanMediaUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateAppearanceSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateAudioSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateControlsSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateLanguageUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.UpdateLibrarySettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val updateAppearanceSettingsUseCase: UpdateAppearanceSettingsUseCase,
    private val updateAudioSettingsUseCase: UpdateAudioSettingsUseCase,
    private val updateLibrarySettingsUseCase: UpdateLibrarySettingsUseCase,
    private val updateControlsSettingsUseCase: UpdateControlsSettingsUseCase,
    private val scanMediaUseCase: ScanMediaUseCase,
    private val playerManager: PlayerManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val isGlassmorphicEnabled: StateFlow<Boolean> = getSettingsUseCase()
        .map { it.glassmorphicStyleEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isAlbumArtworkEnabled: StateFlow<Boolean> = getSettingsUseCase()
        .map { it.isAlbumArtworkEnabled }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val crossfadeDuration: StateFlow<Int> = getSettingsUseCase()
        .map { it.crossfadeDurationSeconds }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        viewModelScope.launch {
            getSettingsUseCase().collect { settings ->
                _uiState.update { currentState ->
                    currentState.copy(
                        settings = settings,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showLanguageDialog(show: Boolean) {
        _uiState.update { it.copy(showLanguageDialog = show) }
    }

    fun showExcludedFoldersDialog(show: Boolean) {
        _uiState.update { it.copy(showExcludedFoldersDialog = show) }
    }

    fun showMinDurationDialog(show: Boolean) {
        _uiState.update { it.copy(showMinDurationDialog = show) }
    }

    fun showVisualizerDialog(show: Boolean) {
        _uiState.update { it.copy(showVisualizerDialog = show) }
    }

    fun onCircularVisualizerSelected(type: CircularVisualizerType) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setCircularVisualizerType(type.name)
            showVisualizerDialog(false)
        }
    }

    fun onLanguageSelected(language: AppLanguage) {
        viewModelScope.launch {
            updateLanguageUseCase(language)
            LocaleHelper.applyLanguage(context, language)
            showLanguageDialog(false)
        }
    }

    fun onGlassmorphicToggle(enabled: Boolean) {
        viewModelScope.launch {
            updateAppearanceSettingsUseCase.setGlassmorphicStyle(enabled)
        }
    }

    fun onAlbumArtToggle(enabled: Boolean) {
        viewModelScope.launch {
            updateAppearanceSettingsUseCase.setAlbumArtworkEnabled(enabled)
        }
    }

    fun onPlaybackSpeedChange(speed: Float) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setPlaybackSpeed(speed)
            val currentPitch = uiState.value.settings.playbackPitch
            playerManager.exoPlayer.playbackParameters = PlaybackParameters(speed, currentPitch)
        }
    }

    fun onPlaybackPitchChange(pitch: Float) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setPlaybackPitch(pitch)
            val currentSpeed = uiState.value.settings.playbackSpeed
            playerManager.exoPlayer.playbackParameters = PlaybackParameters(currentSpeed, pitch)
        }
    }

    fun onCrossfadeDurationChange(seconds: Int) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setCrossfadeDuration(seconds)
        }
    }

    fun onSkipSilenceToggle(enabled: Boolean) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setSkipSilence(enabled)
            playerManager.exoPlayer.skipSilenceEnabled = enabled
        }
    }

    fun onEqualizerPresetSelected(preset: String) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setDefaultEqualizerPreset(preset)
        }
    }

    fun onReplayGainModeSelected(mode: ReplayGainMode) {
        viewModelScope.launch {
            updateAudioSettingsUseCase.setReplayGainMode(mode)
        }
    }

    fun onAddExcludedFolder(path: String) {
        if (path.isNotBlank()) {
            viewModelScope.launch {
                updateLibrarySettingsUseCase.addExcludedFolder(path)
            }
        }
    }

    fun onRemoveExcludedFolder(path: String) {
        viewModelScope.launch {
            updateLibrarySettingsUseCase.removeExcludedFolder(path)
        }
    }

    fun onMinTrackDurationChange(seconds: Int) {
        viewModelScope.launch {
            updateLibrarySettingsUseCase.setMinTrackDuration(seconds)
        }
    }

    fun onRescanMedia() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                scanMediaUseCase()
            }
            _uiState.update { it.copy(isLoading = false, userMessage = "Rescan started") }
        }
    }

    fun onLockscreenControlsToggle(enabled: Boolean) {
        viewModelScope.launch {
            updateControlsSettingsUseCase.setLockscreenControls(enabled)
        }
    }

    fun onScreenOffPlaybackToggle(enabled: Boolean) {
        viewModelScope.launch {
            updateControlsSettingsUseCase.setScreenOffPlayback(enabled)
        }
    }

    fun onHeadsetControlsToggle(enabled: Boolean) {
        viewModelScope.launch {
            updateControlsSettingsUseCase.setHeadsetControls(enabled)
        }
    }

    fun onAllowExternalDevicePlayback(enabled: Boolean) {
        viewModelScope.launch {
            updateControlsSettingsUseCase.setAllowExternalDevicePlayback(enabled)
        }
    }

    fun onShakeToSkipToggle(enabled: Boolean) {
        viewModelScope.launch {
            updateControlsSettingsUseCase.setShakeToSkip(enabled)
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
