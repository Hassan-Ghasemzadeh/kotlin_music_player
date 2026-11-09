package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to toggle the favorite status of a song in the local database.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(songId: Long, isFavorite: Boolean): Result<Unit> {
        return try {
            playlistRepository.toggleFavorite(songId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
