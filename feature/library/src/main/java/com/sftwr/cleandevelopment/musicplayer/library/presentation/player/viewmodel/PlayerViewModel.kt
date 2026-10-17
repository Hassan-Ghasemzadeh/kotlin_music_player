package com.sftwr.cleandevelopment.musicplayer.library.presentation.player.viewmodel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val exoPlayer: ExoPlayer // تزریق ExoPlayer از طریق Hilt (اگر به صورت Singleton یا در Module تعریف شده باشد)
) : ViewModel() {

    val audioSessionId: Int
        @OptIn(UnstableApi::class)
        get() = try {
            exoPlayer.audioSessionId
        } catch (e: Exception) {
            0
        }
}