package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import androidx.lifecycle.ViewModel
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Main ViewModel acting as the root-level state holder,
 * responsible for managing global dependencies such as the [PlayerManager] across the application lifecycle.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    // Injected centralized player manager handling media playback logic globally
    val playerManager: PlayerManager
) : ViewModel()