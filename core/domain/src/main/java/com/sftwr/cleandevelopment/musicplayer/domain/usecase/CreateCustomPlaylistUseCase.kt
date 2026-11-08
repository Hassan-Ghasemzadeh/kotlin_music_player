package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to create a custom user playlist with input validation.
 */
class CreateCustomPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(name: String): Result<Long> {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Playlist name cannot be empty"))
        }
        return try {
            val id = playlistRepository.createCustomPlaylist(trimmedName)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
