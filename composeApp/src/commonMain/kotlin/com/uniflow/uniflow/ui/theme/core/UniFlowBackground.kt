package com.uniflow.uniflow.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UniFlowBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val c = UniFlowTheme.colors

    val base = Brush.verticalGradient(
        colors = listOf(
            c.backgroundGradientTop,
            blend(c.backgroundGradientTop, c.backgroundGradientBottom, 0.38f),
            c.backgroundGradientBottom
        )
    )

    val softGlow = Brush.radialGradient(
        colors = listOf(
            c.accent.copy(alpha = 0.10f),
            Color.Transparent
        ),
        center = androidx.compose.ui.geometry.Offset(220f, 180f),
        radius = 900f
    )

    val vignette = Brush.radialGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.14f)
        ),
        center = androidx.compose.ui.geometry.Offset(600f, 900f),
        radius = 1400f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(base)
            .background(softGlow)
            .background(vignette)
    ) {
        content()
    }
}

private fun blend(a: Color, b: Color, ratio: Float): Color {
    val r = a.red + (b.red - a.red) * ratio
    val g = a.green + (b.green - a.green) * ratio
    val bl = a.blue + (b.blue - a.blue) * ratio
    val alpha = a.alpha + (b.alpha - a.alpha) * ratio
    return Color(r, g, bl, alpha)
}
