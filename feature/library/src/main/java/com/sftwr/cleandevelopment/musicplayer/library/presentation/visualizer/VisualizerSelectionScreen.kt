package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.GlassCard
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.NeonBlue
import com.sftwr.cleandevelopment.musicplayer.home.R
import com.sftwr.cleandevelopment.musicplayer.library.core.util.neonBorder
import com.sftwr.cleandevelopment.musicplayer.model.VisualizerStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

/**
 * Full-screen Visualizer Selection Screen featuring a card-carousel (HorizontalPager with scaling
 * and alpha transformations), glowing neon highlight borders, live animated previews, and a prominent
 * Apply action button.
 */
@Composable
fun VisualizerSelectionRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VisualizerViewModel = hiltViewModel()
) {
    val savedStyle by viewModel.savedVisualizer.collectAsStateWithLifecycle()
    val selectedIndex by viewModel.selectedIndex.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    VisualizerSelectionScreen(
        availableStyles = viewModel.availableStyles,
        savedStyle = savedStyle,
        initialIndex = selectedIndex,
        onBackClick = onBackClick,
        onApplyStyle = { style ->
            viewModel.applySelection(style) {
                onBackClick()
            }
        },
        modifier = modifier
    )
}

@Composable
fun VisualizerSelectionScreen(
    availableStyles: List<VisualizerStyle>,
    savedStyle: VisualizerStyle,
    initialIndex: Int,
    onBackClick: () -> Unit,
    onApplyStyle: (VisualizerStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (availableStyles.size - 1).coerceAtLeast(0)),
        pageCount = { availableStyles.size }
    )

    // Generate live synthetic FFT frequencies for active visualizer preview animation
    val animatedFft by produceState<FloatArray>(
        initialValue = FloatArray(32) { 0.2f }
    ) {
        var step = 0f
        while (true) {
            step += 0.15f
            value = FloatArray(32) { index ->
                (0.15f + 0.45f * abs(sin(step + index * 0.4f))).coerceIn(0.1f, 0.95f)
            }
            delay(50)
        }
    }

    val darkGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0B21),
            Color(0xFF1B1238),
            Color(0xFF2C1454)
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(darkGradient)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.visualizer_style),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle Instruction
                Text(
                    text = "Swipe to preview visualizer styles",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Carousel Horizontal Pager
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 48.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val style = availableStyles.getOrNull(page) ?: return@HorizontalPager
                    val isCentered = pagerState.currentPage == page
                    val isCurrentlyApplied = style == savedStyle

                    // Page distance transformation math
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).let { abs(it) }

                    val cardScale = lerp(
                        start = 0.82f,
                        stop = 1.0f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    val cardAlpha = lerp(
                        start = 0.55f,
                        stop = 1.0f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )

                    val cardShape = RoundedCornerShape(28.dp)
                    val glowingNeonCyan = Color(0xFF91D8FB)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = cardScale
                                scaleY = cardScale
                                alpha = cardAlpha
                            }
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(460.dp)
                                .clip(cardShape)
                                .background(
                                    if (isCentered) {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF1E173E),
                                                Color(0xFF120C28)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.08f),
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    }
                                )
                                .then(
                                    if (isCentered) {
                                        Modifier.border(
                                            width = 2.dp,
                                            brush = Brush.linearGradient(
                                                listOf(
                                                    glowingNeonCyan,
                                                    Color(0xFF0072FF),
                                                    glowingNeonCyan
                                                )
                                            ),
                                            shape = cardShape
                                        )
                                    } else {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = cardShape
                                        )
                                    }
                                )
                                .padding(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top Badge: Currently Active Indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (isCurrentlyApplied) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NeonBlue.copy(alpha = 0.2f))
                                                .border(
                                                    1.dp,
                                                    NeonBlue,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = NeonBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "CURRENTLY ACTIVE",
                                                    color = NeonBlue,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }

                                // Live Visualizer Preview Container
                                Box(
                                    modifier = Modifier
                                        .size(210.dp)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    NeonCircularVisualizer(
                                        type = style.type,
                                        fftData = animatedFft,
                                        albumArtUrl = null,
                                        isPlaying = isCentered,
                                        size = 200.dp
                                    )
                                }

                                // Style Name & Description Text
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = style.title,
                                        color = if (isCentered) glowingNeonCyan else Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = style.description,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp,
                                        maxLines = 2
                                    )
                                }

                                // Pager Page Counter Badge (e.g. 1 / 18)
                                Text(
                                    text = "${page + 1} / ${availableStyles.size}",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Apply Button
                val selectedStyle = availableStyles.getOrNull(pagerState.currentPage)
                    ?: VisualizerStyle.DEFAULT

                val applyButtonGradient = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00D2FF),
                        Color(0xFF0055FF)
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { onApplyStyle(selectedStyle) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(applyButtonGradient),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp),
                        contentPadding = PaddingValues()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Apply Visualizer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
