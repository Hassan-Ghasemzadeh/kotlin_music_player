package com.sftwr.cleandevelopment.musicplayer.library.presentation.equalizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.ui.platform.LocalContext
import com.sftwr.cleandevelopment.musicplayer.domain.ad.AdManager
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.ad.showRewardedAdToUnlockFeature
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalColorPalette
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerBand
import com.sftwr.cleandevelopment.musicplayer.domain.model.EqualizerPreset
import com.sftwr.cleandevelopment.musicplayer.home.R

/**
 * Custom Bottom Sheet Dialog providing a dark glassmorphic UI for tuning audio equalizer bands,
 * sound presets, Bass Boost, and Virtualizer effects.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    viewModel: EqualizerViewModel = hiltViewModel(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onDismissRequest: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val palette = LocalColorPalette.current
    val accent = palette.accent

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = palette.primaryBackground.copy(alpha = 0.96f),
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            EqualizerHeader(
                isEnabled = uiState.isEnabled,
                accentColor = accent,
                secondaryColor = palette.surfaceCard,
                onToggleEnabled = { viewModel.toggleEnabled(it) },
                onClose = onDismissRequest
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (uiState.isEnabled) 1.0f else 0.45f)
            ) {
                Column {
                    val context = LocalContext.current
                    val isAdFree by viewModel.adManager.isAdFree.collectAsState()
                    var transientUnlockedPreset by remember { mutableStateOf<EqualizerPreset?>(null) }

                    PresetsSection(
                        availablePresets = uiState.availablePresets,
                        currentPresetId = uiState.currentPresetId,
                        isAdFree = isAdFree,
                        transientUnlockedPreset = transientUnlockedPreset,
                        isEnabled = uiState.isEnabled,
                        accentColor = accent,
                        secondaryColor = palette.surfaceCard,
                        onPresetSelected = { preset ->
                            val isFree = preset == EqualizerPreset.NORMAL || preset == EqualizerPreset.FLAT || preset == EqualizerPreset.OFF
                            val isUnlocked = isAdFree || isFree || transientUnlockedPreset == preset

                            if (preset != transientUnlockedPreset) {
                                transientUnlockedPreset = null
                            }

                            if (isUnlocked) {
                                viewModel.selectPreset(preset)
                            } else {
                                val wasPlaying = viewModel.pausePlaybackIfPlaying()
                                showRewardedAdToUnlockFeature(
                                    context = context,
                                    adManager = viewModel.adManager,
                                    featureName = "Equalizer Preset: ${preset.displayName}",
                                    onRewardUnlocked = {
                                        transientUnlockedPreset = preset
                                        viewModel.selectPreset(preset)
                                        viewModel.resumePlaybackIfWasPlaying(wasPlaying)
                                    },
                                    onAdDismissOrError = {
                                        viewModel.resumePlaybackIfWasPlaying(wasPlaying)
                                    }
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    FrequencyBandsSection(
                        bands = uiState.bands,
                        isEnabled = uiState.isEnabled,
                        accentColor = accent,
                        onBandLevelChanged = { index, levelMb ->
                            viewModel.setBandLevel(index, levelMb)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AudioEffectsSection(
                        bassBoost = uiState.bassBoostStrength,
                        virtualizer = uiState.virtualizerStrength,
                        isEnabled = uiState.isEnabled,
                        accentColor = accent,
                        onBassBoostChanged = { viewModel.setBassBoostStrength(it) },
                        onVirtualizerChanged = { viewModel.setVirtualizerStrength(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EqualizerHeader(
    isEnabled: Boolean,
    accentColor: Color,
    secondaryColor: Color,
    onToggleEnabled: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accentColor.copy(alpha = 0.3f),
                                secondaryColor.copy(alpha = 0.5f)
                            )
                        )
                    )
                    .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.equalizer_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PresetsSection(
    availablePresets: List<EqualizerPreset>,
    currentPresetId: Short,
    isAdFree: Boolean,
    transientUnlockedPreset: EqualizerPreset?,
    isEnabled: Boolean,
    accentColor: Color,
    secondaryColor: Color,
    onPresetSelected: (EqualizerPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.presets),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = availablePresets,
                key = { it.id }
            ) { preset ->
                val isSelected = currentPresetId == preset.id
                val isFree = preset == EqualizerPreset.NORMAL || preset == EqualizerPreset.FLAT || preset == EqualizerPreset.OFF
                val isUnlocked = isAdFree || isFree || transientUnlockedPreset == preset
                val shape = RoundedCornerShape(20.dp)

                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    listOf(
                                        accentColor.copy(alpha = 0.35f),
                                        secondaryColor.copy(alpha = 0.45f)
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
                            color = if (isSelected) accentColor else Color.White.copy(alpha = 0.15f),
                            shape = shape
                        )
                        .clickable(enabled = isEnabled) {
                            onPresetSelected(preset)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FrequencyBandsSection(
    bands: List<EqualizerBand>,
    isEnabled: Boolean,
    accentColor: Color,
    onBandLevelChanged: (bandIndex: Short, levelMb: Short) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.frequency_bands),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bands.forEach { band ->
                    EqualizerBandItem(
                        band = band,
                        isEnabled = isEnabled,
                        accentColor = accentColor,
                        onLevelChanged = { levelMb ->
                            onBandLevelChanged(band.index, levelMb)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqualizerBandItem(
    band: EqualizerBand,
    isEnabled: Boolean,
    accentColor: Color,
    onLevelChanged: (Short) -> Unit
) {
    val levelDb = band.currentLevelMb / 100f
    val formattedDb = if (levelDb > 0) "+%.1f dB".format(levelDb) else "%.1f dB".format(levelDb)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(54.dp)
    ) {
        Text(
            text = formattedDb,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (band.currentLevelMb != 0.toShort()) accentColor else Color.White.copy(alpha = 0.6f)
            ),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = band.currentLevelMb.toFloat(),
            onValueChange = { newValue ->
                onLevelChanged(newValue.toInt().toShort())
            },
            valueRange = band.minLevelMb.toFloat()..band.maxLevelMb.toFloat(),
            enabled = isEnabled,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(accentColor, CircleShape)
                        .border(1.5.dp, Color.Black, CircleShape)
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                disabledActiveTrackColor = accentColor.copy(alpha = 0.3f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier
                .height(140.dp)
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = band.label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AudioEffectsSection(
    bassBoost: Short,
    virtualizer: Short,
    isEnabled: Boolean,
    accentColor: Color,
    onBassBoostChanged: (Short) -> Unit,
    onVirtualizerChanged: (Short) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.audio_effects),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                EffectSliderRow(
                    title = stringResource(R.string.bass_boost),
                    subtitle = "Enhance low audio frequencies",
                    icon = Icons.Default.MusicNote,
                    value = bassBoost,
                    maxValue = 1000,
                    isEnabled = isEnabled,
                    accentColor = accentColor,
                    onValueChanged = onBassBoostChanged
                )

                Spacer(modifier = Modifier.height(16.dp))

                EffectSliderRow(
                    title = stringResource(R.string.virtualizer),
                    subtitle = "Create spatial audio width effect",
                    icon = Icons.Default.SurroundSound,
                    value = virtualizer,
                    maxValue = 1000,
                    isEnabled = isEnabled,
                    accentColor = accentColor,
                    onValueChanged = onVirtualizerChanged
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EffectSliderRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Short,
    maxValue: Short,
    isEnabled: Boolean,
    accentColor: Color,
    onValueChanged: (Short) -> Unit
) {
    val percentage = ((value.toFloat() / maxValue.toFloat()) * 100).toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChanged(it.toInt().toShort()) },
            valueRange = 0f..maxValue.toFloat(),
            enabled = isEnabled,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(accentColor, CircleShape)
                        .border(1.5.dp, Color.Black, CircleShape)
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = accentColor,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                disabledActiveTrackColor = accentColor.copy(alpha = 0.3f),
                disabledInactiveTrackColor = Color.White.copy(alpha = 0.08f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}