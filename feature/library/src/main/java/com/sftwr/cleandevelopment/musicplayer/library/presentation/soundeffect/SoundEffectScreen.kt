package com.sftwr.cleandevelopment.musicplayer.library.presentation.soundeffect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.platform.LocalContext
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.ad.showRewardedAdToUnlockFeature
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalColorPalette
import com.sftwr.cleandevelopment.musicplayer.domain.model.SoundEffect
import com.sftwr.cleandevelopment.musicplayer.domain.model.isEffectUnlocked
import com.sftwr.cleandevelopment.musicplayer.home.R

fun SoundEffect.getNameRes(): Int {
    return when (this) {
        SoundEffect.ELECTRONIC_TUBE -> R.string.effect_electronic_tube
        SoundEffect.THREE_D_ROTATE -> R.string.effect_three_d_rotate
        SoundEffect.TONE_LOW -> R.string.effect_tone_low
        SoundEffect.SURROUND_SOUND -> R.string.effect_surround_sound
        SoundEffect.MAGIC_SOUND -> R.string.effect_magic_sound
        SoundEffect.LIVE_TREBLE -> R.string.effect_live_treble
    }
}

fun SoundEffect.getDescRes(): Int {
    return when (this) {
        SoundEffect.ELECTRONIC_TUBE -> R.string.effect_electronic_tube_desc
        SoundEffect.THREE_D_ROTATE -> R.string.effect_three_d_rotate_desc
        SoundEffect.TONE_LOW -> R.string.effect_tone_low_desc
        SoundEffect.SURROUND_SOUND -> R.string.effect_surround_sound_desc
        SoundEffect.MAGIC_SOUND -> R.string.effect_magic_sound_desc
        SoundEffect.LIVE_TREBLE -> R.string.effect_live_treble_desc
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundEffectScreen(
    viewModel: SoundEffectViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val palette = LocalColorPalette.current
    val accent = palette.accent

    Scaffold(
        containerColor = palette.primaryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sound_effects),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = palette.primaryBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Master Toggle & Volume/Boost Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(accent.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = accent
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.audio_effects_engine),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (uiState.isEnabled) stringResource(R.string.active_and_processing) else stringResource(R.string.disabled),
                                    color = if (uiState.isEnabled) accent else Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isEnabled,
                            onCheckedChange = { viewModel.toggleEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accent,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (uiState.isEnabled) 1.0f else 0.4f)
                    ) {
                        var sliderPosition by remember(uiState.volumeOrBoost) { mutableStateOf(uiState.volumeOrBoost) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.enhancement_intensity),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${(sliderPosition.coerceIn(0f, 1f) * 100).toInt()}%",
                                color = accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                sliderPosition = it
                                viewModel.setVolumeOrBoost(it)
                            },
                            onValueChangeFinished = { viewModel.setVolumeOrBoost(sliderPosition) },
                            enabled = uiState.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = accent,
                                activeTrackColor = accent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.available_sound_effects),
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val context = LocalContext.current
            val isAdFree by viewModel.adManager.isAdFree.collectAsState()

            // Effects List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .alpha(if (uiState.isEnabled) 1.0f else 0.4f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.availableEffects) { effect ->
                    val isSelected = effect == uiState.currentEffect
                    val isUnlocked = uiState.isEffectUnlocked(effect, isAdFree)
                    SoundEffectItem(
                        effect = effect,
                        isSelected = isSelected,
                        isUnlocked = isUnlocked,
                        accentColor = accent,
                        onUseClick = {
                            if (!uiState.isEnabled) {
                                viewModel.toggleEnabled(true)
                            }
                            if (isUnlocked) {
                                viewModel.selectEffect(effect)
                            } else {
                                val wasPlaying = viewModel.pausePlaybackIfPlaying()
                                showRewardedAdToUnlockFeature(
                                    context = context,
                                    adManager = viewModel.adManager,
                                    featureName = "Sound Effect: ${effect.displayName}",
                                    onRewardUnlocked = {
                                        viewModel.toggleEnabled(true)
                                        viewModel.unlockEffect(effect)
                                        viewModel.selectEffect(effect)
                                        viewModel.resumePlaybackIfWasPlaying(wasPlaying)
                                    },
                                    onAdDismissOrError = {
                                        viewModel.resumePlaybackIfWasPlaying(wasPlaying)
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundEffectItem(
    effect: SoundEffect,
    isSelected: Boolean,
    isUnlocked: Boolean,
    accentColor: Color,
    onUseClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = if (isSelected) {
                                Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.6f)))
                            } else {
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f)))
                            },
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (!isUnlocked) Icons.Default.Lock else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else if (!isUnlocked) Color(0xFFFFD54F) else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = stringResource(effect.getNameRes()),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(effect.getDescRes()),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onUseClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) accentColor else if (!isUnlocked) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.15f),
                    contentColor = if (isSelected || !isUnlocked) Color.Black else Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (isSelected) stringResource(R.string.active) else if (!isUnlocked) "Unlock" else stringResource(R.string.use),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
