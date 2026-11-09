package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case responsible for observing albums belonging to a specific artist name.
 */
class GetArtistAlbumsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(artistName: String): Flow<List<Album>> {
        return musicRepository.getArtistAlbums(artistName)
    }
}
