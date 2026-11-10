package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Represents a single frequency band of the audio equalizer.
 *
 * @property index Zero-based index of the band (0..N-1).
 * @property centerFreqHz Center frequency of the band in Hertz (e.g. 60, 230, 910, 3600, 14000).
 * @property label Formatted frequency label for UI display (e.g. "60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz").
 * @property minLevelMb Minimum supported level in millibels (typically -1500 mB = -15 dB).
 * @property maxLevelMb Maximum supported level in millibels (typically +1500 mB = +15 dB).
 * @property currentLevelMb Active level for this band in millibels.
 */
data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val label: String,
    val minLevelMb: Short = -1500,
    val maxLevelMb: Short = 1500,
    val currentLevelMb: Short = 0
)

/**
 * Represents an equalizer preset configuration (e.g. Normal, Pop, Rock, Custom).
 *
 * @property id Identifier for the preset (-1 for Custom, 0..N for standard or hardware presets).
 * @property name Human-readable name of the preset.
 * @property bandLevels Map of band index to gain level in millibels.
 */
data class EqualizerPreset(
    val id: Short,
    val name: String,
    val bandLevels: Map<Short, Short> = emptyMap()
) {
    companion object {
        val CUSTOM_PRESET_ID: Short = -1
        val CUSTOM_PRESET_NAME = "Custom"

        /**
         * Standard 5-band audio presets for universal hardware fallback.
         * Band order: [60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz]
         */
        val DEFAULT_PRESETS = listOf(
            EqualizerPreset(0, "Normal", mapOf(0.toShort() to 0, 1.toShort() to 0, 2.toShort() to 0, 3.toShort() to 0, 4.toShort() to 0)),
            EqualizerPreset(1, "Classical", mapOf(0.toShort() to 500, 1.toShort() to 300, 2.toShort() to -200, 3.toShort() to 400, 4.toShort() to 400)),
            EqualizerPreset(2, "Dance", mapOf(0.toShort() to 600, 1.toShort() to 0, 2.toShort() to 200, 3.toShort() to 400, 4.toShort() to 0)),
            EqualizerPreset(3, "Flat", mapOf(0.toShort() to 0, 1.toShort() to 0, 2.toShort() to 0, 3.toShort() to 0, 4.toShort() to 0)),
            EqualizerPreset(4, "Folk", mapOf(0.toShort() to 300, 1.toShort() to 0, 2.toShort() to 0, 3.toShort() to 200, 4.toShort() to -100)),
            EqualizerPreset(5, "Heavy Metal", mapOf(0.toShort() to 400, 1.toShort() to 100, 2.toShort() to 900, 3.toShort() to 300, 4.toShort() to 0)),
            EqualizerPreset(6, "Hip Hop", mapOf(0.toShort() to 500, 1.toShort() to 300, 2.toShort() to 0, 3.toShort() to 100, 4.toShort() to 300)),
            EqualizerPreset(7, "Jazz", mapOf(0.toShort() to 400, 1.toShort() to 200, 2.toShort() to -200, 3.toShort() to 200, 4.toShort() to 500)),
            EqualizerPreset(8, "Pop", mapOf(0.toShort() to -100, 1.toShort() to 200, 2.toShort() to 500, 3.toShort() to 100, 4.toShort() to -200)),
            EqualizerPreset(9, "Rock", mapOf(0.toShort() to 500, 1.toShort() to 300, 2.toShort() to -100, 3.toShort() to 300, 4.toShort() to 500))
        )

        /**
         * Standard fallback 5 bands when hardware Equalizer instance is unavailable.
         */
        val DEFAULT_BANDS = listOf(
            EqualizerBand(0, 60, "60Hz"),
            EqualizerBand(1, 230, "230Hz"),
            EqualizerBand(2, 910, "910Hz"),
            EqualizerBand(3, 3600, "3.6kHz"),
            EqualizerBand(4, 14000, "14kHz")
        )
    }
}

/**
 * Domain model representing the full state of audio equalizer and sound effects.
 *
 * @property isEnabled Whether audio equalizer effect is active.
 * @property currentPresetId Active preset ID (-1 for Custom).
 * @property currentPresetName Active preset display name.
 * @property bands List of frequency bands and their active gain levels.
 * @property bassBoostStrength Bass Boost effect strength (0..1000).
 * @property virtualizerStrength Virtualizer effect strength (0..1000).
 * @property availablePresets List of selectable presets.
 * @property isBassBoostSupported Whether BassBoost is hardware-supported.
 * @property isVirtualizerSupported Whether Virtualizer is hardware-supported.
 */
data class EqualizerState(
    val isEnabled: Boolean = false,
    val currentPresetId: Short = 0,
    val currentPresetName: String = "Normal",
    val bands: List<EqualizerBand> = EqualizerPreset.DEFAULT_BANDS,
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val availablePresets: List<EqualizerPreset> = EqualizerPreset.DEFAULT_PRESETS,
    val isBassBoostSupported: Boolean = true,
    val isVirtualizerSupported: Boolean = true
)
