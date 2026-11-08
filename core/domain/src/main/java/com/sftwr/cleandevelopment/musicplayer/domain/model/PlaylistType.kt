package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Represents the type of playlist:
 * - Default smart playlists (RECENTLY_ADDED, RECENTLY_PLAYED, MOST_PLAYED, FAVORITES)
 * - User-created custom playlists (CUSTOM)
 */
enum class PlaylistType {
    RECENTLY_ADDED,
    RECENTLY_PLAYED,
    MOST_PLAYED,
    FAVORITES,
    CUSTOM
}
