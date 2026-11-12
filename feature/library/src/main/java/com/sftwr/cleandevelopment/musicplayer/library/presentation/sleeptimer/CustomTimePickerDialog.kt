package com.sftwr.cleandevelopment.musicplayer.library.presentation.sleeptimer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sftwr.cleandevelopment.musicplayer.home.R

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
                        text = stringResource(R.string.set_custom_time),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.hours),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            IconButton(onClick = { if (hours < 23) hours++ else hours = 0 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = CustomTimeNeonBlue)
                            }
                            Text(
                                text = "%02d".format(hours),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { if (hours > 0) hours-- else hours = 23 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CustomTimeNeonBlue)
                            }
                        }

                        Text(
                            text = ":",
                            color = CustomTimeNeonBlue,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.minutes),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            IconButton(onClick = { if (minutes < 59) minutes++ else minutes = 0 }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = CustomTimeNeonBlue)
                            }
                            Text(
                                text = "%02d".format(minutes),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { if (minutes > 0) minutes-- else minutes = 59 }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = CustomTimeNeonBlue)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel), color = Color.White.copy(alpha = 0.7f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onConfirm(hours, minutes)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CustomTimeNeonBlue)
                        ) {
                            Text(stringResource(R.string.set_timer), color = Color(0xFF0F0B21), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
