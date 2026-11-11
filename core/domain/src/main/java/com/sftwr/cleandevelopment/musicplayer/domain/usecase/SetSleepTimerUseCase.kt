package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.SleepTimerRepository
import javax.inject.Inject

class SetSleepTimerUseCase @Inject constructor(
    private val sleepTimerRepository: SleepTimerRepository
) {
    suspend operator fun invoke(durationMinutes: Int) {
        if (durationMinutes <= 0) {
            sleepTimerRepository.cancelSleepTimer()
        } else {
            sleepTimerRepository.setSleepTimer(durationMinutes)
        }
    }
}
