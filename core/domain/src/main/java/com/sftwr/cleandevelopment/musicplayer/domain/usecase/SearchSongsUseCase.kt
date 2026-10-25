package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.model.Song
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case responsible for encapsulating the search business logic,
 * ensuring blank queries are filtered out and inputs are sanitized.
 */
class SearchSongsUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * Executes the search operation with the given query string.
     * Returns an empty list if the query is blank, otherwise queries the repository.
     */
    suspend operator fun invoke(query: String): List<Song> {
        if (query.isBlank()) return emptyList()
        return musicRepository.searchSongs(query.trim())
    }
}