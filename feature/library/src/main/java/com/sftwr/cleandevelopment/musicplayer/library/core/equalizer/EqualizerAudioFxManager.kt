package com.sftwr.cleandevelopment.musicplayer.library.core.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerBand
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.AudioEffectEngine
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetEqualizerSettingsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Engine Manager managing native Android [Equalizer], [BassBoost], and [Virtualizer] effects.
 * Handles lifecycle, hardware capabilities discovery, state restoration, and safe resource releases.
 */
@Singleton
class EqualizerAudioFxManager @Inject constructor(
    private val getEqualizerSettingsUseCase: GetEqualizerSettingsUseCase
) : AudioEffectEngine {

    private val tag = "EqualizerAudioFxManager"
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var currentSessionId: Int = -1
    private var equalizerEffect: Equalizer? = null
    private var bassBoostEffect: BassBoost? = null
    private var virtualizerEffect: Virtualizer? = null

    private var activeState: EqualizerState = EqualizerState()

    init {
        scope.launch {
            getEqualizerSettingsUseCase().collectLatest { state ->
                activeState = state
                applyCurrentStateToHardware()
            }
        }
    }

    /**
     * Connects audio effects to the given [audioSessionId] of ExoPlayer.
     */
    @Synchronized
    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return

        releaseEffects()
        currentSessionId = audioSessionId

        // Initialize Equalizer
        runCatching {
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = activeState.isEnabled
            equalizerEffect = eq
            Log.d(tag, "Equalizer initialized for session $audioSessionId with ${eq.numberOfBands} bands")
        }.onFailure { e ->
            Log.e(tag, "Failed to initialize native Equalizer: ${e.message}")
            equalizerEffect = null
        }

        // Initialize BassBoost
        runCatching {
            val bb = BassBoost(0, audioSessionId)
            bb.enabled = activeState.isEnabled && activeState.bassBoostStrength > 0
            if (bb.strengthSupported) {
                bb.setStrength(activeState.bassBoostStrength.coerceIn(0, 1000))
            }
            bassBoostEffect = bb
            Log.d(tag, "BassBoost initialized for session $audioSessionId")
        }.onFailure { e ->
            Log.e(tag, "Failed to initialize native BassBoost: ${e.message}")
            bassBoostEffect = null
        }

        // Initialize Virtualizer
        runCatching {
            val virt = Virtualizer(0, audioSessionId)
            virt.enabled = activeState.isEnabled && activeState.virtualizerStrength > 0
            if (virt.strengthSupported) {
                virt.setStrength(activeState.virtualizerStrength.coerceIn(0, 1000))
            }
            virtualizerEffect = virt
            Log.d(tag, "Virtualizer initialized for session $audioSessionId")
        }.onFailure { e ->
            Log.e(tag, "Failed to initialize native Virtualizer: ${e.message}")
            virtualizerEffect = null
        }

        applyCurrentStateToHardware()
    }

    override fun applyEqualizerSettings(audioSessionId: Int, state: EqualizerState) {
        activeState = state
        if (audioSessionId != currentSessionId) {
            attachAudioSession(audioSessionId)
        } else {
            applyCurrentStateToHardware()
        }
    }

    /**
     * Applies the current active [EqualizerState] to underlying native AudioFX instances.
     */
    private fun applyCurrentStateToHardware() {
        val state = activeState

        // Apply to Equalizer
        equalizerEffect?.let { eq ->
            runCatching {
                eq.enabled = state.isEnabled
                if (state.isEnabled) {
                    val minMb = eq.bandLevelRange?.get(0) ?: -1500
                    val maxMb = eq.bandLevelRange?.get(1) ?: 1500

                    if (state.currentPresetId >= 0 && state.currentPresetId < eq.numberOfPresets) {
                        runCatching { eq.usePreset(state.currentPresetId) }
                    } else {
                        state.bands.forEach { band ->
                            if (band.index in 0 until eq.numberOfBands) {
                                val clampedLevel = band.currentLevelMb.coerceIn(minMb, maxMb)
                                eq.setBandLevel(band.index, clampedLevel)
                            }
                        }
                    }
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying Equalizer hardware settings: ${e.message}")
            }
        }

        // Apply to BassBoost
        bassBoostEffect?.let { bb ->
            runCatching {
                val shouldEnable = state.isEnabled && state.bassBoostStrength > 0
                bb.enabled = shouldEnable
                if (shouldEnable && bb.strengthSupported) {
                    bb.setStrength(state.bassBoostStrength.coerceIn(0, 1000))
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying BassBoost hardware settings: ${e.message}")
            }
        }

        // Apply to Virtualizer
        virtualizerEffect?.let { virt ->
            runCatching {
                val shouldEnable = state.isEnabled && state.virtualizerStrength > 0
                virt.enabled = shouldEnable
                if (shouldEnable && virt.strengthSupported) {
                    virt.setStrength(state.virtualizerStrength.coerceIn(0, 1000))
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying Virtualizer hardware settings: ${e.message}")
            }
        }
    }

    /**
     * Discovers and retrieves frequency bands supported by hardware, or provides default 5-band configuration.
     */
    fun getHardwareBands(): List<EqualizerBand> {
        val eq = equalizerEffect ?: return EqualizerPreset.DEFAULT_BANDS
        return runCatching {
            val bandCount = eq.numberOfBands
            val range = eq.bandLevelRange ?: shortArrayOf(-1500, 1500)
            val minMb = range[0]
            val maxMb = range[1]

            (0 until bandCount).map { bandIdx ->
                val shortIdx = bandIdx.toShort()
                val centerHz = eq.getCenterFreq(shortIdx) / 1000
                val formattedLabel = formatFrequency(centerHz)
                val currentLevel = eq.getBandLevel(shortIdx)

                EqualizerBand(
                    index = shortIdx,
                    centerFreqHz = centerHz,
                    label = formattedLabel,
                    minLevelMb = minMb,
                    maxLevelMb = maxMb,
                    currentLevelMb = currentLevel
                )
            }
        }.getOrDefault(EqualizerPreset.DEFAULT_BANDS)
    }

    /**
     * Discovers presets supported by native hardware combined with standard presets.
     */
    fun getHardwarePresets(): List<EqualizerPreset> {
        val eq = equalizerEffect ?: return EqualizerPreset.DEFAULT_PRESETS
        return runCatching {
            val presetCount = eq.numberOfPresets
            if (presetCount <= 0) return EqualizerPreset.DEFAULT_PRESETS

            (0 until presetCount).map { presetIdx ->
                val shortIdx = presetIdx.toShort()
                val name = eq.getPresetName(shortIdx) ?: "پیش‌فرض ${presetIdx + 1}"
                EqualizerPreset(id = shortIdx, name = name)
            }
        }.getOrDefault(EqualizerPreset.DEFAULT_PRESETS)
    }

    private fun formatFrequency(freqHz: Int): String {
        return if (freqHz >= 1000) {
            val kHz = freqHz / 1000f
            if (kHz % 1f == 0f) "${kHz.toInt()}kHz" else "${"%.1f".format(kHz)}kHz"
        } else {
            "${freqHz}Hz"
        }
    }

    /**
     * Safely releases all underlying native audio effect resources.
     */
    @Synchronized
    fun releaseEffects() {
        runCatching {
            equalizerEffect?.enabled = false
            equalizerEffect?.release()
        }
        equalizerEffect = null

        runCatching {
            bassBoostEffect?.enabled = false
            bassBoostEffect?.release()
        }
        bassBoostEffect = null

        runCatching {
            virtualizerEffect?.enabled = false
            virtualizerEffect?.release()
        }
        virtualizerEffect = null

        currentSessionId = -1
        Log.d(tag, "Audio effects cleanly released")
    }
}
