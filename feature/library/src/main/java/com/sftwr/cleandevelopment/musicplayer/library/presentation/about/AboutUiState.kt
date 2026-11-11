package com.sftwr.cleandevelopment.musicplayer.library.presentation.about

import com.sftwr.cleandevelopment.musicplayer.domain.model.AppInfo

/**
 * UI State for the About Us dialog.
 */
data class AboutUiState(
    val appInfo: AppInfo = AppInfo(),
    val isLoading: Boolean = false
)
