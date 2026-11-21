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
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

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
        if (audioSessionId <= 0) return

        if (audioSessionId == currentSessionId && (presetReverb != null || virtualizer != null)) {
            applyCurrentStateToHardware()
            return
        }

        releaseEffectsInternal()
        currentSessionId = audioSessionId

        runCatching {
            @Suppress("DEPRECATION")
            val reverb = PresetReverb(0, audioSessionId)
            presetReverb = reverb
            Log.d(tag, "PresetReverb initialized for session $audioSessionId")
        }.onFailure { e ->
            Log.e(tag, "Failed to initialize PresetReverb for session $audioSessionId: ${e.message}")
            presetReverb = null
        }

        runCatching {
            @Suppress("DEPRECATION")
            val virt = Virtualizer(0, audioSessionId)
            virtualizer = virt
            Log.d(tag, "Virtualizer initialized for session $audioSessionId")
        }.onFailure { e ->
            Log.w(tag, "Failed to initialize native Virtualizer for session $audioSessionId: ${e.message}")
            virtualizer = null
        }

        applyCurrentStateToHardware()
    }

    @Synchronized
    private fun applyCurrentStateToHardware() {
        val state = activeState
        val isEnabled = state.isEnabled

        presetReverb?.let { reverb ->
            runCatching {
                if (isEnabled) {
                    val presetPreset = when (state.currentEffect) {
                        SoundEffect.ELECTRONIC_TUBE -> PresetReverb.PRESET_LARGEHALL
                        SoundEffect.THREE_D_ROTATE -> PresetReverb.PRESET_MEDIUMHALL
                        SoundEffect.TONE_LOW -> PresetReverb.PRESET_LARGEROOM
                        SoundEffect.SURROUND_SOUND -> PresetReverb.PRESET_PLATE
                        SoundEffect.MAGIC_SOUND -> PresetReverb.PRESET_MEDIUMROOM
                        SoundEffect.LIVE_TREBLE -> PresetReverb.PRESET_SMALLROOM
                    }
                    runCatching { reverb.preset = presetPreset }
                        .onFailure {
                            Log.w(tag, "Preset $presetPreset not supported, falling back to SMALLROOM")
                            runCatching { reverb.preset = PresetReverb.PRESET_SMALLROOM }
                        }
                    reverb.enabled = true
                } else {
                    reverb.enabled = false
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying PresetReverb hardware settings: ${e.message}")
                runCatching { reverb.enabled = false }
            }
        }

        virtualizer?.let { virt ->
            runCatching {
                val isSupported = runCatching { virt.strengthSupported }.getOrDefault(false)
                if (isEnabled && isSupported) {
                    val clampedBoost = state.volumeOrBoost.coerceIn(0f, 1f)
                    val strength = (clampedBoost * 1000f).toInt().coerceIn(0, 1000).toShort()
                    virt.setStrength(strength)
                    virt.enabled = true
                } else {
                    virt.enabled = false
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying Virtualizer hardware settings: ${e.message}")
                runCatching { virt.enabled = false }
            }
        }
    }

    @Synchronized
    fun releaseEffects() {
        releaseEffectsInternal()
    }

    private fun releaseEffectsInternal() {
        presetReverb?.let { reverb ->
            runCatching { reverb.enabled = false }
            runCatching { reverb.release() }
        }
        presetReverb = null

        virtualizer?.let { virt ->
            runCatching { virt.enabled = false }
            runCatching { virt.release() }
        }
        virtualizer = null

        currentSessionId = -1
    }
}
