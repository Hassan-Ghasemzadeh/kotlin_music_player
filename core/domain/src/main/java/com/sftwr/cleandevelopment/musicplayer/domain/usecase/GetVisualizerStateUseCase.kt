package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.VisualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.VisualizerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve a reactive stream of the active [VisualizerState].
 */
class GetVisualizerStateUseCase @Inject constructor(
    private val visualizerRepository: VisualizerRepository
) {
    operator fun invoke(): Flow<VisualizerState> {
        return visualizerRepository.getVisualizerState()
    }
}
