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
    DYNAMIC_SCALE(
        id = "dynamic_scale",
        title = "Dynamic Scale",
        description = "Rhythmic frequency bars expanding dynamically to the beat",
        type = CircularVisualizerType.NEON_FREQUENCY_BARS
    ),
    CIRCULAR_WAVE(
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
    HARMONIC_RINGS(
        id = "harmonic_rings",
        title = "Harmonic Rings",
        description = "Concentric harmonic rings expanding outward with music",
        type = CircularVisualizerType.NEON_HARMONIC_RINGS
    ),
    CYMATICS_PATTERN(
        id = "cymatics_pattern",
        title = "Cymatics Pattern",
        description = "Cymatics geometric sound pattern visualization",
        type = CircularVisualizerType.NEON_CYMATICS_PATTERN
    ),
    STARBURST_SPIKES(
        id = "starburst_spikes",
        title = "Starburst Spikes",
        description = "Starburst spikes reacting dynamically to high frequencies",
        type = CircularVisualizerType.NEON_STARBURST_SPIKES
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
    QUANTUM_FLUX(
        id = "quantum_flux",
        title = "Quantum Flux",
        description = "Quantum lines with random jumps triggered by bass spikes",
        type = CircularVisualizerType.NEON_QUANTUM_FLUX
    ),
    DNA_HELIX_PULSE(
        id = "dna_helix_pulse",
        title = "DNA Helix Pulse",
        description = "Double DNA helixes pulsing to rhythm of heart and music",
        type = CircularVisualizerType.NEON_DNA_HELIX_PULSE
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
    SUPERNOVA_BURST(
        id = "supernova_burst",
        title = "Supernova Burst",
        description = "Stellar supernova expanding and contracting with bass",
        type = CircularVisualizerType.NEON_SUPERNOVA_BURST
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
