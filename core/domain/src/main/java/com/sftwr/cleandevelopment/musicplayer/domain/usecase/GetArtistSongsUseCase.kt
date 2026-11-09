package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case responsible for observing songs belonging to a specific artist name.
 */
class GetArtistSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    operator fun invoke(artistName: String): Flow<List<Song>> {
        return musicRepository.getArtistSongs(artistName)
    }
}
