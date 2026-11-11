package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.data.util.LocaleHelper
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel acting as the root-level state holder,
 * responsible for managing global dependencies such as [PlayerManager] and initial locale restoration.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    val playerManager: PlayerManager,
    private val getSettingsUseCase: GetSettingsUseCase,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    init {
        viewModelScope.launch {
            val settings = getSettingsUseCase().firstOrNull()
            if (settings != null) {
                LocaleHelper.applyLanguage(context, settings.language)
            }
        }
    }
}
