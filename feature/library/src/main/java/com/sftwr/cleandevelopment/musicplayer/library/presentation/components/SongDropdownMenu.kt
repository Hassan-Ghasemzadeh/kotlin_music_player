package com.sftwr.cleandevelopment.musicplayer.library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onFavoriteClick: () -> Unit,
    onInfoClick: () -> Unit,
    onTagEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val menuItems = listOf(
        SongMenuItem("پخش بعدی", Icons.Default.PlayArrow, onPlayNext),
        SongMenuItem("افزودن به لیست پخش", Icons.AutoMirrored.Filled.PlaylistAdd, onAddToPlaylist),
        SongMenuItem("افزودن به علاقه‌مندی‌ها", Icons.Default.FavoriteBorder, onFavoriteClick),
        SongMenuItem("مشاهده اطلاعات آهنگ", Icons.Default.Info, onInfoClick),
        SongMenuItem("ویرایش تگ", Icons.Default.Edit, onTagEditClick),
        SongMenuItem("حذف از دستگاه", Icons.Default.Delete, onDeleteClick)
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = MaterialTheme.colorScheme.primary
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