package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Represents available sound effects in the music player.
 */
enum class SoundEffect(
    val id: String,
    val displayName: String,
    val description: String
) {
    ELECTRONIC_TUBE(
        id = "electronic_tube",
        displayName = "Electronic tube",
        description = "Warm vintage vacuum tube saturation and harmonic warmth"
    ),
    THREE_D_ROTATE(
        id = "three_d_rotate",
        displayName = "3D rotate",
        description = "Immersive spatial binaural 3D audio rotation"
    ),
    TONE_LOW(
        id = "tone_low",
        displayName = "Tone low",
        description = "Deep rich sub-bass resonance and weighted low-end emphasis"
    ),
    SURROUND_SOUND(
        id = "surround_sound",
        displayName = "Surround sound",
        description = "Expanded soundstage virtual multi-channel acoustics"
    ),
    MAGIC_SOUND(
        id = "magic_sound",
        displayName = "Magic sound",
        description = "Dynamic multi-band psychoacoustic spatial enhancement"
    ),
    LIVE_TREBLE(
        id = "live_treble",
        displayName = "Live treble",
        description = "Crisp vocal clarity and sparkling high-frequency sheen"
    );

    companion object {
        fun fromId(id: String?): SoundEffect {
            if (id.isNullOrBlank()) return ELECTRONIC_TUBE
            return entries.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) } ?: ELECTRONIC_TUBE
        }
    }
}

/**
 * Domain model representing the full state of audio sound effects.
 *
 * @property isEnabled Whether sound effects are globally active.
 * @property currentEffect Currently selected [SoundEffect].
 * @property volumeOrBoost Active volume or boost intensity parameter (0.0f to 1.0f).
 * @property availableEffects List of selectable sound effects.
 */
data class SoundEffectState(
    val isEnabled: Boolean = false,
    val currentEffect: SoundEffect = SoundEffect.ELECTRONIC_TUBE,
    val volumeOrBoost: Float = 0.5f,
    val availableEffects: List<SoundEffect> = SoundEffect.entries
)
