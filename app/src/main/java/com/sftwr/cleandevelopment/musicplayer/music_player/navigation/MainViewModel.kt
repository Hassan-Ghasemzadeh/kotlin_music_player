package com.sftwr.cleandevelopment.musicplayer.music_player.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sftwr.cleandevelopment.musicplayer.data.util.LocaleHelper
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppSettings
import com.sftwr.cleandevelopment.musicplayer.domain.usecase.GetSettingsUseCase
import com.sftwr.cleandevelopment.musicplayer.library.core.manager.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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

    val settingsState: StateFlow<AppSettings> = getSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    init {
        viewModelScope.launch {
            settingsState.collect { settings ->
                LocaleHelper.applyLanguage(context, settings.language)
            }
        }
    }
}
