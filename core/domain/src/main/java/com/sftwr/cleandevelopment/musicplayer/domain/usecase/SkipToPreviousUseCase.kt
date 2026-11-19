package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import javax.inject.Inject

/**
 * UseCase to skip to the previous track in the playback queue.
 */
class SkipToPreviousUseCase @Inject constructor(
    private val repository: WidgetMusicRepository
) {
    suspend operator fun invoke() {
        val currentState = repository.getWidgetPlaybackStateDirect()
        if (currentState.hasPrevious) {
            repository.skipToPrevious()
        }
    }
}
