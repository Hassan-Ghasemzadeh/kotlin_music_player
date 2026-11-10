package com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetEqualizerSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SaveEqualizerSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.equalizer.EqualizerAudioFxManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing reactive audio equalizer states and user effect controls.
 */
@HiltViewModel
class EqualizerViewModel @Inject constructor(
    getEqualizerSettingsUseCase: GetEqualizerSettingsUseCase,
    private val saveEqualizerSettingsUseCase: SaveEqualizerSettingsUseCase,
    private val equalizerAudioFxManager: EqualizerAudioFxManager
) : ViewModel() {

    val uiState: StateFlow<EqualizerState> = getEqualizerSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EqualizerState()
        )

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            saveEqualizerSettingsUseCase.setEnabled(enabled)
        }
    }

    fun selectPreset(preset: EqualizerPreset) {
        viewModelScope.launch {
            saveEqualizerSettingsUseCase.setPreset(preset)
        }
    }

    fun setBandLevel(bandIndex: Short, levelMb: Short) {
        viewModelScope.launch {
            saveEqualizerSettingsUseCase.setBandLevel(bandIndex, levelMb)
        }
    }

    fun setBassBoostStrength(strength: Short) {
        viewModelScope.launch {
            saveEqualizerSettingsUseCase.setBassBoost(strength)
        }
    }

    fun setVirtualizerStrength(strength: Short) {
        viewModelScope.launch {
            saveEqualizerSettingsUseCase.setVirtualizer(strength)
        }
    }
}
