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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    private val _shortcutCommand = MutableSharedFlow<ShortcutCommand>(extraBufferCapacity = 1)
    val shortcutCommand: SharedFlow<ShortcutCommand> = _shortcutCommand.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsState.collect { settings ->
                LocaleHelper.applyLanguage(context, settings.language)
            }
        }
    }

    fun handleShortcutAction(action: String?) {
        if (action.isNullOrBlank()) return
        viewModelScope.launch {
            when (action) {
                ACTION_RESUME_PLAYBACK -> {
                    playerManager.resumePlayback()
                    _shortcutCommand.emit(ShortcutCommand.ResumePlayback)
                }
                ACTION_SHUFFLE_ALL -> {
                    playerManager.shuffleAll()
                    _shortcutCommand.emit(ShortcutCommand.ShuffleAll)
                }
                ACTION_OPEN_FAVORITES -> {
                    _shortcutCommand.emit(ShortcutCommand.OpenFavorites)
                }
                ACTION_OPEN_SEARCH -> {
                    _shortcutCommand.emit(ShortcutCommand.OpenSearch)
                }
            }
        }
    }

    companion object {
        const val ACTION_RESUME_PLAYBACK = "com.sftwr.cleandevelopment.musicplayer.RESUME_PLAYBACK"
        const val ACTION_SHUFFLE_ALL = "com.sftwr.cleandevelopment.musicplayer.SHUFFLE_ALL"
        const val ACTION_OPEN_FAVORITES = "com.sftwr.cleandevelopment.musicplayer.OPEN_FAVORITES"
        const val ACTION_OPEN_SEARCH = "com.sftwr.cleandevelopment.musicplayer.OPEN_SEARCH"
    }
}
