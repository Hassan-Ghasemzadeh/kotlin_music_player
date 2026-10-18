package com.sftwr.cleandevelopment.musicplayer.domain.model

import android.net.Uri

/**
 * Domain model representing an audio track item retrieved from device storage.
 */
data class Song(
    val id: Long,                   // Unique file identifier in MediaStore (required for loading track)
    val title: String,              // Song title
    val artist: String,             // Artist name
    val album: String,              // Album title
    val albumId: Long,              // Album identifier (used to fetch official cover art via ContentResolver)
    val contentUri: Uri,            // Direct Uri used by ExoPlayer for audio playback
    val albumArtUri: String? = null,// Album art Uri or URL string (nullable if cover image is missing)
    val duration: Long = 0L,        // Song duration in milliseconds
    val size: Long = 0L,            // File size in bytes
    val path: String = ""           // Full absolute path on device storage (useful for folder or filter management)
)