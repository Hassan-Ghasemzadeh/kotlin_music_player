package com.sftwr.cleandevelopment.musicplayer.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val NeonBlue = Color(0xFF91D8FB)
val DrawerDarkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF3F1D78),
        Color(0xFF2C1454),
        Color(0xFF1E0C3A)
    )
)

/**
 * Custom shape for the main content card when drawer is open,
 * keeping the top-left corner and top bar 100% unclipped and rectangular,
 * with a very subtle slant on the left edge so playlist items are fully visible.
 */
val SlantedLeftEdgeShape = GenericShape { size, _ ->
    val topBarHeight = size.height * 0.12f
    val slantOffset = size.width * 0.025f // Very subtle slant offset so playlist items on the left are fully visible
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(slantOffset, size.height)
    lineTo(0f, topBarHeight)
    lineTo(0f, 0f)
    close()
}

data class DrawerMenuItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SlantedNavigationDrawerContent(
    selectedItem: String = "Equalizer",
    onItemSelected: (String) -> Unit,
    onEqualizerClick: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onScanMediaClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onRateUsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val menuItems = listOf(
        DrawerMenuItem("Equalizer", Icons.Default.GraphicEq, onClick = onEqualizerClick),
        DrawerMenuItem("Sleep Timer", Icons.Default.Timer, onClick = onSleepTimerClick),
        DrawerMenuItem("Scan Media", Icons.Default.FolderOpen, onClick = onScanMediaClick),
        DrawerMenuItem("Notification", Icons.Default.Notifications, onClick = {}),
        DrawerMenuItem("Settings", Icons.Default.Settings, onClick = onSettingsClick),
        DrawerMenuItem("About Us", Icons.Default.Info, onClick = onAboutClick),
        DrawerMenuItem("Rate Us", Icons.Default.Star, onClick = onRateUsClick)
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(DrawerDarkGradient)
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with App Icon drawable (app_icon) & App Name ("Music Player")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        NeonBlue.copy(alpha = 0.4f),
                                        Color(0xFF2C1454)
                                    )
                                )
                            )
                            .border(1.5.dp, NeonBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.sftwr.cleandevelopment.musicplayer.designsystem.R.drawable.app_icon),
                            contentDescription = "App Icon",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column {
                        Text(
                            text = "Music Player",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Ultimate Experience",
                            color = NeonBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Menu Items List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    menuItems.forEach { item ->
                        val isSelected = item.title == selectedItem
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.18f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    onItemSelected(item.title)
                                    item.onClick()
                                    onCloseDrawer()
                                }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) NeonBlue else Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Footer version
            Text(
                text = "Version 1.0.0 • Clean Architecture",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun SleepTimerDialog(
    currentTimerMinutes: Int,
    onSelectMinutes: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(0, 15, 30, 45, 60, 90)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1238),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = NeonBlue)
                Text("Sleep Timer", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (currentTimerMinutes == 0) "Timer is Off" else "Stops playback in $currentTimerMinutes minutes",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                options.forEach { mins ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectMinutes(mins) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (mins == 0) "Off" else "$mins Minutes",
                                color = if (currentTimerMinutes == mins) NeonBlue else Color.White,
                                fontWeight = if (currentTimerMinutes == mins) FontWeight.Bold else FontWeight.Normal
                            )
                            if (currentTimerMinutes == mins) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonBlue)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1238),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = NeonBlue)
                Text("Settings", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Audio Engine: Native AudioFX & ExoPlayer", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Text("Theme: Dark Glassmorphic (Neon Blue #91D8FB)", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Text("Storage: Local MediaStore & Room Database", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Text("Version: 1.0.0 (Production)", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = NeonBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1238),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = NeonBlue)
                Text("About Music Player", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Music Player is an advanced, high-performance offline music player built with Jetpack Compose, Material 3, Clean Architecture, and ExoPlayer.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Features a custom Slanted Navigation Drawer, glassmorphic UI cards, equalizer tuning, bass boost, favorite management, and seamless media scanning.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NeonBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}
