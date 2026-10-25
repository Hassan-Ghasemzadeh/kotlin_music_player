package com.sftwr.cleandevelopment.musicplayer.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A customizable Alphabet Indexer Bar component that allows users to quickly
 * navigate through lists by dragging across letters with smooth touch gesture handling.
 */
@Composable
fun AlphabetIndexerBar(
    letters: List<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    neonColor: Color = Color(0xFF00D2FF),
    glassColor: Color = Color.White.copy(alpha = 0.05f)
) {
    // State tracking the currently active letter during touch interaction
    var selectedLetter by remember { mutableStateOf<String?>(null) }

    // State tracking whether the user is actively dragging along the bar
    var isDragging by remember { mutableStateOf(false) }

    // Total measured height of the indexer bar container
    var totalHeight by remember { mutableStateOf(0f) }

    /**
     * Helper function to map vertical touch coordinates (Y position) to
     * the corresponding alphabet letter index safely.
     */
    fun updateSelectedLetter(posY: Float) {
        if (totalHeight > 0f && letters.isNotEmpty()) {
            val clampedY = posY.coerceIn(0f, totalHeight)
            val index = ((clampedY / totalHeight) * letters.size)
                .toInt()
                .coerceIn(0, letters.size - 1)

            val letter = letters[index]
            if (selectedLetter != letter) {
                selectedLetter = letter
                onLetterSelected(letter)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Floating bubble indicator showing the currently selected letter popup
        AnimatedVisibility(
            visible = isDragging && selectedLetter != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-60).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(neonColor.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLetter ?: "",
                    color = Color.Black,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Main indexer container with drag gesture detection
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(glassColor)
                .onGloballyPositioned { coordinates ->
                    totalHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(letters, totalHeight) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            updateSelectedLetter(offset.y)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            updateSelectedLetter(change.position.y)
                        },
                        onDragEnd = {
                            isDragging = false
                            selectedLetter = null
                        },
                        onDragCancel = {
                            isDragging = false
                            selectedLetter = null
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Column displaying all indexer letters vertically distributed
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                letters.forEach { letter ->
                    val isSelected = letter == selectedLetter
                    Text(
                        text = letter,
                        color = if (isSelected) neonColor else Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}