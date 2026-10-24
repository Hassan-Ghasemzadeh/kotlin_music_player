package com.sftwr.cleandevelopment.musicplayer.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest

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

    // استفاده از StateFlow برای پردازش لحظه‌ای و بدون تاخیرِ تاچ بدون بلاک کردن UI Thread
    val dragPositionFlow = remember { MutableStateFlow(0f) }

    LaunchedEffect(letters, totalHeight) {
        dragPositionFlow.collectLatest { posY ->
            val index = ((posY / totalHeight) * letters.size)
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
        // حباب شناور
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

        // ستون ایندکسر با قابلیت تشخیص سریع تاچ و درگ
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(glassColor)
                .onGloballyPositioned { coordinates ->
                    totalHeight = coordinates.size.height.toFloat()
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragPositionFlow.value = offset.y
                        },
                        onDragEnd = {
                            isDragging = false
                            selectedLetter = null
                        },
                        onDragCancel = {
                            isDragging = false
                            selectedLetter = null
                        },
                        onDrag = { change, _ ->
                            dragPositionFlow.value = change.position.y
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            isDragging = true
                            dragPositionFlow.value = offset.y
                            if (tryAwaitRelease()) {
                                isDragging = false
                                selectedLetter = null
                            } else {
                                isDragging = false
                                selectedLetter = null
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
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