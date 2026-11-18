package com.sftwr.cleandevelopment.musicplayer.library.presentation.lyrics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalColorPalette
import com.sftwr.cleandevelopment.musicplayer.home.R
import com.sftwr.cleandevelopment.musicplayer.model.Lyric
import com.sftwr.cleandevelopment.musicplayer.model.LyricLine

@Composable
fun SynchronizedLyricView(
    lyric: Lyric,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onEditClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalColorPalette.current.accent
    val listState = rememberLazyListState()

    val lines = lyric.lines
    val isSynced = lyric.isSynced

    // Calculate active index based on playback position
    val activeIndex = remember(currentPositionMs, lines, isSynced) {
        if (!isSynced || lines.isEmpty()) -1
        else {
            val idx = lines.indexOfLast { it.timeMs <= currentPositionMs }
            if (idx >= 0) idx else 0
        }
    }

    // Automatically scroll to active line
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lines.size) {
            listState.animateScrollToItem(
                index = maxOf(0, activeIndex - 2)
            )
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp)),
        color = Color.Black.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with title, edit button, and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.lyric_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = if (isSynced) stringResource(R.string.lyric_synced) else stringResource(R.string.lyric_plain_text),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accent.copy(alpha = 0.8f)
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.lyric_edit),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (lines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lyric.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    itemsIndexed(lines) { index, line ->
                        val isActive = index == activeIndex

                        val textColor by animateColorAsState(
                            targetValue = if (isActive) accent else Color.White.copy(alpha = 0.55f),
                            label = "textColor"
                        )

                        val textSizeSp = if (isActive) 20.sp else 16.sp
                        val textWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        val scaleFactor by animateFloatAsState(
                            targetValue = if (isActive) 1.08f else 1.0f,
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scaleFactor)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isActive) accent.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .clickable {
                                    if (isSynced && line.timeMs >= 0) {
                                        onSeekTo(line.timeMs)
                                    }
                                }
                                .padding(vertical = 10.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = textColor,
                                    fontSize = textSizeSp,
                                    fontWeight = textWeight,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
