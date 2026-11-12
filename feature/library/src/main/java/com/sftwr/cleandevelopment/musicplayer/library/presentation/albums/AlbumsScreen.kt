package com.sftwr.cleandevelopment.musicplayer.library.presentation.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalGlassmorphicEnabled
import com.sftwr.cleandevelopment.musicplayer.domain.model.Album
import com.sftwr.cleandevelopment.musicplayer.home.R
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding
import com.sftwr.cleandevelopment.musicplayer.library.presentation.components.ScrollToTopButton
import kotlinx.coroutines.launch

/**
 * Stateless AlbumsScreen rendering music albums in a 2-column LazyVerticalGrid with dark glassmorphic design.
 */
@Composable
fun AlbumsScreen(
    albums: List<Album>,
    isLoading: Boolean,
    onAlbumClick: (Long, String) -> Unit,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomPadding = rememberPlayerAwareBottomPadding()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F0B21),
                            Color(0xFF181035),
                            Color(0xFF2C1454)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }

                    Text(
                        text = stringResource(R.string.albums),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF91D8FB))
                            }
                        }
                        albums.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_albums_found),
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                            }
                        }
                        else -> {
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = bottomPadding)
                            ) {
                                items(albums, key = { it.albumId }) { album ->
                                    AlbumItemCard(
                                        album = album,
                                        onClick = { onAlbumClick(album.albumId, album.title) }
                                    )
                                }
                            }
                        }
                    }

                    ScrollToTopButton(
                        isScrolling = gridState.isScrollInProgress,
                        onClick = {
                            coroutineScope.launch {
                                gridState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumItemCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlassmorphic = LocalGlassmorphicEnabled.current
    val backgroundColor = if (isGlassmorphic) Color.White.copy(alpha = 0.06f) else Color(0xFF1B1238)
    val borderColor = if (isGlassmorphic) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f)
    val imageBackground = if (isGlassmorphic) Color.White.copy(alpha = 0.08f) else Color(0xFF2C1454)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(imageBackground),
            contentAlignment = Alignment.Center
        ) {
            if (album.albumArtUri != null) {
                AsyncImage(
                    model = album.albumArtUri,
                    contentDescription = album.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = Color(0xFF91D8FB).copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = album.title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "${album.artist} • ${album.trackCount}",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
