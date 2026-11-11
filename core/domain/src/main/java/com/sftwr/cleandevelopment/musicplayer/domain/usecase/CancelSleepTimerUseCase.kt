package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository
import javax.inject.Inject

class CancelSleepTimerUseCase @Inject constructor(
    private val sleepTimerRepository: SleepTimerRepository
) {
    suspend operator fun invoke() {
        sleepTimerRepository.cancelSleepTimer()
    }
}
