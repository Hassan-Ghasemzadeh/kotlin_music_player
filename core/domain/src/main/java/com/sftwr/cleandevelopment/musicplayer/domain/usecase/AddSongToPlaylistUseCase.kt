package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

/**
 * Use case to add a song to a user-created custom playlist.
 */
class AddSongToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: Long, songId: Long): Result<Unit> {
        return try {
            playlistRepository.addSongToPlaylist(playlistId, songId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
