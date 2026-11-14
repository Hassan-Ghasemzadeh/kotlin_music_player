package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import com.sftwr.cleandevelopment.musicplayer.data.util.FolderFilterHelper
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.NeonBlue
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SettingSliderItem
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.SettingSwitchItem
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.domain.model.ReplayGainMode
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.CircularVisualizerType
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.AudioVisualizerManager
import com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer.VisualizerSelectorScreen
import com.sftwr.cleandevelopment.musicplayer.home.R
import java.util.Locale

fun EqualizerPreset.getStringRes(): Int {
    return when (this) {
        EqualizerPreset.OFF -> R.string.eq_preset_off
        EqualizerPreset.NORMAL -> R.string.eq_preset_normal
        EqualizerPreset.FLAT -> R.string.eq_preset_flat
        EqualizerPreset.BASS_BOOSTER -> R.string.eq_preset_bass_booster
        EqualizerPreset.TREBLE_BOOSTER -> R.string.eq_preset_treble_booster
        EqualizerPreset.ROCK -> R.string.eq_preset_rock
        EqualizerPreset.POP -> R.string.eq_preset_pop
        EqualizerPreset.JAZZ -> R.string.eq_preset_jazz
        EqualizerPreset.CLASSICAL -> R.string.eq_preset_classical
    }
}

val DarkBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F0B21),
        Color(0xFF1B1238),
        Color(0xFF2C1454)
    )
)

