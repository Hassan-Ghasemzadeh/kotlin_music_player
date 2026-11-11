package com.sftwr.cleandevelopment.musicplayer.domain.model

/**
 * Domain model representing the real-time progress state of a media storage scan.
 *
 * @property isScanning True if a media scan is currently active.
 * @property currentPath Current directory or file path being actively scanned.
 * @property scannedCount Total number of audio tracks discovered or processed so far.
 * @property isCompleted True if the media scan operation has finished successfully.
 */
data class ScanProgress(
    val isScanning: Boolean = false,
    val currentPath: String = "",
    val scannedCount: Int = 0,
    val isCompleted: Boolean = false
)
