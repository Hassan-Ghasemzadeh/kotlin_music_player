package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.DeleteResult
import com.sftwr.cleandevelopment.musicplayer.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * Use case responsible for encapsulating the business logic of deleting a song
 * from the music repository.
 */
class DeleteSongUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    /**
     * Invokes the use case execution to delete a song by its unique identifier.
     */
    suspend operator fun invoke(songId: Long): DeleteResult {
        return musicRepository.deleteSong(songId)
    }
}