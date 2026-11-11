package com.sftwr.cleandevelopment.musicplayer.library.presentation.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

val ScanNeonBlue = Color(0xFF91D8FB)

/**
 * Modern glassmorphic Dialog for performing manual media storage scanning with real-time feedback.
 */
@Composable
fun ScanMediaDialog(
    viewModel: ScanMediaViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    AlertDialog(
        onDismissRequest = { viewModel.dismiss(onDismiss) },
        containerColor = Color(0xFF1B1238),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    ScanNeonBlue.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(1.dp, ScanNeonBlue.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isCompleted) Icons.Default.CheckCircle else Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = ScanNeonBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (uiState.isCompleted) "Scan Finished" else "Scanning Media",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (uiState.isScanning) {
                    // Scanning state: Real-time progress indicators
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        Color.White.copy(alpha = 0.02f)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ScanNeonBlue,
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "Scanning device storage...",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ScanNeonBlue,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )

                        Text(
                            text = if (uiState.currentPathBeingScanned.isNotEmpty()) {
                                uiState.currentPathBeingScanned
                            } else {
                                "Reading audio files..."
                            },
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Found ${uiState.scannedCount} tracks so far",
                            color = ScanNeonBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (uiState.isCompleted) {
                    // Completion State: Clean success card
                    AnimatedVisibility(
                        visible = uiState.isCompleted,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            ScanNeonBlue.copy(alpha = 0.15f),
                                            Color.White.copy(alpha = 0.03f)
                                        )
                                    )
                                )
                                .border(1.dp, ScanNeonBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Media scan completed successfully!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Found ${uiState.scannedCount} audio tracks on your device storage.",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Preparing media scan...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            if (uiState.isCompleted) {
                TextButton(onClick = { viewModel.onOkClick(onDismiss) }) {
                    Text("OK", color = ScanNeonBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                TextButton(onClick = { viewModel.dismiss(onDismiss) }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
        }
    )
}
