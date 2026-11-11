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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard

val CustomTimeNeonBlue = Color(0xFF91D8FB)

@Composable
fun CustomTimePickerDialog(
    initialHours: Int = 0,
    initialMinutes: Int = 30,
    onConfirm: (hours: Int, minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var hours by remember { mutableIntStateOf(initialHours.coerceIn(0, 23)) }
    var minutes by remember { mutableIntStateOf(initialMinutes.coerceIn(0, 59)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1B1238),
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2C1454),
                                Color(0xFF0F0B21)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Set Custom Time",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Hours and Minutes selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour Selector
                        TimeColumn(
                            label = "Hours",
                            value = hours,
                            onIncrement = { hours = (hours + 1) % 24 },
                            onDecrement = { hours = if (hours > 0) hours - 1 else 23 }
                        )

                        Text(
                            text = ":",
                            color = CustomTimeNeonBlue,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Minute Selector
                        TimeColumn(
                            label = "Minutes",
                            value = minutes,
                            onIncrement = { minutes = (minutes + 1) % 60 },
                            onDecrement = { minutes = if (minutes > 0) minutes - 1 else 59 }
                        )
                    }

                    // Quick adjustment chips (+15m, +30m, +1h)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAddChip(text = "+15m") {
                            val totalMins = hours * 60 + minutes + 15
                            hours = (totalMins / 60) % 24
                            minutes = totalMins % 60
                        }
                        QuickAddChip(text = "+30m") {
                            val totalMins = hours * 60 + minutes + 30
                            hours = (totalMins / 60) % 24
                            minutes = totalMins % 60
                        }
                        QuickAddChip(text = "+1h") {
                            hours = (hours + 1) % 24
                        }
                    }

                    // Total duration summary
                    val totalMinutes = hours * 60 + minutes
                    Text(
                        text = if (totalMinutes == 0) "Timer will be turned off" else "Total duration: ${if (hours > 0) "${hours}h " else ""}${minutes}m",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "Cancel",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onConfirm(hours, minutes) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomTimeNeonBlue,
                                contentColor = Color(0xFF0F0B21)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Set Timer",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeColumn(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase $label",
                tint = CustomTimeNeonBlue
            )
        }

        GlassCard(
            modifier = Modifier
                .width(80.dp)
                .height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", value),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease $label",
                tint = CustomTimeNeonBlue
            )
        }

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickAddChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = CustomTimeNeonBlue,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
