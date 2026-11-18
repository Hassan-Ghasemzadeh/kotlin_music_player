package com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalColorPalette
import com.sftwr.cleandevelopment.musicplayer.home.R

@Composable
fun LyricSourceDialog(
    onDismissRequest: () -> Unit,
    onSelectOnlineSearch: () -> Unit,
    onSelectLocalFile: () -> Unit,
    onSelectManualEdit: () -> Unit,
    onSelectManageSaved: () -> Unit
) {
    val accent = LocalColorPalette.current.accent

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E28)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.lyric_source_dialog_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LyricOptionItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.lyric_search_online),
                    accentColor = accent,
                    onClick = {
                        onDismissRequest()
                        onSelectOnlineSearch()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LyricOptionItem(
                    icon = Icons.Default.FolderOpen,
                    title = stringResource(R.string.lyric_local_files),
                    accentColor = accent,
                    onClick = {
                        onDismissRequest()
                        onSelectLocalFile()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LyricOptionItem(
                    icon = Icons.Default.Edit,
                    title = stringResource(R.string.lyric_add_edit_manually),
                    accentColor = accent,
                    onClick = {
                        onDismissRequest()
                        onSelectManualEdit()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LyricOptionItem(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = stringResource(R.string.lyric_manage_saved),
                    accentColor = accent,
                    onClick = {
                        onDismissRequest()
                        onSelectManageSaved()
                    }
                )
            }
        }
    }
}

@Composable
private fun LyricOptionItem(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accentColor,
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White
            ),
            modifier = Modifier.weight(1f)
        )
    }
}
