package com.sftwr.cleandevelopment.musicplayer.library.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController
import javax.inject.Inject

/**
 * Clean Architecture Use Case responsible for toggling playback play/pause state.
 */
class TogglePlayPauseUseCase @Inject constructor(
    private val playerController: PlayerController
) {
    operator fun invoke() {
        playerController.togglePlayPause()
    }
}
