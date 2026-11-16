package com.sftwr.cleandevelopment.musicplayer.library.presentation.player.sheet

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.BottomPlayerBar
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalColorPalette
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.sheet.PlayerSheetState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Clean Architecture transition coordinator layout providing high-performance,
 * GPU-accelerated Samsung Music-style expand/collapse transitions between the
 * collapsed mini-player bar and the full-screen Now Playing screen.
 *
 * Runs all animation calculations on the GPU via [Modifier.graphicsLayer] to maintain
 * rock-solid 60/120 FPS performance without layout recomposition overhead.
 */
@Composable
fun PlayerSheetLayout(
    sheetState: PlayerSheetState,
    hasTrack: Boolean,
    songTitle: String,
    artistName: String,
    isPlaying: Boolean,
    hasNext: Boolean,
    hasPrevious: Boolean,
    artUri: Bitmap?,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    fullPlayerContent: @Composable (sheetProgress: Float, onCollapse: () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    val colorPalette = LocalColorPalette.current

    // Intercept hardware/system back button when player sheet is expanded
    BackHandler(enabled = sheetState.progress > 0.05f) {
        scope.launch {
            sheetState.collapse()
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    val screenHeightPx = with(density) { screenHeightDp.toPx() }
    val miniBarHeightDp = 72.dp
    val miniBarHeightPx = with(density) { miniBarHeightDp.toPx() }

    // Total swipe distance from collapsed docked position to expanded full-screen top
    val maxOffsetPx = (screenHeightPx - miniBarHeightPx).coerceAtLeast(1f)
    val progress = sheetState.progress

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Base Main App Content
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        // 2. Dimmed Scrim Overlay (تغییر رنگ پس‌زمینه تاریک‌کننده به رنگ پالت دلخواه شما)
        if (progress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (progress * 0.85f).coerceIn(0f, 0.85f)
                    }
                    // استفاده از رنگ پالت (مثلا primaryBackground یا هر رنگ دلخواه دیگر از colorPalette)
                    .background(colorPalette.surfaceCard)
                    .clickable(enabled = progress > 0.3f) {
                        scope.launch { sheetState.collapse() }
                    }
            )
        }

        // 3. Persistent Sheet Container hosting Mini-Player and Full-Screen Player
        if (hasTrack) {
            val velocityTracker = remember { VelocityTracker() }

            val dragGestureModifier = Modifier.pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        velocityTracker.resetTracking()
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity().y
                        scope.launch {
                            sheetState.settle(velocity)
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            sheetState.settle(0f)
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        scope.launch {
                            sheetState.dispatchRawDelta(dragAmount, maxOffsetPx)
                        }
                    }
                )
            }

            // Expanded Full-Screen Sheet sliding container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val sheetOffsetY = ((1f - progress) * maxOffsetPx).roundToInt()
                        IntOffset(0, sheetOffsetY)
                    }
                    .graphicsLayer {
                        // Smoothly morph top corner radius from mini-bar (16dp) to full screen (0dp)
                        val cornerRadiusPx = with(density) { (16.dp * (1f - progress)).toPx() }
                        shadowElevation = with(density) { (16.dp * progress).toPx() }
                        clip = progress < 0.99f
                        shape = RoundedCornerShape(
                            topStart = cornerRadiusPx,
                            topEnd = cornerRadiusPx
                        )
                    }
                    // پس‌زمینه فقط هنگام باز شدن صفحه اعمال می‌شود تا مینی‌پلیر شفاف بماند
                    .background(
                        if (progress > 0.05f) {
                            colorPalette.primaryBackground.copy(alpha = progress.coerceIn(0f, 1f))
                        } else {
                            Color.Transparent
                        }
                    )
                    .then(dragGestureModifier)
            ) {
                // Full Screen Player Content (fades in smoothly as sheet expands)
                if (progress > 0.05f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = ((progress - 0.15f) / 0.85f).coerceIn(0f, 1f)
                            }
                    ) {
                        fullPlayerContent(progress) {
                            scope.launch { sheetState.collapse() }
                        }
                    }
                }

                // Collapsed Mini-Player Bar (دارای پس‌زمینه اختصاصی، مات، خوانا و کاملاً هم‌اندازه خودِ مینی‌پلیر)
                if (progress < 0.99f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                alpha = (1f - progress * 1.5f).coerceIn(0f, 1f)
                            }
                            .padding(horizontal = 8.dp, vertical = 20.dp)
                    ) {
                        BottomPlayerBar(
                            hasNext = hasNext,
                            hasPrevious = hasPrevious,
                            artUri = artUri,
                            songTitle = songTitle,
                            artistName = artistName,
                            isPlaying = isPlaying,
                            onPlayPauseClick = onPlayPauseClick,
                            onNextClick = onNextClick,
                            onPreviousClick = onPreviousClick,
                            onPlayerClick = {
                                scope.launch { sheetState.expand() }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .background(
                                    color = colorPalette.surfaceCard,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            }
        }
    }
}