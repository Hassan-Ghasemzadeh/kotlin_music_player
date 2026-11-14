package com.sftwr.cleandevelopment.musicplayer.library.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sftwr.cleandevelopment.musicplayer.designsystem.component.LocalColorPalette

/**
 * Custom modifier to draw a gradient neon border around any Composable component,
 * dynamically matching the active [LocalColorPalette].
 */
@Composable
fun Modifier.neonBorder(
    borderWidth: Dp = 1.5.dp,
    shape: Shape,
    color: Color = LocalColorPalette.current.accent,
    brush: Brush = Brush.horizontalGradient(
        colors = listOf(
            color, color.copy(alpha = 0.5f)
        )
    )
): Modifier = this.drawWithContent {
    drawContent()
    // Create the outline based on the provided shape
    val outline = shape.createOutline(size, layoutDirection, this)
    // Draw the gradient stroke over the content
    drawOutline(
        outline = outline, brush = brush, style = Stroke(width = borderWidth.toPx())
    )
}