package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import javax.inject.Inject

/**
 * UseCase to toggle favorite/like status for the currently playing song.
 */
class ToggleLikeUseCase @Inject constructor(
    private val repository: WidgetMusicRepository
) {
    suspend operator fun invoke() {
        repository.toggleLike()
    }
}
