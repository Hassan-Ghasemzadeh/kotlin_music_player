package com.sftwr.cleandevelopment.musicplayer.domain.repository

import com.sftwr.cleandevelopment.musicplayer.domain.model.ScanProgress
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract for executing and monitoring device media scanning operations.
 */
interface MediaScanRepository {

    /**
     * Observes the real-time media scanning progress as a reactive Flow stream.
     */
    fun getScanningProgress(): Flow<ScanProgress>

    /**
     * Initiates an asynchronous background scan of device storage for audio media files.
     */
    suspend fun startScan()

    /**
     * Resets the active scanning progress state back to default/idle.
     */
    fun resetScanState()
}
