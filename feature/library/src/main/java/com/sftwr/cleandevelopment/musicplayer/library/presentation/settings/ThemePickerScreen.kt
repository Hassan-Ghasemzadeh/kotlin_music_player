package com.sftwr.cleandevelopment.musicplayer.library.presentation.settings

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
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.sftwr.cleandevelopment.musicplayer.domain.model.AppLanguage
import com.sftwr.cleandevelopment.musicplayer.domain.model.ColorPalette
import com.sftwr.cleandevelopment.musicplayer.home.R
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ThemePickerScreen(
    uiState: ThemePickerUiState,
    onBackClick: () -> Unit,
    onApplyPalette: (ColorPalette) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val palettes = ColorPalette.entries
    val initialPageIndex = palettes.indexOf(uiState.currentPalette).coerceAtLeast(0)

    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { palettes.size }
    )

    // Track currently selected palette in pager
    val currentSelectedPalette = palettes.getOrNull(pagerState.currentPage) ?: uiState.currentPalette
    val isAlreadyApplied = currentSelectedPalette == uiState.currentPalette

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(uiState.currentPalette.gradientBrush)
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

                Column {
                    Text(
                        text = stringResource(R.string.color_palette_theme),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Swipe to preview themes, tap Apply to update app",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Pager Carousel of 15 Color Palette Cards
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val palette = palettes[page]
                val isCentered = pagerState.currentPage == page
                val isSelectedInApp = palette == uiState.currentPalette
                val titleText = if (uiState.language == AppLanguage.PERSIAN) palette.nativeName else palette.displayName

                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).let { abs(it) }
                val cardScale = lerp(0.85f, 1.0f, 1f - pageOffset.coerceIn(0f, 1f))
                val cardAlpha = lerp(0.6f, 1.0f, 1f - pageOffset.coerceIn(0f, 1f))

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
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(520.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(palette.gradientBrush)
                            .border(
                                width = if (isSelectedInApp || isCentered) 2.5.dp else 1.dp,
                                color = if (isSelectedInApp) palette.accent else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(28.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Card Header: Theme Name & Accent Indicator / Checkmark
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(palette.accent)
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                    Text(
                                        text = titleText,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (isSelectedInApp) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(palette.accent)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Active",
                                            color = palette.primaryBackground,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Miniature Live Home Screen / Player UI Preview Inside Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 12.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(palette.primaryBackground.copy(alpha = 0.9f))
                                    .border(1.dp, palette.accent.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Mini App Bar
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.accent)
                                            )
                                            Text(
                                                text = "Music Player",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Search, null, tint = palette.accent, modifier = Modifier.size(12.dp))
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Settings, null, tint = palette.accent, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }

                                    // Mini Section / Content Cards
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Featured Playlist",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        // Mock Song Card 1
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(palette.surfaceCard)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(palette.accent.copy(alpha = 0.3f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.PlayArrow, null, tint = palette.accent, modifier = Modifier.size(16.dp))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Cybernetic Harmony", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                Text("Neon Pulse • 3:45", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, maxLines = 1)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.accent)
                                            )
                                        }

                                        // Mock Song Card 2
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(palette.secondaryBackground)
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.ColorLens, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Synthwave Odyssey", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                                Text("Retrowave • 4:12", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, maxLines = 1)
                                            }
                                        }
                                    }

                                    // Mini Player Bar at Bottom of Preview
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(palette.surfaceCard)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.accent),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.PlayArrow, null, tint = palette.primaryBackground, modifier = Modifier.size(14.dp))
                                            }
                                            Column {
                                                Text("Now Playing...", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                Text("Active Gradient Theme", color = palette.accent, fontSize = 8.sp, maxLines = 1)
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(palette.accent.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.accent)
                                            )
                                        }
                                    }
                                }
                            }

                            // Color swatches row at bottom of card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.primaryBackground).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Primary", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.secondaryBackground).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Secondary", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.surfaceCard).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Surface", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(palette.accent).border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Accent", color = palette.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Floating / Fixed Primary "Apply" Button at Bottom Center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        onApplyPalette(currentSelectedPalette)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentSelectedPalette.accent,
                        contentColor = currentSelectedPalette.primaryBackground
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = currentSelectedPalette.primaryBackground,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAlreadyApplied) "Theme Applied" else "Apply Palette",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
