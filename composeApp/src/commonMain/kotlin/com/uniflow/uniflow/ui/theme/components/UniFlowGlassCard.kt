package com.uniflow.uniflow.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun UniFlowGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = UniFlowTheme.colors
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(colors.glassSurface)
            .border(
                width = 1.dp,
                color = colors.glassBorder,
                shape = shape
            ),
        content = content
    )
}
