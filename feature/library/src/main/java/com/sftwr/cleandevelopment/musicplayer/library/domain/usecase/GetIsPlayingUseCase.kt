package com.sftwr.cleandevelopment.musicplayer.library.domain.usecase

import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerController
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Clean Architecture Use Case responsible for observing current playback state.
 */
class GetIsPlayingUseCase @Inject constructor(
    private val playerController: PlayerController
) {
    operator fun invoke(): StateFlow<Boolean> {
        return playerController.isPlaying
    }
}
