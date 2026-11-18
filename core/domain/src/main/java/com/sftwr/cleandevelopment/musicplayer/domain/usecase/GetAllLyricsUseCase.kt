package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLyricsUseCase @Inject constructor(
    private val lyricRepository: LyricRepository
) {
    operator fun invoke(): Flow<List<Lyric>> {
        return lyricRepository.getAllLyrics()
    }
}
