package com.uniflow.uniflow.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GlassChip(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = UniFlowTheme.colors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = colors.chipBackground,
        border = BorderStroke(1.dp, colors.glassBorder),
        shadowElevation = 2.dp
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = colors.chipText
            )
        }
    }
}