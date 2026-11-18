package com.sftwr.cleandevelopment.musicplayer.model

/**
 * Single line of a lyric with timestamp in milliseconds.
 */
data class LyricLine(
    val timeMs: Long,
    val text: String
)

/**
 * Domain model for song lyrics.
 */
data class Lyric(
    val songId: Long,
    val title: String,
    val artist: String,
    val content: String,
    val isSynced: Boolean = false,
    val lines: List<LyricLine> = emptyList(),
    val lastModified: Long = System.currentTimeMillis()
)
