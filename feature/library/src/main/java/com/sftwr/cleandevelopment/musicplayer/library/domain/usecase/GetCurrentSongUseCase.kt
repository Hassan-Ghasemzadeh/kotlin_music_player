package com.sftwr.cleandevelopment.musicplayer.library.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController
import com.sftwr.cleandevelopment.musicplayer.library.presentation.tracks.SongUiModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Clean Architecture Use Case responsible for observing the currently active song.
 */
class GetCurrentSongUseCase @Inject constructor(
    private val playerController: PlayerController
) {
    operator fun invoke(): StateFlow<SongUiModel?> {
        return playerController.currentSong
    }
}
