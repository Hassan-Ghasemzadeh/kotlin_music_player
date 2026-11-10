package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.EqualizerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve a reactive stream of the active [EqualizerState].
 */
class GetEqualizerSettingsUseCase @Inject constructor(
    private val equalizerRepository: EqualizerRepository
) {
    operator fun invoke(): Flow<EqualizerState> {
        return equalizerRepository.getEqualizerState()
    }
}
