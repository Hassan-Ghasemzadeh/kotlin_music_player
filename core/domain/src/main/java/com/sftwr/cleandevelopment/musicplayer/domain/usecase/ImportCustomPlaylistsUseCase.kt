package com.sftwr.cleandevelopment.musicplayer.domain.usecase

import android.net.Uri
import com.sftwr.cleandevelopment.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

class ImportCustomPlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(uri: Uri) {
        playlistRepository.importCustomPlaylists(uri)
    }
}
