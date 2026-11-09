package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case responsible for observing the list of music artists from the repository.
 */
class GetArtistsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return musicRepository.getArtists()
    }
}