/**
 * Modern glassmorphic Settings Screen providing categorization across Appearance,
 * Audio Effects, Library Management, Playback Controls, and System Permissions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    defaultEqualizerPreset: EqualizerPreset = uiState.settings.defaultEqualizerPreset,
    isGlassmorphicEnabled: Boolean,
    isAlbumArtworkEnabled: Boolean,
    crossfadeDuration: Int,
    replayGainMode: ReplayGainMode = uiState.settings.replayGainMode,
    isLockscreenControlsEnabled: Boolean = uiState.settings.lockscreenControlsEnabled,
    isScreenOffPlaybackEnabled: Boolean = uiState.settings.screenOffPlaybackEnabled,
    isHeadsetControlsEnabled: Boolean = uiState.settings.headsetControlsEnabled,
    isAllowExternalDevicePlaybackEnabled: Boolean = uiState.settings.allowExternalDevicePlayback,
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onLanguageDialogDismiss: () -> Unit,
    onGlassmorphicToggle: (Boolean) -> Unit,
    onAlbumArtToggle: (Boolean) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onPlaybackPitchChange: (Float) -> Unit,
    onCrossfadeDurationChange: (Int) -> Unit,
    onSkipSilenceToggle: (Boolean) -> Unit,
    onEqualizerPresetSelected: (EqualizerPreset) -> Unit,
    onReplayGainModeSelected: (ReplayGainMode) -> Unit,
    onVisualizerClick: () -> Unit,
    onVisualizerDialogDismiss: () -> Unit,
    onCircularVisualizerSelected: (CircularVisualizerType) -> Unit,
    onExcludedFoldersClick: () -> Unit,
    onExcludedFoldersDialogDismiss: () -> Unit,
    onBrowseFolderClick: () -> Unit,
    onRemoveExcludedFolder: (String) -> Unit,
    onMinTrackDurationChange: (Int) -> Unit,
    onRescanMediaClick: () -> Unit,
    onLockscreenControlsToggle: (Boolean) -> Unit,
    onScreenOffPlaybackToggle: (Boolean) -> Unit,
    onHeadsetControlsToggle: (Boolean) -> Unit,
    onAllowExternalDeviceToggle: (Boolean) -> Unit,
    onShakeToSkipToggle: (Boolean) -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.settings

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(R.string.settings_title),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = NeonBlue,
                        strokeWidth = 2.dp
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    // Section 1: Appearance & Language
                    SettingsCategorySection(
                        title = stringResource(R.string.appearance_and_language),
                        icon = Icons.Default.Palette
                    ) {
                        // App Language Selector
                        SettingsRowItem(
                            title = stringResource(R.string.app_language),
                            subtitle = settings.language.nativeName + " (" + settings.language.displayName + ")",
                            icon = Icons.Default.Language,
                            onClick = onLanguageClick
                        )

                        SettingsDivider()

                        // Glassmorphic Style Toggle
                        SettingSwitchItem(
                            title = stringResource(R.string.glassmorphic_style),
                            subtitle = stringResource(R.string.glassmorphic_style_desc),
                            icon = Icons.Default.Equalizer,
                            checked = isGlassmorphicEnabled,
                            onCheckedChange = onGlassmorphicToggle
                        )

                        SettingsDivider()

                        // Album Art Display Toggle
                        SettingsSwitchRowItem(
                            title = stringResource(R.string.album_art_display),
                            subtitle = stringResource(R.string.album_art_display_desc),
                            icon = Icons.Default.Album,
                            checked = isAlbumArtworkEnabled,
                            onCheckedChange = onAlbumArtToggle
                        )
                    }
                }

                item {
                    // Section 2: Audio & Playback Effects
                    SettingsCategorySection(
                        title = stringResource(R.string.audio_and_effects),
                        icon = Icons.Default.GraphicEq
                    ) {
                        // Playback Speed Slider
                        SettingsSliderRowItem(
                            title = stringResource(R.string.playback_speed),
                            valueText = String.format(Locale.US, "%.2fx", settings.playbackSpeed),
                            icon = Icons.Default.MusicNote,
                            value = settings.playbackSpeed,
                            valueRange = 0.5f..2.0f,
                            steps = 0,
                            onValueChange = onPlaybackSpeedChange
                        )

                        SettingsDivider()

                        // Playback Pitch Slider
                        SettingsSliderRowItem(
                            title = stringResource(R.string.playback_pitch),
                            valueText = String.format(Locale.US, "%.2fx", settings.playbackPitch),
                            icon = Icons.Default.AudioFile,
                            value = settings.playbackPitch,
                            valueRange = 0.5f..2.0f,
                            steps = 0,
                            onValueChange = onPlaybackPitchChange
                        )

                        SettingsDivider()

                        // Crossfade Slider (Crossfade Tracks)
                        SettingSliderItem(
                            title = stringResource(R.string.crossfade),
                            value = crossfadeDuration.toFloat(),
                            valueRange = 0f..12f,
                            steps = 12,
                            valueTextFormatter = { currentVal ->
                                String.format(Locale.US, "%d sec", currentVal.toInt())
                            },
                            icon = Icons.Default.Equalizer,
                            onValueChange = {},
                            onValueChangeFinished = { onCrossfadeDurationChange(it.toInt()) }
                        )

                        SettingsDivider()

                        // Skip Silence Toggle
                        SettingsSwitchRowItem(
                            title = stringResource(R.string.skip_silence),
                            subtitle = stringResource(R.string.skip_silence_desc),
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            checked = settings.skipSilenceEnabled,
                            onCheckedChange = onSkipSilenceToggle
                        )

                        SettingsDivider()

                        // Equalizer Preset Selector
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.default_eq_preset),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                EqualizerPreset.entries.forEach { preset ->
                                    val isSelected = preset == settings.defaultEqualizerPreset
                                    val presetLabel = stringResource(preset.getStringRes())
                                    val shape = RoundedCornerShape(20.dp)

                                    Box(
                                        modifier = Modifier
                                            .clip(shape)
                                            .background(
                                                if (isSelected) {
                                                    Brush.linearGradient(
                                                        listOf(
                                                            Color(0xFF0F0B21),
                                                            Color(0xFF2C1454)
                                                        )
                                                    )
                                                } else {
                                                    Brush.linearGradient(
                                                        listOf(
                                                            Color.White.copy(alpha = 0.08f),
                                                            Color.White.copy(alpha = 0.03f)
                                                        )
                                                    )
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.15f),
                                                shape = shape
                                            )
                                            .clickable { onEqualizerPresetSelected(preset) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = presetLabel,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.8f)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        SettingsDivider()

                        // Visualizer Selector Row
                        SettingsRowItem(
                            title = stringResource(R.string.visualizer_style),
                            subtitle = VisualizerStyle.fromId(settings.selectedVisualizer.ifEmpty { settings.circularVisualizerType }).title,
                            icon = Icons.Default.GraphicEq,
                            onClick = onVisualizerClick
                        )

                        SettingsDivider()

                        // ReplayGain Normalization Selector
                        val replayGainOff = stringResource(R.string.replay_gain_off)
                        val replayGainTrack = stringResource(R.string.replay_gain_track)
                        val replayGainAlbum = stringResource(R.string.replay_gain_album)

                        SettingsChipSelectorRow(
                            title = stringResource(R.string.replay_gain),
                            options = listOf(replayGainOff, replayGainTrack, replayGainAlbum),
                            selectedOption = when (replayGainMode) {
                                ReplayGainMode.OFF -> replayGainOff
                                ReplayGainMode.TRACK -> replayGainTrack
                                ReplayGainMode.ALBUM -> replayGainAlbum
                            },
                            onOptionSelected = { label ->
                                val mode = when (label) {
                                    replayGainTrack -> ReplayGainMode.TRACK
                                    replayGainAlbum -> ReplayGainMode.ALBUM
                                    else -> ReplayGainMode.OFF
                                }
                                onReplayGainModeSelected(mode)
                            }
                        )
                    }
                }

                item {
                    // Section 3: Library & Storage Management
                    SettingsCategorySection(
                        title = stringResource(R.string.library_and_storage),
                        icon = Icons.Default.FolderOff
                    ) {
                        // Excluded Folders Manager
                        SettingsRowItem(
                            title = stringResource(R.string.excluded_folders),
                            subtitle = if (settings.excludedFolders.isEmpty()) stringResource(R.string.no_excluded_folders)
                            else "${settings.excludedFolders.size} folder(s) excluded",
                            icon = Icons.Default.FolderOff,
                            onClick = onExcludedFoldersClick
                        )

                        SettingsDivider()

                        // Minimum Track Duration Slider
                        SettingSliderItem(
                            title = stringResource(R.string.min_track_duration),
                            value = settings.minTrackDurationSeconds.toFloat(),
                            valueRange = 0f..120f,
                            steps = 24,
                            valueTextFormatter = { currentVal ->
                                String.format(Locale.US, "%d sec", currentVal.toInt())
                            },
                            icon = Icons.Default.AudioFile,
                            onValueChange = {},
                            onValueChangeFinished = { onMinTrackDurationChange(it.toInt()) }
                        )

                        SettingsDivider()

                        // Rescan Media Button
                        SettingsActionRowItem(
                            title = stringResource(R.string.rescan_media),
                            subtitle = stringResource(R.string.rescan_media_desc),
                            icon = Icons.Default.Refresh,
                            actionText = stringResource(R.string.rescan_media),
                            onClick = onRescanMediaClick
                        )
                    }
                }

                item {
                    // Section 4: Controls & Background Services
                    SettingsCategorySection(
                        title = stringResource(R.string.controls_and_services),
                        icon = Icons.Default.Phonelink
                    ) {
                        // Lockscreen Controls Toggle
                        SettingSwitchItem(
                            title = stringResource(R.string.lockscreen_controls),
                            subtitle = stringResource(R.string.lockscreen_controls_desc),
                            icon = Icons.Default.ScreenLockPortrait,
                            checked = isLockscreenControlsEnabled,
                            onCheckedChange = onLockscreenControlsToggle
                        )

                        SettingsDivider()

                        // Screen Off Playback Toggle
                        SettingSwitchItem(
                            title = stringResource(R.string.screen_off_playback),
                            subtitle = stringResource(R.string.screen_off_playback_desc),
                            icon = Icons.Default.LockClock,
                            checked = isScreenOffPlaybackEnabled,
                            onCheckedChange = onScreenOffPlaybackToggle
                        )

                        SettingsDivider()

                        // Headset Controls Toggle
                        SettingSwitchItem(
                            title = stringResource(R.string.headset_controls),
                            subtitle = stringResource(R.string.headset_controls_desc),
                            icon = Icons.Default.Headset,
                            checked = isHeadsetControlsEnabled,
                            onCheckedChange = onHeadsetControlsToggle
                        )

                        SettingsDivider()

                        // External Device Playback Toggle
                        SettingSwitchItem(
                            title = stringResource(R.string.allow_external_device),
                            subtitle = stringResource(R.string.allow_external_device_desc),
                            icon = Icons.Default.Phonelink,
                            checked = isAllowExternalDevicePlaybackEnabled,
                            onCheckedChange = onAllowExternalDeviceToggle
                        )

                        SettingsDivider()

                        // Shake to Skip Toggle
                        SettingsSwitchRowItem(
                            title = stringResource(R.string.shake_to_skip),
                            subtitle = stringResource(R.string.shake_to_skip_desc),
                            icon = Icons.Default.Vibration,
                            checked = settings.shakeToSkipEnabled,
                            onCheckedChange = onShakeToSkipToggle
                        )
                    }
                }

                item {
                    // Section 5: System & Permissions
                    SettingsCategorySection(
                        title = stringResource(R.string.system_and_permissions),
                        icon = Icons.Default.Notifications
                    ) {
                        SettingsRowItem(
                            title = stringResource(R.string.notification_permissions_status),
                            subtitle = stringResource(R.string.notification_permissions_status_desc),
                            icon = Icons.Default.Notifications,
                            onClick = onNavigateToPermissions
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Language Selection Dialog
    if (uiState.showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = settings.language,
            onLanguageSelected = onLanguageSelected,
            onDismiss = onLanguageDialogDismiss
        )
    }

    // Excluded Folders Dialog
    if (uiState.showExcludedFoldersDialog) {
        ExcludedFoldersDialog(
            excludedFolders = settings.excludedFolders,
            onBrowseFolderClick = onBrowseFolderClick,
            onRemoveFolder = onRemoveExcludedFolder,
            onDismiss = onExcludedFoldersDialogDismiss
        )
    }

    // Visualizer Selector Dialog
    if (uiState.showVisualizerDialog) {
        VisualizerSelectorDialog(
            onDismiss = onVisualizerDialogDismiss,
            onSelectType = onCircularVisualizerSelected
        )
    }
}

@Composable
fun SettingsCategorySection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = NeonBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (icon == Icons.Default.Album || icon == Icons.Default.FolderOff || icon == Icons.Default.AudioFile) NeonBlue else Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun SettingsSwitchRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (icon == Icons.Default.Album || icon == Icons.Default.FolderOff || icon == Icons.Default.AudioFile) NeonBlue else Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonBlue,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun SettingsSliderRowItem(
    title: String,
    valueText: String,
    icon: ImageVector,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueText,
                color = NeonBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = NeonBlue,
                activeTrackColor = NeonBlue,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsChipSelectorRow(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option.equals(selectedOption, ignoreCase = true)
                val shape = RoundedCornerShape(20.dp)

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0F0B21),
                                        Color(0xFF2C1454)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            }
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.15f),
                            shape = shape
                        )
                        .clickable { onOptionSelected(option) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsActionRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F0B21),
                            Color(0xFF2C1454)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = NeonBlue,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionText,
                color = NeonBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.08f))
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1238),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = NeonBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.select_language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    val isSelected = language == currentLanguage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { onLanguageSelected(language) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onLanguageSelected(language) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = language.nativeName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = language.displayName,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun ExcludedFoldersDialog(
    excludedFolders: Set<String>,
    onBrowseFolderClick: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1238),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderOff, contentDescription = null, tint = NeonBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.excluded_folders),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onBrowseFolderClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = Color(0xFF0F0B21)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.browse_and_add_folder),
                        color = Color(0xFF0F0B21),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (excludedFolders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_excluded_folders),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        excludedFolders.forEach { folderPath ->
                            val displayName = remember(folderPath) {
                                FolderFilterHelper.formatFolderPath(folderPath)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = displayName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (folderPath != displayName) {
                                        Text(
                                            text = folderPath,
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(onClick = { onRemoveFolder(folderPath) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun VisualizerSelectorDialog(
    onDismiss: () -> Unit,
    onSelectType: (CircularVisualizerType) -> Unit
) {
    val audioVisualizerManager = remember { AudioVisualizerManager() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1238),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.visualizer_style),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                VisualizerSelectorScreen(
                    audioVisualizerManager = audioVisualizerManager,
                    isPlaying = true,
                    onSelectType = { type ->
                        onSelectType(type)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    color = NeonBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
