package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalAlbumArtworkEnabled
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Jetpack Compose wrapper for the OpenGL-based CrystalSonic visualizer.
 * Uses a uniform outer ring dimension (420.dp) and central album art disc (340.dp)
 * to ensure exact visual alignment across all visualizer styles.
 */
@Composable
fun CrystalSonicVisualizer(
    fftData: FloatArray,
    albumArtUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 420.dp,
    discSize: Dp = Dp.Unspecified,
) {
    val context = LocalContext.current
    val renderer = remember { CrystalSonicRenderer() }

    renderer.updateFft(fftData)

    val albumArtBytes by produceState<Bitmap?>(initialValue = null, key1 = albumArtUrl) {
        value = if (!albumArtUrl.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                albumArtUrl.toUri().extractAlbumArt(context)
            }
        } else {
            null
        }
    }

    val glSurfaceView = remember {
        GLSurfaceView(context).apply {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0)
            holder.setFormat(PixelFormat.TRANSLUCENT)
            setZOrderOnTop(true)
            setEGLContextClientVersion(2)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            glSurfaceView.onPause()
        }
    }

    val actualDiscSize = if (discSize != Dp.Unspecified) discSize else size * (340f / 420f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        // Layer 1: OpenGL Audio-Reactive DNA Waveform Ring & Particles Overlay
        AndroidView(
            factory = { glSurfaceView },
            modifier = Modifier.size(size),
        ) { view ->
            if (isPlaying) {
                view.onResume()
            } else {
                view.onPause()
            }
        }

        // Layer 2: Central Circular Album Art Disc (340dp proportionally scaled)
        Box(
            modifier = Modifier
                .size(actualDiscSize)
                .clip(CircleShape)
                .background(Color(0xFF1D1838))
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFB300), // Golden Yellow
                            Color(0xFFFF5722), // Deep Warm Orange
                            Color(0xFFFF2D55), // Vibrant Coral Pink
                            Color(0xFFFFB300), // Golden Yellow
                        ),
                    ),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            val isAlbumArtworkEnabled = LocalAlbumArtworkEnabled.current
            if (isAlbumArtworkEnabled && (albumArtBytes != null)) {
                AsyncImage(
                    model = albumArtBytes,
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                Image(
                    painter = painterResource(id = com.sftwr.cleandevelopment.musicplayer.home.R.drawable.ic_default_album_art),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}
