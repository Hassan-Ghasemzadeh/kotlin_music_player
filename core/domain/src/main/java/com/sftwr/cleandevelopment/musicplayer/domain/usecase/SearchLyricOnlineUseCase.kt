package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import javax.inject.Inject

class SearchLyricOnlineUseCase @Inject constructor(
    private val lyricRepository: LyricRepository
) {
    suspend operator fun invoke(title: String, artist: String): Result<Lyric> {
        return lyricRepository.searchLyricOnline(title, artist)
    }
}
