package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import android.net.Uri
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

class ExportCustomPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(playlistIds: List<Long>, uri: Uri) {
        playlistRepository.exportCustomPlaylists(playlistIds, uri)
    }
}
