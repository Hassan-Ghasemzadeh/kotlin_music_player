package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case responsible for observing songs belonging to a specific album ID.
 */
class GetAlbumSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(albumId: Long): Flow<List<Song>> {
        return musicRepository.getAlbumSongs(albumId)
    }
}
