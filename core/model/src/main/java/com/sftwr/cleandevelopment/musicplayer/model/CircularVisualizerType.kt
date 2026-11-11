package com.sftwr.cleandevelopment.musicplayer.model

/**
 * Enum representing circular visualizer styles with unique keys and titles.
 */
enum class CircularVisualizerType(val key: String, val title: String) {
    NEON_ORBITAL_WAVE("neon_orbital_wave", "NEON ORBITAL WAVE"),
    NEON_POLYGON_RING("neon_polygon_ring", "NEON POLYGON RING"),
    NEON_FREQUENCY_BARS("neon_frequency_bars", "NEON FREQUENCY BARS"),
    NEON_SPIRAL_PULSE("neon_spiral_pulse", "NEON SPIRAL PULSE"),
    NEON_CRYSTAL_MATRIX("neon_crystal_matrix", "NEON CRYSTAL MATRIX"),
    NEON_HARMONIC_RINGS("neon_harmonic_rings", "NEON HARMONIC RINGS"),
    NEON_CYMATICS_PATTERN("neon_cymatics_pattern", "NEON CYMATICS PATTERN"),
    NEON_STARBURST_SPIKES("neon_starburst_spikes", "NEON STARBURST SPIKES"),
    NEON_FRACTAL_FLOWER("neon_fractal_flower", "NEON FRACTAL FLOWER"),
    NEON_SINGULARITY_VORTEX("neon_singularity_vortex", "NEON SINGULARITY VORTEX"),
    NEON_QUANTUM_FLUX("neon_quantum_flux", "NEON QUANTUM FLUX"),
    NEON_DNA_HELIX_PULSE("neon_dna_helix_pulse", "NEON DNA HELIX PULSE"),
    NEON_HYPERSPACE_TUNNEL("neon_hyperspace_tunnel", "NEON HYPERSPACE TUNNEL"),
    NEON_NEURAL_SYNAPSE("neon_neural_synapse", "NEON NEURAL SYNAPSE"),
    NEON_PLASMA_STORM("neon_plasma_storm", "NEON PLASMA STORM"),
    NEON_SUPERNOVA_BURST("neon_supernova_burst", "NEON SUPERNOVA BURST"),
    NEON_CYBER_MATRIX_GRID("neon_cyber_matrix_grid", "NEON CYBER MATRIX GRID"),
    NEON_GRAVITATIONAL_RING("neon_gravitational_ring", "NEON GRAVITATIONAL RING");

    companion object {
        fun fromKey(key: String): CircularVisualizerType {
            return entries.find { it.key.equals(key, ignoreCase = true) || it.name.equals(key, ignoreCase = true) } ?: NEON_FREQUENCY_BARS
        }
    }
}
