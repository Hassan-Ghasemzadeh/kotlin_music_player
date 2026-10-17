package com.sftwr.cleandevelopment.musicplayer.library.presentation.visualizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sftwr.cleandevelopment.musicplayer.library.core.util.neonBorder
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.AudioVisualizerManager
import com.sftwr.cleandevelopment.musicplayer.library.core.visualizer.CircularVisualizerType

@Composable
fun VisualizerSelectorScreen(
    audioVisualizerManager: AudioVisualizerManager,
    isPlaying: Boolean,
    onSelectType: (CircularVisualizerType) -> Unit
) {
    val fftData by audioVisualizerManager.fftData.collectAsState()
    val types = CircularVisualizerType.entries.toTypedArray()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0B21))
            .padding(12.dp)
    ) {
        Text(
            text = "SELECT VISUALIZER",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(types) { itemType ->
                val cardShape = RoundedCornerShape(16.dp)
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191336)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .neonBorder(shape = cardShape)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        NeonCircularVisualizer(
                            type = itemType,
                            fftData = fftData,
                            albumArtUrl = null,
                            isPlaying = isPlaying,
                            size = 110.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = itemType.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}