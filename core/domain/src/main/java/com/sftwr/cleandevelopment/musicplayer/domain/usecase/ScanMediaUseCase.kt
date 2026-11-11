package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.MediaScanRepository
import javax.inject.Inject

/**
 * Use case responsible for triggering a manual background scan of device media storage.
 */
class ScanMediaUseCase @Inject constructor(
    private val mediaScanRepository: MediaScanRepository
) {
    /**
     * Executes the scan operation in the media scan repository.
     */
    suspend operator fun invoke() {
        mediaScanRepository.startScan()
    }
}
