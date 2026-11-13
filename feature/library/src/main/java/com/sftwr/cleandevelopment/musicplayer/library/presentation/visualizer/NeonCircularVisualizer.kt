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
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.CircularVisualizerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * NeonCircularVisualizer renders 18 distinct, neon-glowing circular audio visualizer effects
 * radiating outward around a rotating vinyl album art disc.
 */
@Composable
fun NeonCircularVisualizer(
    type: CircularVisualizerType,
    fftData: FloatArray,
    albumArtUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    discRatio: Float = 0.78f
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
        } else null
    }

    // Rotation animation for the vinyl album art disc and visualizer orbits
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
    val neonGold = Color(0xFFFFD700)

    // Inner vinyl album disc diameter
    val innerDiscSize = size * discRatio

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 1. Canvas drawing area rendering dynamic visualizer effects around the inner album disc
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val baseRadius = (innerDiscSize.toPx() / 2) + 2f
            val maxOutward = (size.toPx() / 2) - baseRadius - 4f
            val safeMaxOutward = maxOutward.coerceAtLeast(10f)
            val pointsCount = if (fftData.isNotEmpty()) fftData.size else 64

            when (type) {
                // 1. Dynamic Scale: Rounded frequency bars radiating outward
                CircularVisualizerType.NEON_FREQUENCY_BARS -> {
                    for (i in 0 until pointsCount) {
                        val angle = (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i) { 0.2f }
                        val barLen = 6f + (amp * safeMaxOutward * 0.85f)
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
                            strokeWidth = 3.5f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 2. Circular Wave: Dual rotating sinusoidal orbital wave rings
                CircularVisualizerType.NEON_ORBITAL_WAVE -> {
                    val path1 = Path()
                    val path2 = Path()
                    for (i in 0..pointsCount) {
                        val angle = (2 * PI * (i % pointsCount) / pointsCount)
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val r1 = (baseRadius + 4f) + (amp * safeMaxOutward * 0.75f * sin(
                            angle * 4 + Math.toRadians(rotationAngle.toDouble())
                        )).toFloat()
                        val r2 = (baseRadius + 4f) + (amp * safeMaxOutward * 0.75f * cos(
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
                    drawPath(path2, neonPink, style = Stroke(width = 3f))
                }

                // 3. Spectrum Bars: Geometric polygon ring with interconnected web lines
                CircularVisualizerType.NEON_POLYGON_RING -> {
                    val points = mutableListOf<Offset>()
                    for (i in 0 until pointsCount) {
                        val angle = (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i) { 0.2f }
                        val r = baseRadius + 4f + (amp * safeMaxOutward * 0.75f)
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
                        drawLine(neonBlue.copy(alpha = 0.5f), points[i], cross, strokeWidth = 1.2f)
                    }
                }

                // 4. Spiral Pulse: Archimedean spiral expanding with beat
                CircularVisualizerType.NEON_SPIRAL_PULSE -> {
                    val path = Path()
                    for (i in 0..120) {
                        val angle = (4 * PI * i / 120) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val r = baseRadius + (i * (safeMaxOutward / 140f)) + (amp * safeMaxOutward * 0.5f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonGold, style = Stroke(width = 2.5f))
                }

                // 5. Crystal Matrix: Grid of rotating glowing diamond crystal blocks
                CircularVisualizerType.NEON_CRYSTAL_MATRIX -> {
                    for (i in 0 until pointsCount step 2) {
                        val angle = (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i) { 0.2f }
                        val r = baseRadius + 6f + (amp * safeMaxOutward * 0.75f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        drawRect(
                            color = if (i % 4 == 0) neonCyan else neonPurple,
                            topLeft = Offset(x - 4f, y - 4f),
                            size = Size(8f, 8f)
                        )
                    }
                }

                // 6. Harmonic Rings: Concentric water-ripple rings expanding outward
                CircularVisualizerType.NEON_HARMONIC_RINGS -> {
                    val ringCount = 3
                    for (rIdx in 1..ringCount) {
                        val slice = fftData.take((rIdx * 10).coerceAtMost(fftData.size))
                        val avgAmp = if (slice.isNotEmpty()) slice.average().toFloat().takeIf { !it.isNaN() } ?: 0.2f else 0.2f
                        val currentRadius = baseRadius + (rIdx * (safeMaxOutward / 4f)) + (avgAmp * safeMaxOutward * 0.4f)
                        drawCircle(
                            color = if (rIdx % 2 == 0) neonCyan else neonPink,
                            radius = currentRadius,
                            center = center,
                            style = Stroke(width = (2.5f - rIdx * 0.3f).coerceAtLeast(1f))
                        )
                    }
                }

                // 7. Cymatics Pattern: 8-lobe harmonic geometric cymatics resonance pattern
                CircularVisualizerType.NEON_CYMATICS_PATTERN -> {
                    val path = Path()
                    val harmonics = 8
                    for (i in 0..360) {
                        val angle = Math.toRadians(i.toDouble())
                        val fftIdx = (i / 6) % pointsCount
                        val amp = fftData.getOrElse(fftIdx) { 0.2f }
                        val modulation = sin(angle * harmonics) * (amp * safeMaxOutward * 0.6f)
                        val r = baseRadius + 6f + modulation.toFloat()
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonCyan, style = Stroke(width = 2.5f))
                }

                // 8. Starburst Spikes: Alternating sharp purple and gold explosion rays
                CircularVisualizerType.NEON_STARBURST_SPIKES -> {
                    for (i in 0 until pointsCount) {
                        val angle = (2 * PI * i / pointsCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i) { 0.2f }
                        val spikeLen = 8f + (amp * safeMaxOutward * 0.85f)
                        val start = Offset(
                            (center.x + baseRadius * cos(angle)).toFloat(),
                            (center.y + baseRadius * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (baseRadius + spikeLen) * cos(angle)).toFloat(),
                            (center.y + (baseRadius + spikeLen) * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = if (i % 2 == 0) neonGold else neonPurple,
                            start = start,
                            end = end,
                            strokeWidth = if (amp > 0.4f) 3.5f else 1.5f
                        )
                    }
                }

                // 9. Fractal Flower: 12-petaled floral lotus blooming with music
                CircularVisualizerType.NEON_FRACTAL_FLOWER -> {
                    val petals = 12
                    val path = Path()
                    for (i in 0..360) {
                        val angle = Math.toRadians(i.toDouble())
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val r = baseRadius + 6f + (sin(angle * petals).toFloat() * (8f + amp * safeMaxOutward * 0.6f))
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonPurple, style = Stroke(width = 2.5f))
                }

                // 10. Singularity Vortex: Counter-rotating black hole accretion vortex
                CircularVisualizerType.NEON_SINGULARITY_VORTEX -> {
                    val path = Path()
                    for (i in 0 until pointsCount) {
                        val angle = (2 * PI * i / pointsCount) - Math.toRadians(rotationAngle.toDouble() * 1.5)
                        val amp = fftData.getOrElse(i) { 0.2f }
                        val r = baseRadius + 4f + ((1f - amp) * safeMaxOutward * 0.7f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonPink, style = Stroke(width = 3f))
                }

                // 11. Quantum Flux: Disconnected lightning flux lines jumping with bass
                CircularVisualizerType.NEON_QUANTUM_FLUX -> {
                    for (i in 0 until pointsCount step 3) {
                        val angle = (2 * PI * i / pointsCount)
                        val amp = fftData.getOrElse(i) { 0.2f }
                        val offsetDist = amp * safeMaxOutward * 0.85f
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

                // 12. DNA Helix Pulse: Double helix with connecting base-pair rungs
                CircularVisualizerType.NEON_DNA_HELIX_PULSE -> {
                    val path1 = Path()
                    val path2 = Path()
                    val points1 = mutableListOf<Offset>()
                    val points2 = mutableListOf<Offset>()

                    for (i in 0..80) {
                        val angle = (4 * PI * i / 80) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val r1 = baseRadius + 8f + (amp * safeMaxOutward * 0.6f)
                        val r2 = baseRadius + 8f - (amp * safeMaxOutward * 0.25f)
                        val p1 = Offset(
                            (center.x + r1 * cos(angle)).toFloat(),
                            (center.y + r1 * sin(angle)).toFloat()
                        )
                        val p2 = Offset(
                            (center.x + r2 * cos(angle + PI)).toFloat(),
                            (center.y + r2 * sin(angle + PI)).toFloat()
                        )
                        points1.add(p1)
                        points2.add(p2)

                        if (i == 0) {
                            path1.moveTo(p1.x, p1.y)
                            path2.moveTo(p2.x, p2.y)
                        } else {
                            path1.lineTo(p1.x, p1.y)
                            path2.lineTo(p2.x, p2.y)
                        }
                    }
                    drawPath(path1, neonCyan, style = Stroke(width = 2f))
                    drawPath(path2, neonPink, style = Stroke(width = 2f))

                    // Draw connecting DNA base-pair rungs
                    for (i in points1.indices step 4) {
                        if (i < points2.size) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = points1[i],
                                end = points2[i],
                                strokeWidth = 1f
                            )
                        }
                    }
                }

                // 13. Hyperspace Tunnel: 3D perspective tunnel depth frames zooming outward
                CircularVisualizerType.NEON_HYPERSPACE_TUNNEL -> {
                    val avgAmp = if (fftData.isNotEmpty()) fftData.average().toFloat().takeIf { !it.isNaN() } ?: 0.2f else 0.2f
                    for (r in 1..4) {
                        val side = (baseRadius + (r * (safeMaxOutward / 4.5f)) + (avgAmp * safeMaxOutward * 0.35f)) * 1.4f
                        val topLeft = Offset(center.x - side / 2, center.y - side / 2)
                        drawRect(
                            color = if (r % 2 == 0) neonCyan.copy(alpha = 0.8f) else neonPurple.copy(alpha = 0.8f),
                            topLeft = topLeft,
                            size = Size(side, side),
                            style = Stroke(width = (4 - r).toFloat().coerceAtLeast(1.2f))
                        )
                    }
                }

                // 14. Neural Synapse: Interconnected neural network nodes with firing arcs
                CircularVisualizerType.NEON_NEURAL_SYNAPSE -> {
                    val points = mutableListOf<Offset>()
                    for (i in 0 until 16) {
                        val angle = (2 * PI * i / 16) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i * 4) { 0.2f }
                        val r = baseRadius + 8f + (amp * safeMaxOutward * 0.75f)
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
                        drawCircle(neonCyan, radius = 3f, center = points[i])
                    }
                }

                // 15. Plasma Storm: Chaotic electrical sparks and plasma tendrils
                CircularVisualizerType.NEON_PLASMA_STORM -> {
                    val path = Path()
                    for (i in 0..180) {
                        val angle = Math.toRadians(i * 2.0) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val noise = sin(i.toDouble() * 0.5 + rotationAngle.toDouble() * 0.1).toFloat()
                        val r = baseRadius + 6f + (amp * abs(noise) * safeMaxOutward * 0.8f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonGreen, style = Stroke(width = 1.5f))
                }

                // 16. Supernova Burst: Glowing radial supernova aura + radial rays
                CircularVisualizerType.NEON_SUPERNOVA_BURST -> {
                    val avgAmp = if (fftData.isNotEmpty()) fftData.average().toFloat().takeIf { !it.isNaN() } ?: 0.2f else 0.2f
                    val radius = baseRadius + (avgAmp * safeMaxOutward * 0.85f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(neonCyan.copy(alpha = (avgAmp * 0.8f).coerceIn(0.1f, 1f)), Color.Transparent),
                            center = center,
                            radius = radius.coerceAtLeast(1f)
                        ),
                        radius = radius,
                        center = center
                    )
                }

                // 17. Cyber Matrix Grid: Stacked digital LED block equalizer bars in matrix green
                CircularVisualizerType.NEON_CYBER_MATRIX_GRID -> {
                    val barCount = 16
                    for (i in 0 until barCount) {
                        val angle = (2 * PI * i / barCount) + Math.toRadians(rotationAngle.toDouble())
                        val amp = fftData.getOrElse(i * 3) { 0.2f }
                        val blockCount = (1 + (amp * 6).toInt()).coerceIn(1, 6)
                        for (b in 0 until blockCount) {
                            val r = baseRadius + 4f + (b * 7f)
                            val x = (center.x + r * cos(angle)).toFloat()
                            val y = (center.y + r * sin(angle)).toFloat()
                            drawCircle(neonGreen, radius = 2.5f, center = Offset(x, y))
                        }
                    }
                }

                // 18. Gravitational Ring: Curved gravitational lensing ring distortion in blue and gold
                CircularVisualizerType.NEON_GRAVITATIONAL_RING -> {
                    val path = Path()
                    for (i in 0..pointsCount) {
                        val angle = (2 * PI * i / pointsCount)
                        val amp = fftData.getOrElse(i % pointsCount) { 0.2f }
                        val r = baseRadius + 6f + (sin(angle * 3 + Math.toRadians(rotationAngle.toDouble())) * amp * safeMaxOutward * 0.6f)
                        val x = (center.x + r * cos(angle)).toFloat()
                        val y = (center.y + r * sin(angle)).toFloat()
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, neonBlue, style = Stroke(width = 3.5f))
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
            contentAlignment = Alignment.Center
        ) {
            val isAlbumArtworkEnabled = LocalAlbumArtworkEnabled.current
            if (isAlbumArtworkEnabled && albumArtBytes != null) {
                AsyncImage(
                    model = albumArtBytes,
                    contentDescription = "Album Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = com.sftwr.cleandevelopment.musicplayer.home.R.drawable.ic_default_album_art),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
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
                        colors = listOf(neonCyan, neonBlue, neonPurple, neonCyan)
                    ),
                    shape = CircleShape
                )
        )
    }
}
