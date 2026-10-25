package com.sftwr.cleandevelopment.musicplayer.library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Model representing a single menu item
data class SongMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SongDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onFavoriteClick: () -> Unit,
    onInfoClick: () -> Unit,
    onTagEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val menuItems = listOf(
        SongMenuItem("افزودن به لیست پخش", Icons.AutoMirrored.Filled.PlaylistAdd, onAddToPlaylist),
        SongMenuItem("افزودن به علاقه‌مندی‌ها", Icons.Default.FavoriteBorder, onFavoriteClick),
        SongMenuItem("مشاهده اطلاعات آهنگ", Icons.Default.Info, onInfoClick),
        SongMenuItem("ویرایش تگ", Icons.Default.Edit, onTagEditClick),
        SongMenuItem("حذف از دستگاه", Icons.Default.Delete, onDeleteClick)
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = Color.Transparent,
        modifier = Modifier
            .background(
                color = Color(0xFF0F0B21),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = Color.White
                    )
                },
                onClick = {
                    item.onClick()
                    onDismissRequest()
                }
            )
        }
    }
}