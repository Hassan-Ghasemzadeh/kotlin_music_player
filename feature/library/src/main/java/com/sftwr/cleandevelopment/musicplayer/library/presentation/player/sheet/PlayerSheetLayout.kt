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

    val maxOffsetPx = (screenHeightPx - miniBarHeightPx).coerceAtLeast(1f)
    val progress = sheetState.progress

    // تعریف مشترک درگ برای اینکه هم روی مینی‌پلیر و هم صفحه کامل به درستی کار کند
    val velocityTracker = remember { VelocityTracker() }
    val dragGestureModifier = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragStart = { velocityTracker.resetTracking() },
            onDragEnd = {
                val velocity = velocityTracker.calculateVelocity().y
                scope.launch { sheetState.settle(velocity) }
            },
            onDragCancel = {
                scope.launch { sheetState.settle(0f) }
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

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Base Main App Content
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        // 2. Dimmed Scrim Overlay
        if (progress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (progress * 0.85f).coerceIn(0f, 0.85f)
                    }
                    .background(colorPalette.surfaceCard)
                    .clickable(enabled = progress > 0.3f) {
                        scope.launch { sheetState.collapse() }
                    }
            )
        }

        if (hasTrack) {
            // 3. Expanded Full-Screen Sheet sliding container (بدون درگ اضافه در فضای خالی)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val sheetOffsetY = ((1f - progress) * maxOffsetPx).roundToInt()
                        IntOffset(0, sheetOffsetY)
                    }
                    .graphicsLayer {
                        val cornerRadiusPx = with(density) { (16.dp * (1f - progress)).toPx() }
                        shadowElevation = with(density) { (16.dp * progress).toPx() }
                        clip = progress < 0.99f
                        shape = RoundedCornerShape(
                            topStart = cornerRadiusPx,
                            topEnd = cornerRadiusPx
                        )
                    }
                    .background(
                        if (progress > 0.05f) {
                            colorPalette.primaryBackground.copy(alpha = progress.coerceIn(0f, 1f))
                        } else {
                            Color.Transparent
                        }
                    )
            ) {
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
            }

            // 4. Collapsed Mini-Player Bar (تنها مرجع درگ و کلیک برای حالت بسته)
            if (progress < 0.99f) {
                val isTallDevice = screenHeightPx > 2400f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            alpha = (1f - progress * 1.5f).coerceIn(0f, 1f)
                        }
                        .then(
                            if (isTallDevice) Modifier.navigationBarsPadding()
                            else Modifier.padding(vertical = 45.dp)
                        )
                        .padding(horizontal = 8.dp)
                        .then(dragGestureModifier)
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