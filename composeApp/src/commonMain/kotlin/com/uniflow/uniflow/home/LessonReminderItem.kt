package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun LessonReminderItem(
    reminder: LessonReminderUi,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val reminderType = ReminderType.fromDb(reminder.reminderType)
    val visual = reminderTypeVisual(reminderType)
    val contentAlpha = if (reminder.isEnabled) 1f else 0.55f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = UniFlowTheme.colors.chipBackground,
                shape = RoundedCornerShape(14.dp)
            )
            .border(1.dp, UniFlowTheme.colors.glassBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(visual.accent.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.accent
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminderType.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = UniFlowTheme.colors.textSecondary.copy(alpha = contentAlpha)
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = UniFlowTheme.colors.textPrimary.copy(alpha = contentAlpha)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = formatReminderTriggerAt(reminder.triggerAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = UniFlowTheme.colors.textSecondary.copy(alpha = contentAlpha)
                )

                if (!reminder.isEnabled) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Elvégezve",
                        style = MaterialTheme.typography.labelMedium,
                        color = visual.accent
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Szerkesztés",
                        tint = UniFlowTheme.colors.textSecondary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Törlés",
                        tint = UniFlowTheme.colors.danger
                    )
                }
            }
        }
    }
}
