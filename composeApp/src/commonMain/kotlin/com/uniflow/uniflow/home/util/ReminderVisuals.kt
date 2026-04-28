package com.uniflow.uniflow.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.uniflow.uniflow.ui.theme.UniFlowTheme

data class ReminderTypeVisual(
    val icon: ImageVector,
    val accent: Color
)

@Composable
fun reminderTypeVisual(type: ReminderType): ReminderTypeVisual {
    return when (type) {
        ReminderType.GENERAL -> ReminderTypeVisual(
            icon = Icons.AutoMirrored.Filled.Notes,
            accent = UniFlowTheme.colors.accent
        )

        ReminderType.EXAM -> ReminderTypeVisual(
            icon = Icons.Filled.School,
            accent = Color(0xFFFF7A59)
        )

        ReminderType.ASSIGNMENT -> ReminderTypeVisual(
            icon = Icons.AutoMirrored.Filled.Assignment,
            accent = Color(0xFF10B981)
        )

        ReminderType.LESSON -> ReminderTypeVisual(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accent = Color(0xFFF59E0B)
        )
    }
}
