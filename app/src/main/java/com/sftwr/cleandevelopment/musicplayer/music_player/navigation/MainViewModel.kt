package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.lifecycle.ViewModel
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Main ViewModel to hold global states like PlayerManager at the root level.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    val playerManager: PlayerManager
) : ViewModel()