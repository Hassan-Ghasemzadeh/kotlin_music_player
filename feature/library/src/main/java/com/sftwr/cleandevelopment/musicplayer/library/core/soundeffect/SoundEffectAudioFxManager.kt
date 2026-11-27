package com.sftwr.cleandevelopment.musicplayer.library.core.soundeffect

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.os.Build
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
 * Native AudioFX Engine Manager managing real-time Android audio effects:
 * [Equalizer], [BassBoost], [Virtualizer], [LoudnessEnhancer] (API 19+), and [PresetReverb].
 *
 * Implements 6 distinct, audible, non-clipping DSP profiles for each sound preset
 * scaled dynamically by user enhancement intensity (0% to 100%).
 */
@Singleton
class SoundEffectAudioFxManager @Inject constructor(
    private val getSoundEffectsUseCase: GetSoundEffectsUseCase
) {
    private val tag = "SoundEffectAudioFxManager"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentSessionId: Int = -1
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var presetReverb: PresetReverb? = null

    private var activeState: SoundEffectState = SoundEffectState()

    init {
        scope.launch {
            getSoundEffectsUseCase().collectLatest { state ->
                activeState = state
                applyCurrentStateToHardware()
            }
        }
    }

    /**
     * Binds native AudioFX instances to the given [audioSessionId] of ExoPlayer.
     */
    @Synchronized
    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return

        if (audioSessionId == currentSessionId && hasActiveEffects()) {
            applyCurrentStateToHardware()
            return
        }

        releaseEffectsInternal()
        currentSessionId = audioSessionId

        // Initialize Equalizer
        runCatching {
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = activeState.isEnabled
            equalizer = eq
            Log.d(tag, "Equalizer initialized for session $audioSessionId with ${eq.numberOfBands} bands")
        }.onFailure { e ->
            Log.w(tag, "Failed to initialize native Equalizer for session $audioSessionId: ${e.message}")
            equalizer = null
        }

        // Initialize BassBoost
        runCatching {
            val bb = BassBoost(0, audioSessionId)
            bb.enabled = activeState.isEnabled
            bassBoost = bb
            Log.d(tag, "BassBoost initialized for session $audioSessionId")
        }.onFailure { e ->
            Log.w(tag, "Failed to initialize native BassBoost for session $audioSessionId: ${e.message}")
            bassBoost = null
        }

        // Initialize Virtualizer
        runCatching {
            @Suppress("DEPRECATION")
            val virt = Virtualizer(0, audioSessionId)
            virt.enabled = activeState.isEnabled
            virtualizer = virt
            Log.d(tag, "Virtualizer initialized for session $audioSessionId")
        }.onFailure { e ->
            Log.w(tag, "Failed to initialize native Virtualizer for session $audioSessionId: ${e.message}")
            virtualizer = null
        }

        // Initialize LoudnessEnhancer (API 19+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            runCatching {
                val le = LoudnessEnhancer(audioSessionId)
                le.enabled = activeState.isEnabled
                loudnessEnhancer = le
                Log.d(tag, "LoudnessEnhancer initialized for session $audioSessionId")
            }.onFailure { e ->
                Log.w(tag, "Failed to initialize native LoudnessEnhancer for session $audioSessionId: ${e.message}")
                loudnessEnhancer = null
            }
        }

        // Initialize PresetReverb
        runCatching {
            @Suppress("DEPRECATION")
            val rev = PresetReverb(0, audioSessionId)
            rev.enabled = activeState.isEnabled
            presetReverb = rev
            Log.d(tag, "PresetReverb initialized for session $audioSessionId")
        }.onFailure { e ->
            Log.w(tag, "Failed to initialize native PresetReverb for session $audioSessionId: ${e.message}")
            presetReverb = null
        }

        applyCurrentStateToHardware()
    }

    private fun hasActiveEffects(): Boolean {
        return equalizer != null || bassBoost != null || virtualizer != null ||
                loudnessEnhancer != null || presetReverb != null
    }

    /**
     * Applies the current active [SoundEffectState] and DSP profile scaled by intensity to native AudioFX.
     */
    @Synchronized
    private fun applyCurrentStateToHardware() {
        val state = activeState
        val isEnabled = state.isEnabled
        val intensity = state.volumeOrBoost.coerceIn(0f, 1f)
        val effect = state.currentEffect

        if (!isEnabled) {
            disableAllHardwareEffects()
            return
        }

        val dspProfile = getDspProfile(effect)

        // 1. Apply to Equalizer
        equalizer?.let { eq ->
            runCatching {
                eq.enabled = true
                val bandCount = eq.numberOfBands
                val range = eq.bandLevelRange
                val minMb = range?.get(0)?.toInt() ?: -1500
                val maxMb = range?.get(1)?.toInt() ?: 1500

                for (b in 0 until bandCount) {
                    val shortIdx = b.toShort()
                    val centerHz = runCatching { eq.getCenterFreq(shortIdx) / 1000 }.getOrDefault(1000)
                    val targetGainMb = interpolateEqGainMb(centerHz, dspProfile.refEqGainsMb)
                    val scaledGainMb = (targetGainMb * intensity).toInt().coerceIn(minMb, maxMb).toShort()
                    eq.setBandLevel(shortIdx, scaledGainMb)
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying Equalizer for effect ${effect.name}: ${e.message}")
            }
        }

        // 2. Apply to BassBoost
        bassBoost?.let { bb ->
            runCatching {
                val isSupported = runCatching { bb.strengthSupported }.getOrDefault(false)
                if (isSupported && dspProfile.targetBassBoost > 0) {
                    val strength = (dspProfile.targetBassBoost * intensity).toInt().coerceIn(0, 1000).toShort()
                    bb.setStrength(strength)
                    bb.enabled = true
                } else {
                    bb.enabled = false
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying BassBoost for effect ${effect.name}: ${e.message}")
                runCatching { bb.enabled = false }
            }
        }

        // 3. Apply to Virtualizer
        virtualizer?.let { virt ->
            runCatching {
                val isSupported = runCatching { virt.strengthSupported }.getOrDefault(false)
                if (isSupported && dspProfile.targetVirtualizer > 0) {
                    val strength = (dspProfile.targetVirtualizer * intensity).toInt().coerceIn(0, 1000).toShort()
                    virt.setStrength(strength)
                    virt.enabled = true
                } else {
                    virt.enabled = false
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying Virtualizer for effect ${effect.name}: ${e.message}")
                runCatching { virt.enabled = false }
            }
        }

        // 4. Apply to LoudnessEnhancer (API 19+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            loudnessEnhancer?.let { le ->
                runCatching {
                    if (dspProfile.targetLoudnessGainMb > 0) {
                        val gainMb = (dspProfile.targetLoudnessGainMb * intensity).toInt().coerceIn(0, 2000)
                        le.setTargetGain(gainMb)
                        le.enabled = true
                    } else {
                        le.enabled = false
                    }
                }.onFailure { e ->
                    Log.e(tag, "Error applying LoudnessEnhancer for effect ${effect.name}: ${e.message}")
                    runCatching { le.enabled = false }
                }
            }
        }

        // 5. Apply to PresetReverb
        presetReverb?.let { rev ->
            runCatching {
                if (dspProfile.targetReverbPreset != PresetReverb.PRESET_NONE) {
                    runCatching { rev.preset = dspProfile.targetReverbPreset }
                        .onFailure {
                            Log.w(tag, "Reverb preset ${dspProfile.targetReverbPreset} unsupported, falling back to SMALLROOM")
                            runCatching { rev.preset = PresetReverb.PRESET_SMALLROOM }
                        }
                    rev.enabled = true
                } else {
                    rev.enabled = false
                }
            }.onFailure { e ->
                Log.e(tag, "Error applying PresetReverb for effect ${effect.name}: ${e.message}")
                runCatching { rev.enabled = false }
            }
        }
    }

    private fun disableAllHardwareEffects() {
        equalizer?.runCatching { enabled = false }
        bassBoost?.runCatching { enabled = false }
        virtualizer?.runCatching { enabled = false }
        loudnessEnhancer?.runCatching { enabled = false }
        presetReverb?.runCatching { enabled = false }
    }

    @Synchronized
    fun releaseEffects() {
        releaseEffectsInternal()
    }

    private fun releaseEffectsInternal() {
        equalizer?.let { eq ->
            runCatching { eq.enabled = false }
            runCatching { eq.release() }
        }
        equalizer = null

        bassBoost?.let { bb ->
            runCatching { bb.enabled = false }
            runCatching { bb.release() }
        }
        bassBoost = null

        virtualizer?.let { virt ->
            runCatching { virt.enabled = false }
            runCatching { virt.release() }
        }
        virtualizer = null

        loudnessEnhancer?.let { le ->
            runCatching { le.enabled = false }
            runCatching { le.release() }
        }
        loudnessEnhancer = null

        presetReverb?.let { rev ->
            runCatching { rev.enabled = false }
            runCatching { rev.release() }
        }
        presetReverb = null

        currentSessionId = -1
        Log.d(tag, "Sound effects cleanly released")
    }

    /**
     * Internal DSP contour specification for reference frequencies:
     * 60Hz (sub-bass), 230Hz (low-mid), 910Hz (mid), 3600Hz (upper-mid), 14000Hz (highs).
     */
    private class DspProfile(
        val refEqGainsMb: DoubleArray, // 5 reference gains in mB (millibels, where 100mB = 1dB)
        val targetBassBoost: Int,      // 0..1000
        val targetVirtualizer: Int,    // 0..1000
        val targetLoudnessGainMb: Int, // 0..2000 mB
        val targetReverbPreset: Short  // PresetReverb PRESET_*
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is DspProfile) return false
            return refEqGainsMb.contentEquals(other.refEqGainsMb) &&
                    targetBassBoost == other.targetBassBoost &&
                    targetVirtualizer == other.targetVirtualizer &&
                    targetLoudnessGainMb == other.targetLoudnessGainMb &&
                    targetReverbPreset == other.targetReverbPreset
        }

        override fun hashCode(): Int {
            var result = refEqGainsMb.contentHashCode()
            result = 31 * result + targetBassBoost
            result = 31 * result + targetVirtualizer
            result = 31 * result + targetLoudnessGainMb
            result = 31 * result + targetReverbPreset
            return result
        }
    }

    private fun getDspProfile(effect: SoundEffect): DspProfile {
        return when (effect) {
            // Vacuum Tube (لامپ خلاء الکترونیکی): Analog saturation feel, low-mid warmth, sub-bass roll-off, soft treble
            SoundEffect.ELECTRONIC_TUBE -> DspProfile(
                refEqGainsMb = doubleArrayOf(-150.0, 400.0, 150.0, -200.0, -300.0),
                targetBassBoost = 150,
                targetVirtualizer = 50,
                targetLoudnessGainMb = 150,
                targetReverbPreset = PresetReverb.PRESET_SMALLROOM
            )

            // 3D Rotation (چرخش سه‌بعدی): Spatial/rotational stereo width, clear low-mids, airy treble cues
            SoundEffect.THREE_D_ROTATE -> DspProfile(
                refEqGainsMb = doubleArrayOf(0.0, -150.0, 200.0, 300.0, 450.0),
                targetBassBoost = 100,
                targetVirtualizer = 1000,
                targetLoudnessGainMb = 100,
                targetReverbPreset = PresetReverb.PRESET_MEDIUMHALL
            )

            // Deep Bass (بیس عمیق): Heavy low-end emphasis with headroom gain protection against clipping
            SoundEffect.TONE_LOW -> DspProfile(
                refEqGainsMb = doubleArrayOf(700.0, 500.0, -100.0, -200.0, -250.0),
                targetBassBoost = 950,
                targetVirtualizer = 50,
                targetLoudnessGainMb = 0, // 0 gain to protect headroom and avoid clipping
                targetReverbPreset = PresetReverb.PRESET_NONE
            )

            // Surround Sound (صدای فراگیر): Multi-channel soundstage, depth scoop + sparkling top-end
            SoundEffect.SURROUND_SOUND -> DspProfile(
                refEqGainsMb = doubleArrayOf(200.0, -100.0, -200.0, 350.0, 500.0),
                targetBassBoost = 300,
                targetVirtualizer = 900,
                targetLoudnessGainMb = 150,
                targetReverbPreset = PresetReverb.PRESET_LARGEHALL
            )

            // Magic Sound (صدای جادویی): Dynamic V-shape smile curve, spatial width & psychoacoustic shine
            SoundEffect.MAGIC_SOUND -> DspProfile(
                refEqGainsMb = doubleArrayOf(450.0, 100.0, -150.0, 400.0, 600.0),
                targetBassBoost = 400,
                targetVirtualizer = 650,
                targetLoudnessGainMb = 250,
                targetReverbPreset = PresetReverb.PRESET_PLATE
            )

            // Live Treble (تریبل زنده): Crisp vocal/instrument transients, clean low roll-off, sparkling highs
            SoundEffect.LIVE_TREBLE -> DspProfile(
                refEqGainsMb = doubleArrayOf(-200.0, -100.0, 250.0, 600.0, 750.0),
                targetBassBoost = 0,
                targetVirtualizer = 250,
                targetLoudnessGainMb = 150,
                targetReverbPreset = PresetReverb.PRESET_MEDIUMROOM
            )
        }
    }

    /**
     * Interpolates target mB gain for a hardware band center frequency (in Hz)
     * using piecewise linear interpolation across standard reference points:
     * 60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz.
     */
    private fun interpolateEqGainMb(centerHz: Int, refGains: DoubleArray): Double {
        val refFreqs = doubleArrayOf(60.0, 230.0, 910.0, 3600.0, 14000.0)
        val hz = centerHz.toDouble()

        if (hz <= refFreqs[0]) return refGains[0]
        if (hz >= refFreqs[refFreqs.lastIndex]) return refGains[refGains.lastIndex]

        for (i in 0 until refFreqs.lastIndex) {
            val f0 = refFreqs[i]
            val f1 = refFreqs[i + 1]
            if (hz in f0..f1) {
                val g0 = refGains[i]
                val g1 = refGains[i + 1]
                val ratio = (hz - f0) / (f1 - f0)
                return g0 + ratio * (g1 - g0)
            }
        }
        return 0.0
    }
}
