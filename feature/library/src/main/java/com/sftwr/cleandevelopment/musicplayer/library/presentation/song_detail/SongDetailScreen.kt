package com.sftwr.cleandevelopment.musicplayer.library.presentation.song_detail


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sftwr.cleandevelopment.musicplayer.home.R
import com.sftwr.cleandevelopment.musicplayer.library.core.util.rememberPlayerAwareBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    songDetail: SongDetailUiModel?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    val neonBlue = Color(0xFF00D2FF)
    val darkPurple = Color(0xFF0F0B21)
    val glassColor = Color.White.copy(alpha = 0.05f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkPurple)
    ) {

        if (songDetail != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(songDetail.albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 50.dp)
                    .alpha(0.35f),
                error = painterResource(id = R.drawable.ic_default_album_art),
                placeholder = painterResource(id = R.drawable.ic_default_album_art)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                darkPurple.copy(alpha = 0.5f),
                                darkPurple.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("اطلاعات آهنگ", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->

            if (songDetail == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    if (error != null) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        CircularProgressIndicator(color = neonBlue)
                    }
                }
            } else {
                val playerBottomPadding = rememberPlayerAwareBottomPadding()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 10.dp,
                            bottom = 10.dp + playerBottomPadding
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = songDetail.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = songDetail.artist,
                        style = MaterialTheme.typography.titleSmall,
                        color = neonBlue,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier
                            .size(220.dp)
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp),
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(songDetail.albumArtUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "کاور آلبوم",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.15f)),
                            error = painterResource(id = R.drawable.ic_default_album_art),
                            placeholder = painterResource(id = R.drawable.ic_default_album_art)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(glassColor)
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "مشخصات فایل",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = neonBlue,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            InfoRow(label = "آلبوم", value = songDetail.album)
                            InfoRow(label = "ژانر", value = songDetail.genre.ifEmpty { "نامشخص" })
                            InfoRow(
                                label = "سال انتشار",
                                value = songDetail.year.ifEmpty { "نامشخص" })
                            InfoRow(label = "زمان", value = songDetail.duration)
                            InfoRow(label = "حجم", value = songDetail.size)
                            InfoRow(label = "فرمت", value = songDetail.mimeType)
                            InfoRow(
                                label = "بیت‌ریت",
                                value = if (songDetail.bitrate.isNotEmpty()) "${songDetail.bitrate} bps" else "نامشخص"
                            )
                            InfoRow(label = "فرکانس", value = songDetail.sampleRate)
                            InfoRow(
                                label = "مسیر ذخیره‌سازی",
                                value = songDetail.path,
                                maxLines = 4
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    maxLines: Int = 1
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Right,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Right,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}