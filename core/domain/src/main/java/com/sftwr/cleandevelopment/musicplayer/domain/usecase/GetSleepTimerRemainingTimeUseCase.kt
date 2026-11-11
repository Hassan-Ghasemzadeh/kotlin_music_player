package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.SleepTimerState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSleepTimerRemainingTimeUseCase @Inject constructor(
    private val sleepTimerRepository: SleepTimerRepository
) {
    operator fun invoke(): Flow<SleepTimerState> {
        return sleepTimerRepository.getSleepTimerState()
    }
}
