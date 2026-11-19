package com.sftwr.cleandevelopment.musicplayer.domain.player

/**
 * Domain bridge interface defining playback control commands from external components
 * (such as Glance AppWidgets or system integrations) to the active media player engine.
 */
interface PlayerControlBridge {
    fun togglePlayPause()
    fun playNext()
    fun playPrevious()
    fun toggleShuffle()
    fun toggleFavorite(songId: Long, isFavorite: Boolean)
    fun seekTo(positionMs: Long)
}
