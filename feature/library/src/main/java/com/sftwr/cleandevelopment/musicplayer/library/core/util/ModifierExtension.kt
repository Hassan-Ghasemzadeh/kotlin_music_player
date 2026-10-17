package com.sftwr.cleandevelopment.musicplayer.library.core.util


import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.neonBorder(
    borderWidth: Dp = 1.5.dp, shape: Shape, brush: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF00D2FF), Color(0xFF0072FF)
        )
    )
): Modifier = this.drawWithContent {
    drawContent()
    val outline = shape.createOutline(size, layoutDirection, this)
    drawOutline(
        outline = outline, brush = brush, style = Stroke(width = borderWidth.toPx())
    )
}