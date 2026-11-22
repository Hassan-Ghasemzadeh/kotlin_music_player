package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalAlbumArtworkEnabled
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ExplosiveParticlesVisualizer(
    fftData: FloatArray,
    albumArtUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = Dp.Unspecified,
    discRatio: Float = 0.86f,
    discSize: Dp = Dp.Unspecified,
) {
    val context = LocalContext.current
    val renderer = remember { ExplosiveParticlesRenderer() }

    renderer.updateFft(fftData)
    renderer.discRatio = discRatio

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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, glSurfaceView, isPlaying) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    glSurfaceView.onPause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlaying) {
                        glSurfaceView.onResume()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    glSurfaceView.onPause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            glSurfaceView.onPause()
        }
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val maxAvailable = minOf(this.maxWidth, this.maxHeight)
        val effectiveSize = if ((size != Dp.Unspecified) && (size < maxAvailable)) size else maxAvailable
        val actualDiscSize = if (discSize != Dp.Unspecified) discSize else effectiveSize * discRatio

        Box(
            modifier = Modifier.size(effectiveSize),
            contentAlignment = Alignment.Center,
        ) {
            // Layer 1: Central Circular Album Art Disc
            Box(
                modifier = Modifier
                    .size(actualDiscSize)
                    .clip(CircleShape)
                    .background(Color(0xFF1D1838))
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFFFB300),
                                Color(0xFFFF5722),
                                Color(0xFFFF2D55),
                                Color(0xFFFFB300),
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

            // Layer 2: OpenGL Visualizer Overlay
            AndroidView(
                factory = { glSurfaceView },
                modifier = Modifier.size(effectiveSize),
            ) { view ->
                if (isPlaying) {
                    view.onResume()
                    view.visibility = View.VISIBLE
                } else {
                    view.onPause()
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }
}