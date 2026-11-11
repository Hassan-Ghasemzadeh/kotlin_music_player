package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.ScanProgress
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MediaScanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case responsible for retrieving a reactive stream of real-time media scanning progress updates.
 */
class GetScanningProgressUseCase @Inject constructor(
    private val mediaScanRepository: MediaScanRepository
) {
    /**
     * Observes real-time scanning progress state from the repository.
     */
    operator fun invoke(): Flow<ScanProgress> {
        return mediaScanRepository.getScanningProgress()
    }
}
