package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.player.PlayerControlBridge
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject
import javax.inject.Provider

/**
 * Use case to toggle the favorite status of a song in the local database and update active player bridge.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playerControlBridgeProvider: Provider<PlayerControlBridge>? = null
) {
    suspend operator fun invoke(songId: Long, isFavorite: Boolean): Result<Unit> {
        return try {
            playlistRepository.toggleFavorite(songId, isFavorite)
            playerControlBridgeProvider?.get()?.toggleFavorite(songId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
