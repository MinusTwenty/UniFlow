package com.uniflow.uniflow.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UniFlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val c = UniFlowTheme.colors

    val backgroundBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF1A2742),
            c.backgroundGradientTop,
            c.backgroundGradientBottom,
            Color(0xFF0A1020)
        ),
        center = Offset(500f, 250f),
        radius = 1200f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        content()
    }
}