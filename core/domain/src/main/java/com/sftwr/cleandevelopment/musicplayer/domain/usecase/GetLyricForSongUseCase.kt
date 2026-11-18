package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.LyricRepository
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLyricForSongUseCase @Inject constructor(
    private val lyricRepository: LyricRepository
) {
    operator fun invoke(songId: Long): Flow<Lyric?> {
        return lyricRepository.getLyricForSong(songId)
    }

    suspend fun getDirect(songId: Long): Lyric? {
        return lyricRepository.getLyricForSongDirect(songId)
    }
}
