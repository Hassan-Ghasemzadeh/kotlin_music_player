package com.sftwr.cleandevelopment.musicplayer.library.core.visualizer

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot

class AudioVisualizerManager {
    private val _fftData = MutableStateFlow(FloatArray(64))
    val fftData: StateFlow<FloatArray> = _fftData.asStateFlow()

    private var visualizer: Visualizer? = null

    /**
     * Starts listening to audio frequency data from the given audio session ID.
     * Requires the RECORD_AUDIO permission.
     */
    fun startListening(audioSessionId: Int) {
        if (audioSessionId <= 0) {
            Log.w("VisualizerDebug", "Skipping: Invalid audioSessionId ($audioSessionId)")
            return
        }

        stopListening()

        try {
            val viz = Visualizer(audioSessionId).apply {
                enabled = false
                captureSize = Visualizer.getCaptureSizeRange()[1]

                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {}

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let { bytes ->
                                val bands = 64
                                val result = FloatArray(bands)
                                for (i in 0 until bands) {
                                    if (i * 2 + 1 < bytes.size) {
                                        val r = bytes[i * 2].toFloat()
                                        val im = bytes[i * 2 + 1].toFloat()
                                        val magnitude = hypot(r, im) / 128f
                                        result[i] = (magnitude * 1.8f).coerceIn(0.05f, 1f)
                                    }
                                }
                                _fftData.value = result
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    false,
                    true
                )
                enabled = true
            }
            visualizer = viz
            Log.d("VisualizerDebug", "Real Visualizer successfully started for session: $audioSessionId")
        } catch (e: Exception) {
            Log.e("VisualizerError", "Failed to initialize visualizer: ${e.message}", e)
        }
    }

    /**
     * Stops the visualizer and releases its resources, resetting the FFT data state.
     */
    fun stopListening() {
        try {
            visualizer?.let {
                it.enabled = false
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            visualizer = null
            _fftData.value = FloatArray(64)
        }
    }
}