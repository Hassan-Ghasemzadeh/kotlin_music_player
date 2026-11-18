package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import javax.inject.Inject

class SaveLyricUseCase @Inject constructor(
    private val lyricRepository: LyricRepository
) {
    suspend operator fun invoke(lyric: Lyric) {
        lyricRepository.saveLyric(lyric)
    }
}
