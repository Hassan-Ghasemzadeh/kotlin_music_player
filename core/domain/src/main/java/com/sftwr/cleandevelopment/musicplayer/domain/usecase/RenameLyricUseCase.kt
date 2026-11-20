package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.domain.repository.WidgetMusicRepository
import javax.inject.Inject

class RenameLyricUseCase @Inject constructor(
    private val lyricRepository: LyricRepository,
    private val widgetMusicRepository: WidgetMusicRepository
) {
    suspend operator fun invoke(songId: Long, newTitle: String, newArtist: String) {
        lyricRepository.renameLyric(songId, newTitle, newArtist)
        widgetMusicRepository.refreshLyricsForSong(songId)
    }
}
