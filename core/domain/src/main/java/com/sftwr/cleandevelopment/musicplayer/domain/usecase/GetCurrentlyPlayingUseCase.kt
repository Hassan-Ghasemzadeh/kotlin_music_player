package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.WidgetPlaybackState
import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to retrieve reactive playback state stream for widgets and UI components.
 */
class GetCurrentlyPlayingUseCase @Inject constructor(
    private val repository: WidgetMusicRepository
) {
    operator fun invoke(): Flow<WidgetPlaybackState> = repository.getWidgetPlaybackState()

    suspend fun getDirect(): WidgetPlaybackState = repository.getWidgetPlaybackStateDirect()
}
