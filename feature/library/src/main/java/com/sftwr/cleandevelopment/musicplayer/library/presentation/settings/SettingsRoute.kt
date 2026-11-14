package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Stateful route wrapper for the Settings screen collecting ViewModel state.
 */
@Composable
fun SettingsRoute(
    onBackClick: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToVisualizerSelection: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.onAddExcludedFolder(it.toString())
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val defaultEqualizerPreset by viewModel.defaultEqualizerPreset.collectAsStateWithLifecycle()
    val isGlassmorphicEnabled by viewModel.isGlassmorphicEnabled.collectAsStateWithLifecycle()
    val isAlbumArtworkEnabled by viewModel.isAlbumArtworkEnabled.collectAsStateWithLifecycle()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsStateWithLifecycle()
    val replayGainMode by viewModel.replayGainMode.collectAsStateWithLifecycle()
    val isLockscreenControlsEnabled by viewModel.isLockscreenControlsEnabled.collectAsStateWithLifecycle()
    val isScreenOffPlaybackEnabled by viewModel.isScreenOffPlaybackEnabled.collectAsStateWithLifecycle()
    val isHeadsetControlsEnabled by viewModel.isHeadsetControlsEnabled.collectAsStateWithLifecycle()
    val isAllowExternalDevicePlaybackEnabled by viewModel.isAllowExternalDevicePlaybackEnabled.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        defaultEqualizerPreset = defaultEqualizerPreset,
        isGlassmorphicEnabled = isGlassmorphicEnabled,
        isAlbumArtworkEnabled = isAlbumArtworkEnabled,
        crossfadeDuration = crossfadeDuration,
        replayGainMode = replayGainMode,
        isLockscreenControlsEnabled = isLockscreenControlsEnabled,
        isScreenOffPlaybackEnabled = isScreenOffPlaybackEnabled,
        isHeadsetControlsEnabled = isHeadsetControlsEnabled,
        isAllowExternalDevicePlaybackEnabled = isAllowExternalDevicePlaybackEnabled,
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
        onVisualizerClick = onNavigateToVisualizerSelection,
        onVisualizerDialogDismiss = { viewModel.showVisualizerDialog(false) },
        onCircularVisualizerSelected = viewModel::onCircularVisualizerSelected,
        onExcludedFoldersClick = { viewModel.showExcludedFoldersDialog(true) },
        onExcludedFoldersDialogDismiss = { viewModel.showExcludedFoldersDialog(false) },
        onBrowseFolderClick = { folderPickerLauncher.launch(null) },
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

    if (uiState.showScanMediaDialog) {
        com.sftwr.cleandevelopment.musicplayer.library.presentation.scan.ScanMediaDialog(
            onDismiss = { viewModel.showScanMediaDialog(false) }
        )
    }
}
