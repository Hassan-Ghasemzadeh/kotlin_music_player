package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.sftwr.cleandevelopment.musicplayer.library.core.util.extractAlbumArt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.CircularVisualizerType

/**
 * NeonCircularVisualizer renders complex audio frequency visualizer effects around a vinyl-like album art container.
 */
@Composable
fun NeonCircularVisualizer(
    type: CircularVisualizerType,
    fftData: FloatArray,
    albumArtUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val context = LocalContext.current

    // Extract embedded album art bytes from the audio file path
    val albumArtBytes = remember(albumArtUrl) {
        if (!albumArtUrl.isNullOrBlank()) {
            albumArtUrl.toUri().extractAlbumArt(context)
        } else {
            null
        }
    }

    // Rotation animation for the vinyl-like album art disc
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ), label = "rotation"
    )

    val neonCyan = Color(0xFF00D2FF)
    val neonBlue = Color(0xFF0055FF)
    val neonPurple = Color(0xFFA000FF)
    val neonPink = Color(0xFFFF007F)
    val neonGreen = Color(0xFF00FF66)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Rotating album art or fallback icon layer resembling a gramophone record
        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (isPlaying) rotationAngle else 0f)
                .clip(CircleShape)
                .background(Color(0xFF1D1838)),
            contentAlignment = Alignment.Center
        ) {
            if (albumArtBytes != null) {
                AsyncImage(
                    model = albumArtBytes,
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Album Art Placeholder",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        // Static gradient neon border wrapping the circular container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(neonCyan, neonBlue, neonPurple, neonCyan)
                    ),
                    shape = CircleShape
                )
        )

        // Canvas drawing area for rendering various audio frequency visualizer effects
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val baseRadius = (size.toPx() / 2) - 3f
            val pointsCount = if (fftData.isNotEmpty()) fftData.size else 64

            when (type) {
                CircularVisualizerType.NEON_FREQUENCY_BARS -> {
                    for (i in 0 until pointsCount) {
                        val angle =
                            (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = if (i < fftData.size) fftData[i] else 0.2f
                        val barLen = 20f + (amp * 90f)
                        val start = Offset(
                            (center.x + baseRadius * cos(angle)).toFloat(),
                            (center.y + baseRadius * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (baseRadius + barLen) * cos(angle)).toFloat(),
                            (center.y + (baseRadius + barLen) * sin(angle)).toFloat()
                        )
                        drawLine(
                            brush = Brush.radialGradient(listOf(neonCyan, neonBlue)),
                            start = start,
                            end = end,
                            strokeWidth = 3.5f
                        )
                    }
                }

                CircularVisualizerType.NEON_ORBITAL_WAVE -> {
                    val path1 = Path()
                    val path2 = Path()
                    for (i in 0..pointsCount) {
                        val angle = (2 * PI * (i % pointsCount) / pointsCount)
                        val amp = if (i < fftData.size) fftData[i] else 0.2f
                        val r1 = (baseRadius + 8f) + (amp * 45f * sin(
                            angle * 4 + Math.toRadians(rotationAngle.toDouble())
                        )).toFloat()
                        val r2 = (baseRadius + 8f) + (amp * 45f * cos(
                            angle * 4 - Math.toRadians(rotationAngle.toDouble())
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
                    drawPath(path2, neonPurple, style = Stroke(width = 3f))
                }

                CircularVisualizerType.NEON_POLYGON_RING -> {
                    val points = mutableListOf<Offset>()
                    for (i in 0 until pointsCount) {
                        val angle =
                            (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = if (i < fftData.size) fftData[i] else 0.2f
                        val r = baseRadius + 12f + (amp * 45f)
                        points.add(
                            Offset(
                                (center.x + r * cos(angle)).toFloat(),
                                (center.y + r * sin(angle)).toFloat()
                            )
                        )
                    }
                    for (i in points.indices) {
                        val next = points[(i + 1) % points.size]
                        val cross = points[(i + 5) % points.size]
                        drawLine(neonCyan, points[i], next, strokeWidth = 2f)
                        drawLine(neonBlue.copy(alpha = 0.6f), points[i], cross, strokeWidth = 1.2f)
                    }
                }

                CircularVisualizerType.NEON_SPIRAL_PULSE -> {
                    val path = Path()
                    for (i in 0..120) {
                        val angle = (4 * PI * i / 120) + Math.toRadians(rotationAngle.toDouble())
                        val fftIdx = i % pointsCount
                        val amp = if (fftIdx < fftData.size) fftData[fftIdx] else 0.2f
                        val r = baseRadius + (i * 0.35f) + (amp * 25f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonCyan, style = Stroke(width = 2.5f))
                }

                CircularVisualizerType.NEON_STARBURST_SPIKES -> {
                    for (i in 0 until pointsCount) {
                        val angle =
                            (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = if (i < fftData.size) fftData[i] else 0.2f
                        val spikeLen = 15f + (amp * 70f)
                        val start = Offset(
                            (center.x + baseRadius * cos(angle)).toFloat(),
                            (center.y + baseRadius * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (baseRadius + spikeLen) * cos(angle)).toFloat(),
                            (center.y + (baseRadius + spikeLen) * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = if (i % 2 == 0) neonCyan else neonPurple,
                            start = start,
                            end = end,
                            strokeWidth = if (amp > 0.4f) 3.5f else 1.5f
                        )
                    }
                }

                CircularVisualizerType.NEON_HARMONIC_RINGS -> {
                    val ringCount = 3
                    for (rIdx in 1..ringCount) {
                        val avgAmp = fftData.take(rIdx * 10).average().toFloat()
                        val currentRadius = baseRadius + (rIdx * 14f) + (avgAmp * 30f)
                        drawCircle(
                            color = if (rIdx % 2 == 0) neonCyan else neonBlue,
                            radius = currentRadius,
                            center = center,
                            style = Stroke(width = 2f)
                        )
                    }
                }

                CircularVisualizerType.NEON_CYMATICS_PATTERN -> {
                    val path = Path()
                    val harmonics = 8
                    for (i in 0..360) {
                        val angle = Math.toRadians(i.toDouble())
                        val fftIdx = (i / 6) % pointsCount
                        val amp = if (fftIdx < fftData.size) fftData[fftIdx] else 0.2f
                        val modulation = sin(angle * harmonics) * (amp * 40f)
                        val r = baseRadius + 10f + modulation.toFloat()
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonCyan, style = Stroke(width = 2.5f))
                }

                CircularVisualizerType.NEON_CRYSTAL_MATRIX -> {
                    for (i in 0 until pointsCount step 2) {
                        val angle =
                            (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = if (i < fftData.size) fftData[i] else 0.2f
                        val r = baseRadius + 15f + (amp * 40f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        drawRect(
                            color = neonCyan,
                            topLeft = Offset(x - 4f, y - 4f),
                            size = androidx.compose.ui.geometry.Size(8f, 8f)
                        )
                    }
                }

                CircularVisualizerType.NEON_FRACTAL_FLOWER -> {
                    val petals = 12
                    val path = Path()
                    for (i in 0..360) {
                        val angle = Math.toRadians(i.toDouble())
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val r =
                            baseRadius + 12f + (sin(angle * petals).toFloat() * (15f + amp * 35f))
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonPurple, style = Stroke(width = 2.5f))
                }

                CircularVisualizerType.NEON_SINGULARITY_VORTEX -> {
                    val path = Path()
                    for (i in 0 until pointsCount) {
                        val angle =
                            (2 * PI * i / pointsCount) - Math.toRadians(rotationAngle.toDouble() * 1.5)
                        val amp = if (i < fftData.size) fftData[i] else 0.2f
                        val r = baseRadius + 5f + ((1f - amp) * 30f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonPink, style = Stroke(width = 3f))
                }

                CircularVisualizerType.NEON_QUANTUM_FLUX -> {
                    for (i in 0 until pointsCount step 3) {
                        val angle = (2 * PI * i / pointsCount)
                        val amp = if (i < fftData.size) fftData[i] else 0.2f
                        val offsetDist = amp * 60f
                        val start = Offset(
                            (center.x + baseRadius * cos(angle)).toFloat(),
                            (center.y + baseRadius * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (baseRadius + offsetDist) * cos(angle + 0.1)).toFloat(),
                            (center.y + (baseRadius + offsetDist) * sin(angle + 0.1)).toFloat()
                        )
                        drawLine(neonGreen, start, end, strokeWidth = 2.5f)
                    }
                }

                CircularVisualizerType.NEON_DNA_HELIX_PULSE -> {
                    val path1 = Path()
                    val path2 = Path()
                    for (i in 0..100) {
                        val angle = (4 * PI * i / 100) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val r1 = baseRadius + 15f + (amp * 35f)
                        val r2 = baseRadius + 15f - (amp * 35f)
                        val x1 = (center.x + r1 * cos(angle)).toFloat()
                        val y1 = (center.y + r1 * sin(angle)).toFloat()
                        val x2 = (center.x + r2 * cos(angle + PI)).toFloat()
                        val y2 = (center.y + r2 * sin(angle + PI)).toFloat()
                        if (i == 0) {
                            path1.moveTo(x1, y1); path2.moveTo(x2, y2)
                        } else {
                            path1.lineTo(x1, y1); path2.lineTo(x2, y2)
                        }
                    }
                    drawPath(path1, neonCyan, style = Stroke(width = 2f))
                    drawPath(path2, neonPink, style = Stroke(width = 2f))
                }

                CircularVisualizerType.NEON_HYPERSPACE_TUNNEL -> {
                    for (r in 1..4) {
                        val avgAmp = fftData.average().toFloat()
                        val radius = baseRadius + (r * 18f) + (avgAmp * 25f * r)
                        drawCircle(
                            color = if (r % 2 == 0) neonCyan.copy(alpha = 0.7f) else neonPurple.copy(
                                alpha = 0.7f
                            ),
                            radius = radius,
                            center = center,
                            style = Stroke(width = (5 - r).toFloat())
                        )
                    }
                }

                CircularVisualizerType.NEON_NEURAL_SYNAPSE -> {
                    val points = mutableListOf<Offset>()
                    for (i in 0 until 16) {
                        val angle = (2 * PI * i / 16) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i * 4) { 0.2f }
                        val r = baseRadius + 20f + (amp * 40f)
                        points.add(
                            Offset(
                                (center.x + r * cos(angle)).toFloat(),
                                (center.y + r * sin(angle)).toFloat()
                            )
                        )
                    }
                    for (i in points.indices) {
                        for (j in i + 1 until points.size) {
                            if ((i - j) % 3 == 0) {
                                drawLine(
                                    neonCyan.copy(alpha = 0.4f),
                                    points[i],
                                    points[j],
                                    strokeWidth = 1f
                                )
                            }
                        }
                    }
                }

                CircularVisualizerType.NEON_PLASMA_STORM -> {
                    val path = Path()
                    for (i in 0..180) {
                        val angle =
                            Math.toRadians(i * 2.0) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val noise =
                            sin(i.toDouble() * 0.5 + rotationAngle.toDouble() * 0.1).toFloat()
                        val r = baseRadius + 10f + (amp * kotlin.math.abs(noise) * 55f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonGreen, style = Stroke(width = 1.5f))
                }

                CircularVisualizerType.NEON_SUPERNOVA_BURST -> {
                    val avgAmp = fftData.average().toFloat()
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(neonCyan.copy(alpha = avgAmp * 0.8f), Color.Transparent),
                            center = center,
                            radius = baseRadius + (avgAmp * 80f)
                        ),
                        radius = baseRadius + (avgAmp * 80f),
                        center = center
                    )
                }

                CircularVisualizerType.NEON_CYBER_MATRIX_GRID -> {
                    for (i in 0 until 12) {
                        val angle = (2 * PI * i / 12) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i * 5) { 0.2f }
                        val start = Offset(
                            (center.x + baseRadius * cos(angle)).toFloat(),
                            (center.y + baseRadius * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (baseRadius + 30f + amp * 40f) * cos(angle)).toFloat(),
                            (center.y + (baseRadius + 30f + amp * 40f) * sin(angle)).toFloat()
                        )
                        drawLine(neonGreen, start, end, strokeWidth = 2f)
                    }
                }

                CircularVisualizerType.NEON_GRAVITATIONAL_RING -> {
                    val path = Path()
                    for (i in 0..pointsCount) {
                        val angle = (2 * PI * i / pointsCount)
                        val amp = fftData[i % pointsCount]
                        val r =
                            baseRadius + 12f + (sin(angle * 3 + Math.toRadians(rotationAngle.toDouble())) * amp * 35f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonBlue, style = Stroke(width = 3.5f))
                }
            }
        }
    }
}