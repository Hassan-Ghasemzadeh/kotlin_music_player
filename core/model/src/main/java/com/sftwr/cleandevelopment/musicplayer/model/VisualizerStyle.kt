package com.sftwr.cleandevelopment.musicplayer.model

/**
 * Enum representing available audio visualizer styles with details for preview, title, description, and visualizer type.
 */
enum class VisualizerStyle(
    val id: String,
    val title: String,
    val description: String,
    val type: CircularVisualizerType
) {
    NEON_FREQUENCY_BARS(
        id = "neon_frequency_bars",
        title = "Neon Frequency Bars",
        description = "Rhythmic frequency bars expanding dynamically to the beat",
        type = CircularVisualizerType.NEON_FREQUENCY_BARS,
    ),
    NEON_ORBITAL_WAVE(
        id = "circular_wave",
        title = "Circular Wave",
        description = "Dual orbital wave reflections matching audio frequencies",
        type = CircularVisualizerType.NEON_ORBITAL_WAVE
    ),
    SPECTRUM_BARS(
        id = "spectrum_bars",
        title = "Spectrum Bars",
        description = "Geometric polygon matrix with interconnected spectrum lines",
        type = CircularVisualizerType.NEON_POLYGON_RING
    ),
    SPIRAL_PULSE(
        id = "spiral_pulse",
        title = "Spiral Pulse",
        description = "Pulsing spiral waves synchronized with the audio rhythm",
        type = CircularVisualizerType.NEON_SPIRAL_PULSE
    ),
    CRYSTAL_MATRIX(
        id = "crystal_matrix",
        title = "Crystal Matrix",
        description = "Crystalline matrix layout shifting with sound intensity",
        type = CircularVisualizerType.NEON_CRYSTAL_MATRIX
    ),
    CYMATICS_PATTERN(
        id = "cymatics_pattern",
        title = "Cymatics Pattern",
        description = "Cymatics geometric sound pattern visualization",
        type = CircularVisualizerType.NEON_CYMATICS_PATTERN
    ),
    FRACTAL_FLOWER(
        id = "fractal_flower",
        title = "Fractal Flower",
        description = "Fractal flower blooming and shrinking with audio beats",
        type = CircularVisualizerType.NEON_FRACTAL_FLOWER
    ),
    SINGULARITY_VORTEX(
        id = "singularity_vortex",
        title = "Singularity Vortex",
        description = "Rotating gravitational vortex swallowing sound waves",
        type = CircularVisualizerType.NEON_SINGULARITY_VORTEX
    ),
    HYPERSPACE_TUNNEL(
        id = "hyperspace_tunnel",
        title = "Hyperspace Tunnel",
        description = "3D light-speed hyperspace tunnel moving toward the user",
        type = CircularVisualizerType.NEON_HYPERSPACE_TUNNEL
    ),
    NEURAL_SYNAPSE(
        id = "neural_synapse",
        title = "Neural Synapse",
        description = "Interconnected neural synapse and brain light patterns",
        type = CircularVisualizerType.NEON_NEURAL_SYNAPSE
    ),
    PLASMA_STORM(
        id = "plasma_storm",
        title = "Plasma Storm",
        description = "Plasma storm with dancing electrical audio sparks",
        type = CircularVisualizerType.NEON_PLASMA_STORM
    ),
    CYBER_MATRIX_GRID(
        id = "cyber_matrix_grid",
        title = "Cyber Matrix Grid",
        description = "Digital matrix grid rising with audio frequency waves",
        type = CircularVisualizerType.NEON_CYBER_MATRIX_GRID
    ),
    GRAVITATIONAL_RING(
        id = "gravitational_ring",
        title = "Gravitational Ring",
        description = "Curved gravitational ring resembling space lensing",
        type = CircularVisualizerType.NEON_GRAVITATIONAL_RING
    ),
    CRYSTAL_SONIC(
        id = "crystal_sonic",
        title = "Crystal Sonic",
        description = "3D Crystal Sonic visualizer with audio-reactive DNA ring and particles",
        type = CircularVisualizerType.CRYSTAL_SONIC
    ),
    DYNAMIC_SCALE(
        id = "dynamic_scale",
        title = "Dynamic Scale",
        description = "Rhythmic frequency bars expanding dynamically to the beat",
        type = CircularVisualizerType.DYNAMIC_SCALE
    ),
    EXPLOSIVE_PARTICLES(
        id = "explosive_particles",
        title = "Explosive Particles",
        description = "Explosive particle burst emission surrounding central album art",
        type = CircularVisualizerType.EXPLOSIVE_PARTICLES
    );

    companion object {
        val DEFAULT = DYNAMIC_SCALE

        fun fromId(id: String?): VisualizerStyle {
            if (id.isNullOrBlank()) return DEFAULT
            return entries.find {
                it.id.equals(id, ignoreCase = true) ||
                        it.name.equals(id, ignoreCase = true) ||
                        it.type.name.equals(id, ignoreCase = true) ||
                        it.type.key.equals(id, ignoreCase = true)
            } ?: DEFAULT
        }
    }
}