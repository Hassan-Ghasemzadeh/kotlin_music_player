package com.sftwr.cleandevelopment.musicplayer.library.core.soundeffect

import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffect
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffectState
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSoundEffectsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Engine Manager managing native Android audio effects ([PresetReverb], [Virtualizer])
 * corresponding to user selected [SoundEffect] modes.
 */
@Singleton
class SoundEffectAudioFxManager @Inject constructor(
    private val getSoundEffectsUseCase: GetSoundEffectsUseCase
) {
    private val tag = "SoundEffectAudioFxManager"
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var currentSessionId: Int = -1
    private var presetReverb: PresetReverb? = null
    private var virtualizer: Virtualizer? = null

    private var activeState: SoundEffectState = SoundEffectState()

    init {
        scope.launch {
            getSoundEffectsUseCase().collectLatest { state ->
                activeState = state
                applyCurrentStateToHardware()
            }
        }
    }

    @Synchronized
    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return

        releaseEffects()
        currentSessionId = audioSessionId

        runCatching {
            @Suppress("DEPRECATION")
            val reverb = PresetReverb(0, audioSessionId)
            reverb.enabled = activeState.isEnabled
            presetReverb = reverb
        }.onFailure { e ->
            Log.e(tag, "Failed to initialize PresetReverb: ${e.message}")
            presetReverb = null
        }

        runCatching {
            @Suppress("DEPRECATION")
            val virt = Virtualizer(0, audioSessionId)
            virt.enabled = activeState.isEnabled && virt.strengthSupported
            virtualizer = virt
        }.onFailure { e ->
            Log.e(tag, "Failed to initialize Virtualizer: ${e.message}")
            virtualizer = null
        }

        applyCurrentStateToHardware()
    }

    private fun applyCurrentStateToHardware() {
        val state = activeState
        val isEnabled = state.isEnabled

        presetReverb?.let { reverb ->
            runCatching {
                reverb.enabled = isEnabled
                if (isEnabled) {
                    val presetPreset = when (state.currentEffect) {
                        SoundEffect.ELECTRONIC_TUBE -> PresetReverb.PRESET_LARGEHALL
                        SoundEffect.THREE_D_ROTATE -> PresetReverb.PRESET_MEDIUMHALL
                        SoundEffect.TONE_LOW -> PresetReverb.PRESET_LARGEROOM
                        SoundEffect.SURROUND_SOUND -> PresetReverb.PRESET_PLATE
                        SoundEffect.MAGIC_SOUND -> PresetReverb.PRESET_MEDIUMROOM
                        SoundEffect.LIVE_TREBLE -> PresetReverb.PRESET_SMALLROOM
                    }
                    reverb.preset = presetPreset
                }
            }
        }

        virtualizer?.let { virt ->
            runCatching {
                virt.enabled = isEnabled
                if (isEnabled && virt.strengthSupported) {
                    val strength = (state.volumeOrBoost * 1000f).toInt().coerceIn(0, 1000).toShort()
                    virt.setStrength(strength)
                }
            }
        }
    }

    @Synchronized
    fun releaseEffects() {
        runCatching {
            presetReverb?.enabled = false
            presetReverb?.release()
        }
        presetReverb = null

        runCatching {
            virtualizer?.enabled = false
            virtualizer?.release()
        }
        virtualizer = null

        currentSessionId = -1
    }
}
