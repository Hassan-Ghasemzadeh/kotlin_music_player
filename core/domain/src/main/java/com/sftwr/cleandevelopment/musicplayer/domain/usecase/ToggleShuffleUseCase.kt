package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import javax.inject.Inject

/**
 * UseCase to toggle playback shuffle mode.
 */
class ToggleShuffleUseCase @Inject constructor(
    private val repository: WidgetMusicRepository
) {
    suspend operator fun invoke() {
        repository.toggleShuffle()
    }
}
