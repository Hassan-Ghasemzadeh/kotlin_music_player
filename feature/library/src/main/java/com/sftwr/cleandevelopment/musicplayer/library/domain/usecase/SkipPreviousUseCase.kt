package com.sftwr.cleandevelopment.musicplayer.library.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController
import javax.inject.Inject

/**
 * Clean Architecture Use Case responsible for skipping to the previous track in queue.
 */
class SkipPreviousUseCase @Inject constructor(
    private val playerController: PlayerController
) {
    operator fun invoke() {
        playerController.playPrevious()
    }
}
