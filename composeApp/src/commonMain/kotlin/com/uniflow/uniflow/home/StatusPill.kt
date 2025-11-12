package com.uniflow.uniflow.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun StatusPill(status: LessonStatus, breakToNextMinutes: Int?) {
    val label = when (status) {
        LessonStatus.ACTIVE -> "Aktív"
        LessonStatus.UPCOMING -> {
            if (breakToNextMinutes != null) "Következő • szünet ${breakToNextMinutes}p"
            else "Következő"
        }
        LessonStatus.PAST -> "Lezajlott"
    }
    Surface(
        color = MaterialTheme.colorScheme.inversePrimary,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}