package com.sftwr.cleandevelopment.musicplayer.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * A production-ready, customizable Alphabet Indexer Bar component that enables users
 * to fast-scroll through sorted lists via touch down or vertical drag gestures.
 *
 * Dynamically supports LTR and RTL layout directions with edge offsets to ensure
 * the bar sits perfectly centered in the margin between the screen border and list cards.
 */
@Composable
fun AlphabetIndexerBar(
    letters: List<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    neonColor: Color = LocalColorPalette.current.accent,
    glassColor: Color = Color.White.copy(alpha = 0.08f)
) {
    // Detect active layout direction
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    // Remember updated state references to prevent pointer input cancellation on recomposition
    val currentLetters by rememberUpdatedState(letters)
    val currentOnLetterSelected by rememberUpdatedState(onLetterSelected)

    // Touch interaction states
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var totalHeight by remember { mutableFloatStateOf(0f) }

    /**
     * Safely maps vertical touch position (posY) to the corresponding letter in the list.
     */
    fun updateSelectedLetter(posY: Float) {
        if (totalHeight > 0f && currentLetters.isNotEmpty()) {
            val clampedY = posY.coerceIn(0f, totalHeight)
            val fraction = (clampedY / totalHeight).coerceIn(0f, 0.999f)
            val index = (fraction * currentLetters.size).toInt().coerceIn(0, currentLetters.size - 1)

            val letter = currentLetters[index]
            if (selectedLetter != letter) {
                selectedLetter = letter
                currentOnLetterSelected(letter)
            }
        }
    }

    // Offset to position the bar perfectly centered in the margin between the screen edge and track cards
    // In RTL (bar on left screen edge): -9.dp absoluteOffset places bar right in the visual center
    // In LTR (bar on right screen edge): +9.dp absoluteOffset places bar right in the visual center
    val barEdgeOffset = if (isRtl) (-9).dp else 9.dp

    // Absolute bubble horizontal offset relative to the fixed container:
    // In RTL (bar on left screen edge): bubble aligns CenterEnd and absoluteOffsets +36.dp (physical RIGHT over song cards)
    // In LTR (bar on right screen edge): bubble aligns CenterStart and absoluteOffsets -52.dp (physical LEFT over song cards)
    val bubbleAlignment = if (isRtl) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleXOffset = if (isRtl) 36.dp else (-52).dp

    Box(
        modifier = modifier
            .fillMaxHeight()
            .absoluteOffset(x = barEdgeOffset)
            .width(26.dp)
            .padding(vertical = 4.dp)
            .zIndex(10f),
        contentAlignment = Alignment.Center
    ) {
        // 1. Fast Scroller Interactive Track Container (Fixed width 22.dp)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(glassColor)
                .onGloballyPositioned { coordinates ->
                    totalHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        isDragging = true
                        updateSelectedLetter(down.position.y)

                        drag(down.id) { change ->
                            change.consume()
                            updateSelectedLetter(change.position.y)
                        }

                        isDragging = false
                        selectedLetter = null
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Inner Column rendering letters with equal weight distribution
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                letters.forEach { letter ->
                    val isSelected = letter == selectedLetter
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            color = if (isSelected) neonColor else Color.White.copy(alpha = 0.7f),
                            fontSize = if (letters.size > 25) 9.sp else 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // 2. Floating Letter Selection Popup Indicator (Positioned independently with absoluteOffset)
        AnimatedVisibility(
            visible = isDragging && selectedLetter != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(bubbleAlignment)
                .absoluteOffset(x = bubbleXOffset)
                .zIndex(20f)
        ) {
            Box(
                modifier = Modifier
                    .requiredSize(50.dp)
                    .clip(CircleShape)
                    .background(neonColor.copy(alpha = 0.95f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLetter ?: "",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
