package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import javax.inject.Inject

/**
 * UseCase to skip to the next track in the playback queue.
 */
class SkipToNextUseCase @Inject constructor(
    private val repository: WidgetMusicRepository
) {
    suspend operator fun invoke() {
        repository.skipToNext()
    }
}
