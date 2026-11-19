package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * UseCase to observe time-synced lyrics corresponding to the currently active song.
 */
class GetSyncedLyricsUseCase @Inject constructor(
    private val lyricRepository: LyricRepository,
    private val widgetMusicRepository: WidgetMusicRepository
) {
    operator fun invoke(): Flow<Lyric?> {
        return widgetMusicRepository.getWidgetPlaybackState().flatMapLatest { state ->
            val songId = state.currentSong?.id ?: return@flatMapLatest flowOf(state.lyrics)
            lyricRepository.getLyricForSong(songId)
        }
    }
}
