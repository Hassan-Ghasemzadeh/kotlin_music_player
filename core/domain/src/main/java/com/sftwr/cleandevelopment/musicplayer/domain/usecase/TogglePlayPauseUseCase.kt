package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import javax.inject.Inject

/**
 * UseCase to toggle playback state (Play/Pause) from widget or UI interactions.
 */
class TogglePlayPauseUseCase @Inject constructor(
    private val repository: WidgetMusicRepository
) {
    suspend operator fun invoke() {
        repository.togglePlayPause()
    }
}
