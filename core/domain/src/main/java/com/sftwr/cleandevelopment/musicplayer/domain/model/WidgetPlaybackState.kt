package com.sftwr.cleandevelopment.musicplayer.domain.model

import com.sftwr.cleandevelopment.musicplayer.model.Lyric

/**
 * Domain model representing reactive playback state for Glance widgets and presentation.
 */
data class WidgetPlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val isFavorite: Boolean = false,
    val hasNext: Boolean = true,
    val hasPrevious: Boolean = true,
    val lyrics: Lyric? = null,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculates current playback progress as a float ratio between 0.0 and 1.0.
     */
    val progressRatio: Float
        get() = if (durationMs > 0L) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    /**
     * Formatted string showing progress percentage (e.g., "45%").
     */
    val progressPercentageText: String
        get() = "${(progressRatio * 100).toInt()}%"

    /**
     * Finds the index of the currently active time-synced lyric line based on [currentPositionMs].
     */
    val currentLyricIndex: Int
        get() {
            val lines = lyrics?.lines ?: return -1
            if (lines.isEmpty()) return -1
            val index = lines.indexOfLast { it.timeMs <= currentPositionMs }
            return if (index >= 0) index else 0
        }

    /**
     * Gets the currently active lyric line based on [currentLyricIndex].
     */
    val activeLyricLine: com.sftwr.cleandevelopment.musicplayer.model.LyricLine?
        get() {
            val idx = currentLyricIndex
            val lines = lyrics?.lines ?: return null
            return lines.getOrNull(idx)
        }
}
