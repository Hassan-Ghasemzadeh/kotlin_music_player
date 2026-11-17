package com.sftwr.cleandevelopment.musicplayer.library.presentation.soundeffect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffect
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSoundEffectsUseCase
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.SaveSoundEffectConfigUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.soundeffect.SoundEffectAudioFxManager
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing reactive sound effect states and user audio controls.
 */
@HiltViewModel
class SoundEffectViewModel @Inject constructor(
    getSoundEffectsUseCase: GetSoundEffectsUseCase,
    private val saveSoundEffectConfigUseCase: SaveSoundEffectConfigUseCase,
    private val soundEffectAudioFxManager: SoundEffectAudioFxManager,
    private val playerManager: PlayerManager,
    val adManager: AdManager
) : ViewModel() {

    val uiState: StateFlow<SoundEffectState> = getSoundEffectsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SoundEffectState()
        )

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            saveSoundEffectConfigUseCase.setEnabled(enabled)
        }
    }

    fun selectEffect(effect: SoundEffect) {
        viewModelScope.launch {
            saveSoundEffectConfigUseCase.setCurrentEffect(effect)
        }
    }

    fun pausePlaybackIfPlaying(): Boolean {
        val playing = playerManager.isPlaying.value
        if (playing) {
            playerManager.exoPlayer.pause()
        }
        return playing
    }

    fun resumePlaybackIfWasPlaying(wasPlaying: Boolean) {
        if (wasPlaying && !playerManager.isPlaying.value) {
            playerManager.exoPlayer.play()
        }
    }

    fun setVolumeOrBoost(value: Float) {
        viewModelScope.launch {
            saveSoundEffectConfigUseCase.setVolumeOrBoost(value)
        }
    }
}
