package com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard

val SleepTimerNeonBlue = Color(0xFF91D8FB)

@Composable
fun SleepTimerDialog(
    viewModel: SleepTimerViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showCustomTimePicker) {
        CustomTimePickerDialog(
            initialHours = uiState.customHours,
            initialMinutes = uiState.customMinutes,
            onConfirm = { hours, minutes ->
                viewModel.setCustomTime(hours, minutes)
            },
            onDismiss = { viewModel.dismissCustomTimePicker() }
        )
    }

    val options = remember {
        listOf(
            TimerPreset.OFF,
            TimerPreset.MIN_30,
            TimerPreset.HOUR_1,
            TimerPreset.HOUR_1_30,
            TimerPreset.HOUR_2,
            TimerPreset.CUSTOM
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1238),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = SleepTimerNeonBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Sleep Timer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active countdown banner
                if (uiState.isActive && uiState.remainingTimeFormatted.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        SleepTimerNeonBlue.copy(alpha = 0.2f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(1.dp, SleepTimerNeonBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Timer Active",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Stops playback in ${uiState.remainingTimeFormatted}",
                                    color = SleepTimerNeonBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            IconButton(
                                onClick = { viewModel.cancelTimer() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    text = "Off",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Select a timer duration to automatically stop music playback:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Presets list
                options.forEach { preset ->
                    val isSelected = uiState.selectedPreset == preset && (preset != TimerPreset.OFF || !uiState.isActive)
                    val label = when (preset) {
                        TimerPreset.CUSTOM -> {
                            if (uiState.selectedPreset == TimerPreset.CUSTOM && uiState.durationMinutes > 0) {
                                val h = uiState.durationMinutes / 60
                                val m = uiState.durationMinutes % 60
                                "Custom (${if (h > 0) "${h}h " else ""}${m}m)"
                            } else {
                                "Custom..."
                            }
                        }
                        else -> preset.label
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.selectPreset(preset)
                            },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = SleepTimerNeonBlue,
                                        unselectedColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                                Text(
                                    text = label,
                                    color = if (isSelected) SleepTimerNeonBlue else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            }

                            if (preset == TimerPreset.CUSTOM && isSelected) {
                                IconButton(
                                    onClick = { viewModel.showCustomTimePicker() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Custom Time",
                                        tint = SleepTimerNeonBlue
                                    )
                                }
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SleepTimerNeonBlue
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = SleepTimerNeonBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}
