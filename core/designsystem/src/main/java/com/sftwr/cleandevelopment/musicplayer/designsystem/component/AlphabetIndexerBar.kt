package com.sftwr.cleandevelopment.musicplayer.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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

@Composable
fun AlphabetIndexerBar(
    letters: List<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    neonColor: Color = Color(0xFF00D2FF),
    glassColor: Color = Color.White.copy(alpha = 0.05f)
) {
    var selectedLetter by remember { mutableStateOf<String?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var totalHeight by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
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

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(glassColor)
                .onGloballyPositioned { coordinates ->
                    totalHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(letters, totalHeight) {
                    awaitPointerEventScope {
                        while (true) {
                            val pointerEvent = awaitPointerEvent()
                            val change = pointerEvent.changes.firstOrNull()

                            if (change != null) {
                                if (change.pressed) {
                                    if (!isDragging) isDragging = true

                                    updateLetter(
                                        posY = change.position.y,
                                        totalHeight = totalHeight,
                                        letters = letters
                                    ) { letter ->
                                        if (selectedLetter != letter) {
                                            selectedLetter = letter
                                            onLetterSelected(letter)
                                        }
                                    }
                                    change.consume()
                                } else {
                                    if (isDragging) {
                                        isDragging = false
                                        selectedLetter = null
                                    }
                                }
                            }
                        }
                    }
                },
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


private inline fun updateLetter(
    posY: Float,
    totalHeight: Float,
    letters: List<String>,
    onLetterChanged: (String) -> Unit
) {
    if (totalHeight <= 0f || letters.isEmpty()) return
    val index = ((posY / totalHeight) * letters.size)
        .toInt()
        .coerceIn(0, letters.size - 1)
    onLetterChanged(letters[index])
}