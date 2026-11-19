package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import javax.inject.Inject

/**
 * UseCase to seek playback to a target position in milliseconds.
 */
class SeekToUseCase @Inject constructor(
    private val repository: WidgetMusicRepository
) {
    suspend operator fun invoke(positionMs: Long) {
        repository.seekTo(positionMs)
    }
}
