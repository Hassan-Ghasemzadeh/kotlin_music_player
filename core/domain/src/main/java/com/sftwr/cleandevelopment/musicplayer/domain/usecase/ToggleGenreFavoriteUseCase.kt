package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to bulk toggle the favorite status of all songs belonging to a genre in a single database transaction.
 */
class ToggleGenreFavoriteUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(genreName: String, isFavorite: Boolean): Result<Unit> {
        return try {
            playlistRepository.toggleGenreFavorite(genreName, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
