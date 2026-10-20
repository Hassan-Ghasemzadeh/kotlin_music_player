package com.sftwr.cleandevelopment.musicplayer.domain.usecase


import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * Executes the search operation with the given query.
     */
    suspend operator fun invoke(query: String): List<Song> {
        if (query.isBlank()) return emptyList()
        return musicRepository.searchSongs(query.trim())
    }
}