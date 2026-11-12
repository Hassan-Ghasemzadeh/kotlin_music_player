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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sftwr.cleandevelopment.musicplayer.home.R

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
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    isFavorite: Boolean = false
) {
    val favoriteTitle = if (isFavorite) stringResource(R.string.remove_from_favorites) else stringResource(R.string.add_to_favorites)
    val favoriteIcon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder

    val menuItems = mutableListOf(
        SongMenuItem(stringResource(R.string.add_to_playlist), Icons.AutoMirrored.Filled.PlaylistAdd, onAddToPlaylist),
        SongMenuItem(favoriteTitle, favoriteIcon, onFavoriteClick),
        SongMenuItem(stringResource(R.string.share_song), Icons.Default.Share, onShareClick),
        SongMenuItem(stringResource(R.string.song_info), Icons.Default.Info, onInfoClick),
        SongMenuItem(stringResource(R.string.edit_tags), Icons.Default.Edit, onTagEditClick),
    )

    if (onRemoveFromPlaylist != null) {
        menuItems.add(SongMenuItem(stringResource(R.string.remove_from_playlist), Icons.Default.RemoveCircleOutline, onRemoveFromPlaylist))
    }

    menuItems.add(SongMenuItem(stringResource(R.string.delete_from_device), Icons.Default.Delete, onDeleteClick))

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
