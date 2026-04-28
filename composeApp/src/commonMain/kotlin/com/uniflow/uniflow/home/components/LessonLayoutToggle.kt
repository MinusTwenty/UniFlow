package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun LessonLayoutToggle(
    selectedMode: LessonLayoutMode,
    onModeChange: (LessonLayoutMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape
            )
            .height(40.dp)
    ) {
        ToggleItem(
            selected = selectedMode == LessonLayoutMode.HORIZONTAL,
            onClick = { onModeChange(LessonLayoutMode.HORIZONTAL) },
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ViewCarousel,
                contentDescription = "Vízszintes nézet",
                tint = if (selectedMode == LessonLayoutMode.HORIZONTAL)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.primary
            )
        }

        ToggleItem(
            selected = selectedMode == LessonLayoutMode.VERTICAL,
            onClick = { onModeChange(LessonLayoutMode.VERTICAL) },
            shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ViewAgenda,
                contentDescription = "Függőleges nézet",
                tint = if (selectedMode == LessonLayoutMode.VERTICAL)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ToggleItem(
    selected: Boolean,
    onClick: () -> Unit,
    shape: RoundedCornerShape,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = shape
            )
            .clickable(onClick = onClick)
            .width(52.dp)
            .height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
