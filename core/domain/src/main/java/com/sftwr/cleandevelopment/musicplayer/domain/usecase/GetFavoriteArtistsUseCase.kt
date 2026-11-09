package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Artist
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe favorite artists from the repository.
 */
class GetFavoriteArtistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Artist>> {
        return playlistRepository.getFavoriteArtists()
    }
}
