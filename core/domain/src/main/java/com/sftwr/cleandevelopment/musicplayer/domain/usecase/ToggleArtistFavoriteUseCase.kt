package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to bulk toggle the favorite status of all songs belonging to an artist in a single database transaction.
 */
class ToggleArtistFavoriteUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(artistName: String, isFavorite: Boolean): Result<Unit> {
        return try {
            playlistRepository.toggleArtistFavorite(artistName, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
