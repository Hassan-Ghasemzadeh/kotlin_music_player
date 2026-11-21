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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ShortcutCommand {
    data object ResumePlayback : ShortcutCommand
    data object ShuffleAll : ShortcutCommand
    data object OpenFavorites : ShortcutCommand
    data object OpenSearch : ShortcutCommand
}

/**
 * Main ViewModel acting as the root-level state holder,
 * responsible for managing global dependencies such as [PlayerManager], shortcut intents, and initial locale restoration.
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
            started = SharingStarted.Eagerly,
            initialValue = getSettingsUseCase.getInitialSettings()
        )

    private val _shortcutCommand = MutableStateFlow<ShortcutCommand?>(null)
    val shortcutCommand: StateFlow<ShortcutCommand?> = _shortcutCommand.asStateFlow()

    init {
        viewModelScope.launch {
            settingsState
                .map { it.language }
                .distinctUntilChanged()
                .collect { language ->
                    LocaleHelper.applyLanguage(context, language)
                }
        }
    }

    fun handleShortcutAction(action: String?) {
        if (action.isNullOrBlank()) return
        viewModelScope.launch {
            when (action) {
                ACTION_RESUME_PLAYBACK -> {
                    playerManager.resumePlayback()
                    _shortcutCommand.value = ShortcutCommand.ResumePlayback
                }
                ACTION_SHUFFLE_ALL -> {
                    playerManager.shuffleAll()
                    _shortcutCommand.value = ShortcutCommand.ShuffleAll
                }
                ACTION_OPEN_FAVORITES -> {
                    _shortcutCommand.value = ShortcutCommand.OpenFavorites
                }
                ACTION_OPEN_SEARCH -> {
                    _shortcutCommand.value = ShortcutCommand.OpenSearch
                }
            }
        }
    }

    fun clearShortcutCommand() {
        _shortcutCommand.value = null
    }

    companion object {
        const val ACTION_RESUME_PLAYBACK = "com.sftwr.cleandevelopment.musicplayer.RESUME_PLAYBACK"
        const val ACTION_SHUFFLE_ALL = "com.sftwr.cleandevelopment.musicplayer.SHUFFLE_ALL"
        const val ACTION_OPEN_FAVORITES = "com.sftwr.cleandevelopment.musicplayer.OPEN_FAVORITES"
        const val ACTION_OPEN_SEARCH = "com.sftwr.cleandevelopment.musicplayer.OPEN_SEARCH"
    }
}
