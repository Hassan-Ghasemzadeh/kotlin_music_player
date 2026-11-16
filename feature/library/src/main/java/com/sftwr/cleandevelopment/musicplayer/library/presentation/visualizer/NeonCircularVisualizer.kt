package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalAlbumArtworkEnabled
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import com.sftwr.cleandevelopment.musicplayer.model.CircularVisualizerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * NeonCircularVisualizer renders circular audio visualizer effects
 * radiating outward around a central album art disc.
 * Standardized to outer ring size 420.dp and central album art disc 340.dp (discRatio = 340/420).
 */
@Composable
fun NeonCircularVisualizer(
    type: CircularVisualizerType,
    fftData: FloatArray,
    albumArtUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 420.dp,
    discRatio: Float = 340f / 420f,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val context = LocalContext.current

    // Extract embedded album art bytes asynchronously on IO thread
    val albumArtBytes by produceState<Bitmap?>(initialValue = null, key1 = albumArtUrl) {
        val url = albumArtUrl
        value = if (!url.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                url.toUri().extractAlbumArt(context)
            }
        } else {
            null
        }
    }

    // Rotation animation for the vinyl album art disc and visualizer orbits
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
        ),
        label = "rotation",
    )

    val neonCyan = Color(0xFF00D2FF)
    val neonBlue = Color(0xFF0055FF)
    val neonPurple = Color(0xFFA000FF)
    val neonPink = Color(0xFFFF007F)
    val neonGreen = Color(0xFF00FF66)
    val neonGold = Color(0xFFFFD700)

    // Inner vinyl album disc diameter
    val innerDiscSize = size * discRatio

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        // 1. Render specific standalone visualizers if matched, otherwise render Canvas visualizers
        when (type) {
            CircularVisualizerType.CRYSTAL_SONIC -> {
                CrystalSonicVisualizer(
                    fftData = fftData,
                    albumArtUrl = albumArtUrl,
                    isPlaying = isPlaying,
                    size = size,
                    discSize = innerDiscSize,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            CircularVisualizerType.DYNAMIC_SCALE -> {
                DynamicScaleVisualizer(
                    fftData = fftData,
                    albumArtUrl = albumArtUrl,
                    isPlaying = isPlaying,
                    size = size,
                    discSize = innerDiscSize,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            CircularVisualizerType.EXPLOSIVE_PARTICLES -> {
                ExplosiveParticlesVisualizer(
                    fftData = fftData,
                    albumArtUrl = albumArtUrl,
                    isPlaying = isPlaying,
                    size = size,
                    discSize = innerDiscSize,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            CircularVisualizerType.PSYCHEDELIC_WAVES -> {
                PsychedelicWavesVisualizer(
                    fftData = fftData,
                    albumArtUrl = albumArtUrl,
                    isPlaying = isPlaying,
                    size = size,
                    discSize = innerDiscSize,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            else -> {
                // Canvas drawing area rendering dynamic visualizer effects around the inner album disc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.toPx() / 2, size.toPx() / 2)
                    val baseRadius = (innerDiscSize.toPx() / 2) + 2f
                    val maxOutward = (size.toPx() / 2) - baseRadius - 4f
                    val safeMaxOutward = maxOutward.coerceAtLeast(10f)

                    // استفاده از مقدار ایمن برای جلوگیری از صفر بودن داده‌ها در پیش‌نمایش
                    val pointsCount = 64
                    val safeFft = if (fftData.isNotEmpty()) fftData else FloatArray(pointsCount) { 0.3f }

                    when (type) {
                        CircularVisualizerType.NEON_FREQUENCY_BARS -> {
                            for (i in 0 until pointsCount) {
                                val angle =
                                    (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val barLen = 6f + (amp * safeMaxOutward * 0.85f)
                                val start = Offset(
                                    (center.x + baseRadius * cos(angle)).toFloat(),
                                    (center.y + baseRadius * sin(angle)).toFloat(),
                                )
                                val end = Offset(
                                    (center.x + (baseRadius + barLen) * cos(angle)).toFloat(),
                                    (center.y + (baseRadius + barLen) * sin(angle)).toFloat(),
                                )
                                drawLine(
                                    brush = Brush.radialGradient(listOf(neonCyan, neonBlue)),
                                    start = start,
                                    end = end,
                                    strokeWidth = 3.5f,
                                    cap = StrokeCap.Round,
                                )
                            }
                        }

                        CircularVisualizerType.NEON_ORBITAL_WAVE -> {
                            val path1 = Path()
                            val path2 = Path()
                            for (i in 0..pointsCount) {
                                val angle = (2 * PI * (i % pointsCount) / pointsCount)
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r1 = (baseRadius + 4f) + (amp * safeMaxOutward * 0.75f * sin(
                                    angle * 4 + Math.toRadians(rotationAngle.toDouble()),
                                )).toFloat()
                                val r2 = (baseRadius + 4f) + (amp * safeMaxOutward * 0.75f * cos(
                                    angle * 4 - Math.toRadians(rotationAngle.toDouble()),
                                )).toFloat()

                                val x1 = (center.x + r1 * cos(angle)).toFloat()
                                val y1 = (center.y + r1 * sin(angle)).toFloat()
                                val x2 = (center.x + r2 * cos(angle)).toFloat()
                                val y2 = (center.y + r2 * sin(angle)).toFloat()

                                if (i == 0) {
                                    path1.moveTo(x1, y1)
                                    path2.moveTo(x2, y2)
                                } else {
                                    path1.lineTo(x1, y1)
                                    path2.lineTo(x2, y2)
                                }
                            }
                            drawPath(path1, neonCyan, style = Stroke(width = 3f))
                            drawPath(path2, neonPink, style = Stroke(width = 3f))
                        }

                        CircularVisualizerType.NEON_POLYGON_RING -> {
                            val points = mutableListOf<Offset>()
                            for (i in 0 until pointsCount) {
                                val angle =
                                    (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r = baseRadius + 4f + (amp * safeMaxOutward * 0.75f)
                                points.add(
                                    Offset(
                                        (center.x + r * cos(angle)).toFloat(),
                                        (center.y + r * sin(angle)).toFloat(),
                                    ),
                                )
                            }
                            for (i in points.indices) {
                                val next = points[(i + 1) % points.size]
                                val cross = points[(i + 5) % points.size]
                                drawLine(neonCyan, points[i], next, strokeWidth = 2f)
                                drawLine(
                                    neonBlue.copy(alpha = 0.5f),
                                    points[i],
                                    cross,
                                    strokeWidth = 1.2f
                                )
                            }
                        }

                        CircularVisualizerType.NEON_SPIRAL_PULSE -> {
                            val path = Path()
                            for (i in 0..120) {
                                val angle =
                                    (4 * PI * i / 120) + Math.toRadians(rotationAngle.toDouble())
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r =
                                    baseRadius + (i * (safeMaxOutward / 140f)) + (amp * safeMaxOutward * 0.5f)
                                val x = (center.x + r * cos(angle)).toFloat()
                                val y = (center.y + r * sin(angle)).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, neonGold, style = Stroke(width = 2.5f))
                        }

                        CircularVisualizerType.NEON_CRYSTAL_MATRIX -> {
                            for (i in 0 until pointsCount step 2) {
                                val angle =
                                    (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r = baseRadius + 6f + (amp * safeMaxOutward * 0.75f)
                                val x = (center.x + r * cos(angle)).toFloat()
                                val y = (center.y + r * sin(angle)).toFloat()
                                drawRect(
                                    color = if (i % 4 == 0) neonCyan else neonPurple,
                                    topLeft = Offset(x - 4f, y - 4f),
                                    size = Size(8f, 8f),
                                )
                            }
                        }

                        CircularVisualizerType.NEON_CYMATICS_PATTERN -> {
                            val particleCount = 24
                            for (i in 0 until particleCount) {
                                val angle =
                                    (2 * PI * i / particleCount) + Math.toRadians(rotationAngle.toDouble() * (if (i % 2 == 0) 1.5 else -1.5))
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r = baseRadius + 4f + (amp * safeMaxOutward * 0.8f)
                                val x = (center.x + r * cos(angle)).toFloat()
                                val y = (center.y + r * sin(angle)).toFloat()
                                drawCircle(
                                    color = if (i % 3 == 0) neonCyan else neonPink,
                                    radius = 3.5f + (amp * 4f),
                                    center = Offset(x, y),
                                )
                            }
                        }


                        CircularVisualizerType.NEON_FRACTAL_FLOWER -> {
                            for (i in 0 until pointsCount) {
                                val angle =
                                    (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble() * 2.0)
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r =
                                    baseRadius + (i * (safeMaxOutward / pointsCount)) + (amp * 10f)
                                val x = (center.x + r * cos(angle)).toFloat()
                                val y = (center.y + r * sin(angle)).toFloat()
                                drawCircle(neonGreen, radius = 2.5f, center = Offset(x, y))
                            }
                        }

                        CircularVisualizerType.NEON_SINGULARITY_VORTEX -> {
                            for (i in 0 until pointsCount) {
                                val angle =
                                    (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r = baseRadius + 4f + (amp * safeMaxOutward * 0.7f)
                                val start = Offset(
                                    (center.x + baseRadius * cos(angle)).toFloat(),
                                    (center.y + baseRadius * sin(angle)).toFloat(),
                                )
                                val end = Offset(
                                    (center.x + r * cos(angle)).toFloat(),
                                    (center.y + r * sin(angle)).toFloat(),
                                )
                                drawLine(
                                    color = neonPurple,
                                    start = start,
                                    end = end,
                                    strokeWidth = 2f,
                                )
                                if (i % 2 == 0) {
                                    drawCircle(neonPink, radius = 3f, center = end)
                                }
                            }
                        }


                        CircularVisualizerType.NEON_HYPERSPACE_TUNNEL -> {
                            for (i in 0 until pointsCount) {
                                val angle = (2 * PI * i / pointsCount)
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r = baseRadius + 2f + (amp * safeMaxOutward)
                                val start = Offset(
                                    (center.x + baseRadius * cos(angle)).toFloat(),
                                    (center.y + baseRadius * sin(angle)).toFloat(),
                                )
                                val end = Offset(
                                    (center.x + r * cos(angle)).toFloat(),
                                    (center.y + r * sin(angle)).toFloat(),
                                )
                                drawLine(neonCyan.copy(alpha = 0.7f), start, end, strokeWidth = 2f)
                            }
                        }

                        CircularVisualizerType.NEON_NEURAL_SYNAPSE -> {
                            val points = mutableListOf<Offset>()
                            for (i in 0 until 18) {
                                val angle =
                                    (2 * PI * i / 18) + Math.toRadians(rotationAngle.toDouble() * 0.5)
                                val amp = safeFft.getOrElse((i * 3) % safeFft.size) { 0.3f }
                                val r = baseRadius + 6f + (amp * safeMaxOutward * 0.75f)
                                points.add(
                                    Offset(
                                        (center.x + r * cos(angle)).toFloat(),
                                        (center.y + r * sin(angle)).toFloat(),
                                    ),
                                )
                            }
                            for (i in points.indices) {
                                for (j in i + 1 until points.size) {
                                    if ((i - j) % 3 == 0) {
                                        drawLine(
                                            neonCyan.copy(alpha = 0.4f),
                                            points[i],
                                            points[j],
                                            strokeWidth = 1f,
                                        )
                                    }
                                }
                                drawCircle(neonCyan, radius = 3f, center = points[i])
                            }
                        }

                        CircularVisualizerType.NEON_PLASMA_STORM -> {
                            val path = Path()
                            for (i in 0..180) {
                                val angle =
                                    Math.toRadians(i * 2.0) + Math.toRadians(rotationAngle.toDouble())
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val noise =
                                    sin(i.toDouble() * 0.5 + rotationAngle.toDouble() * 0.1).toFloat()
                                val r = baseRadius + 6f + (amp * abs(noise) * safeMaxOutward * 0.8f)
                                val x = (center.x + r * cos(angle)).toFloat()
                                val y = (center.y + r * sin(angle)).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, neonGreen, style = Stroke(width = 1.5f))
                        }

                        CircularVisualizerType.NEON_CYBER_MATRIX_GRID -> {
                            val barCount = 72
                            for (i in 0 until barCount) {
                                val angle = (2 * PI * i / barCount) + Math.toRadians(rotationAngle.toDouble() * 0.5)
                                val amp = safeFft.getOrElse((i * 2) % safeFft.size) { 0.3f }

                                val blockCount = (2 + (amp * 8).toInt()).coerceIn(2, 10)

                                for (b in 0 until blockCount) {
                                    val r = baseRadius + 6f + (b * 9f)
                                    val x = (center.x + r * cos(angle)).toFloat()
                                    val y = (center.y + r * sin(angle)).toFloat()

                                    val dotColor = when {
                                        b == blockCount - 1 -> neonCyan
                                        i % 3 == 0 -> neonGreen
                                        else -> neonGreen.copy(alpha = 0.7f)
                                    }

                                    drawCircle(
                                        color = dotColor,
                                        radius = 2f + (amp * 1.5f),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }

                        CircularVisualizerType.NEON_GRAVITATIONAL_RING -> {
                            val path = Path()
                            for (i in 0..pointsCount) {
                                val angle = (2 * PI * i / pointsCount)
                                val amp = safeFft.getOrElse(i % safeFft.size) { 0.3f }
                                val r =
                                    baseRadius + 6f + (sin(angle * 3 + Math.toRadians(rotationAngle.toDouble())) * amp * safeMaxOutward * 0.6f)
                                val x = (center.x + r * cos(angle)).toFloat()
                                val y = (center.y + r * sin(angle)).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, neonBlue, style = Stroke(width = 3.5f))
                        }

                        else -> {}
                    }
                }
            }
        }

        // 2. Inner rotating vinyl album art disc
        Box(
            modifier = Modifier
                .size(innerDiscSize)
                .rotate(if (isPlaying) rotationAngle else 0f)
                .clip(CircleShape)
                .background(Color(0xFF1D1838)),
            contentAlignment = Alignment.Center,
        ) {
            val isAlbumArtworkEnabled = LocalAlbumArtworkEnabled.current
            if (isAlbumArtworkEnabled && albumArtBytes != null) {
                AsyncImage(
                    model = albumArtBytes,
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Image(
                    painter = painterResource(id = com.sftwr.cleandevelopment.musicplayer.home.R.drawable.ic_default_album_art),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        // 3. Static gradient neon border around the inner vinyl disc
        Box(
            modifier = Modifier
                .size(innerDiscSize)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(neonCyan, neonBlue, neonPurple, neonCyan),
                    ),
                    shape = CircleShape,
                ),
        )
    }
}