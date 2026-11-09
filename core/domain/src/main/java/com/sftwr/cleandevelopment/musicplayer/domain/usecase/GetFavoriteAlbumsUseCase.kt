package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe favorite albums from the repository.
 */
class GetFavoriteAlbumsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Album>> {
        return playlistRepository.getFavoriteAlbums()
    }
}
