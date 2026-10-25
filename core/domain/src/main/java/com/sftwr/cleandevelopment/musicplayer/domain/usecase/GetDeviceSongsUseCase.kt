package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import javax.inject.Inject
import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case responsible for retrieving a reactive stream of all device audio songs
 * from the music repository.
 */
class GetDeviceSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * Invokes the use case execution to observe the live list of device songs as a Flow.
     */
    operator fun invoke(): Flow<List<Song>> {
        return musicRepository.getDeviceSongs()
    }
}