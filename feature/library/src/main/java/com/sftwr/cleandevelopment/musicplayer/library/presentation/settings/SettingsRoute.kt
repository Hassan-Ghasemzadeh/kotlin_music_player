package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful route wrapper for the Settings screen collecting ViewModel state.
 */
@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isGlassmorphicEnabled by viewModel.isGlassmorphicEnabled.collectAsStateWithLifecycle()
    val isAlbumArtworkEnabled by viewModel.isAlbumArtworkEnabled.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        isGlassmorphicEnabled = isGlassmorphicEnabled,
        isAlbumArtworkEnabled = isAlbumArtworkEnabled,
        onBackClick = onBackClick,
        onLanguageClick = { viewModel.showLanguageDialog(true) },
        onLanguageSelected = viewModel::onLanguageSelected,
        onLanguageDialogDismiss = { viewModel.showLanguageDialog(false) },
        onGlassmorphicToggle = viewModel::onGlassmorphicToggle,
        onAlbumArtToggle = viewModel::onAlbumArtToggle,
        onPlaybackSpeedChange = viewModel::onPlaybackSpeedChange,
        onPlaybackPitchChange = viewModel::onPlaybackPitchChange,
        onCrossfadeDurationChange = viewModel::onCrossfadeDurationChange,
        onSkipSilenceToggle = viewModel::onSkipSilenceToggle,
        onEqualizerPresetSelected = viewModel::onEqualizerPresetSelected,
        onReplayGainModeSelected = viewModel::onReplayGainModeSelected,
        onVisualizerClick = { viewModel.showVisualizerDialog(true) },
        onVisualizerDialogDismiss = { viewModel.showVisualizerDialog(false) },
        onCircularVisualizerSelected = viewModel::onCircularVisualizerSelected,
        onExcludedFoldersClick = { viewModel.showExcludedFoldersDialog(true) },
        onExcludedFoldersDialogDismiss = { viewModel.showExcludedFoldersDialog(false) },
        onAddExcludedFolder = viewModel::onAddExcludedFolder,
        onRemoveExcludedFolder = viewModel::onRemoveExcludedFolder,
        onMinTrackDurationChange = viewModel::onMinTrackDurationChange,
        onRescanMediaClick = viewModel::onRescanMedia,
        onLockscreenControlsToggle = viewModel::onLockscreenControlsToggle,
        onScreenOffPlaybackToggle = viewModel::onScreenOffPlaybackToggle,
        onHeadsetControlsToggle = viewModel::onHeadsetControlsToggle,
        onAllowExternalDeviceToggle = viewModel::onAllowExternalDevicePlayback,
        onShakeToSkipToggle = viewModel::onShakeToSkipToggle,
        onNavigateToPermissions = onNavigateToPermissions,
        modifier = modifier
    )
}
