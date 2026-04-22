package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun TimetableViewToggle(
    selected: TimetableViewMode,
    onChange: (TimetableViewMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleButton(
            text = "Heti",
            selected = selected == TimetableViewMode.WEEKLY,
            onClick = { onChange(TimetableViewMode.WEEKLY) }
        )
        ToggleButton(
            text = "Napi",
            selected = selected == TimetableViewMode.DAILY,
            onClick = { onChange(TimetableViewMode.DAILY) }
        )
    }
}

@Composable
private fun ToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary
                else UniFlowTheme.colors.chipBackground,
                shape = shape
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else UniFlowTheme.colors.glassBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                UniFlowTheme.colors.chipText
            }
        )
    }
}