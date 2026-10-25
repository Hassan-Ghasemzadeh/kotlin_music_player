package com.sftwr.cleandevelopment.musicplayer.library.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * A compact floating action button that appears during active list scrolling
 * and smoothly fades out after scrolling stops.
 *
 * @param isScrolling Indicates whether the LazyList is currently being scrolled by the user.
 * @param visibleOnlyWhenScrolled Determines if the button visibility strictly mirrors scroll activity.
 * @param onClick Callback invoked when the button is clicked to scroll to the top.
 * @param modifier Modifier for positioning or styling.
 */
@Composable
fun ScrollToTopButton(
    isScrolling: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local state to manage delayed fading when scrolling stops
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isScrolling) {
        if (isScrolling) {
            // Show the button immediately when scrolling starts
            isVisible = true
        } else {
            // Wait for 1.5 seconds after scrolling stops, then smoothly hide the button
            delay(1500L.milliseconds)
            isVisible = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)),
        exit = fadeOut(animationSpec = tween(durationMillis = 600)), // Slow fade-out when stopped
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(36.dp) // Compact size matching requirements
                .shadow(elevation = 6.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF8BE9FD)) // Neon cyan accent color
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Scroll to top",
                tint = Color(0xFF0F0B21), // Dark contrast icon
                modifier = Modifier.size(22.dp)
            )
        }
    }
}