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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sftwr.cleandevelopment.musicplayer.designsystem.R

val NeonBlue = Color(0xFF91D8FB)
val DrawerDarkGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF3F1D78),
        Color(0xFF2C1454),
        Color(0xFF1E0C3A)
    )
)

/**
 * Custom shape for the main content card when drawer is open in LTR mode,
 * keeping the top-left corner and top bar 100% unclipped and rectangular,
 * with a very subtle slant on the left edge so playlist items are fully visible.
 */
val SlantedLeftEdgeShape = GenericShape { size, _ ->
    val topBarHeight = size.height * 0.12f
    val slantOffset = size.width * 0.025f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)
    lineTo(slantOffset, size.height)
    lineTo(0f, topBarHeight)
    lineTo(0f, 0f)
    close()
}

/**
 * Custom shape for the main content card when drawer is open in RTL mode,
 * keeping the top-right corner and top bar 100% unclipped and rectangular,
 * with a very subtle slant on the right edge.
 */
val SlantedRightEdgeShape = GenericShape { size, _ ->
    val topBarHeight = size.height * 0.12f
    val slantOffset = size.width * 0.025f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, topBarHeight)
    lineTo(size.width - slantOffset, size.height)
    lineTo(0f, size.height)
    lineTo(0f, 0f)
    close()
}

data class DrawerMenuItem(
    val id: String,
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
    onPermissionsClick: () -> Unit = {},
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onRateUsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val menuItems = listOf(
        DrawerMenuItem("Equalizer", stringResource(R.string.equalizer), Icons.Default.GraphicEq, onClick = onEqualizerClick),
        DrawerMenuItem("Sleep Timer", stringResource(R.string.sleep_timer), Icons.Default.Timer, onClick = onSleepTimerClick),
        DrawerMenuItem("Scan Media", stringResource(R.string.scan_media), Icons.Default.FolderOpen, onClick = onScanMediaClick),
        DrawerMenuItem("Notification / Permissions", stringResource(R.string.notification_permissions), Icons.Default.Notifications, onClick = onPermissionsClick),
        DrawerMenuItem("Settings", stringResource(R.string.settings_title), Icons.Default.Settings, onClick = onSettingsClick),
        DrawerMenuItem("About Us", stringResource(R.string.about_us), Icons.Default.Info, onClick = onAboutClick),
        DrawerMenuItem("Rate Us", stringResource(R.string.rate_us), Icons.Default.Star, onClick = onRateUsClick)
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(DrawerDarkGradient)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with App Icon & Localized App Name
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
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = stringResource(R.string.drawer_title),
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.drawer_title),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.drawer_subtitle),
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
                        val isSelected = item.id == selectedItem || item.title == selectedItem
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
                                    onItemSelected(item.id)
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
                text = stringResource(R.string.version, "3.0.4"),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
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
                Text(stringResource(R.string.settings_title), color = Color.White, fontWeight = FontWeight.Bold)
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
                Text(stringResource(R.string.version, "1.0.0"), color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok), color = NeonBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
}
