package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import com.sftwr.cleandevelopment.musicplayer.domain.model.AppSettings

/**
 * UI State for the Settings Screen.
 */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = false,
    val showLanguageDialog: Boolean = false,
    val showExcludedFoldersDialog: Boolean = false,
    val showMinDurationDialog: Boolean = false,
    val showVisualizerDialog: Boolean = false,
    val showScanMediaDialog: Boolean = false,
    val userMessage: String? = null
)
