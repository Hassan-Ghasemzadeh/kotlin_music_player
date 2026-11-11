package com.sftwr.cleandevelopment.musicplayer.library.presentation.scan

/**
 * UI State representing the real-time status of the media scan process.
 *
 * @property isScanning Indicates whether a media scan is currently in progress.
 * @property currentPathBeingScanned The real-time storage directory path currently being scanned.
 * @property scannedCount Total number of audio tracks discovered during the scan.
 * @property isCompleted Indicates whether the scanning operation has finished successfully.
 */
data class ScanMediaUiState(
    val isScanning: Boolean = false,
    val currentPathBeingScanned: String = "",
    val scannedCount: Int = 0,
    val isCompleted: Boolean = false
)
