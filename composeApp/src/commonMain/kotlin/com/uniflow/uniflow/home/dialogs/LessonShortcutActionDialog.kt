package com.uniflow.uniflow.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun LessonShortcutActionDialog(
    lesson: LessonCard,
    onDismiss: () -> Unit,
    onAddNote: () -> Unit,
    onAddReminder: () -> Unit,
    onAddFile: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        UniFlowGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Gyors hozzáadás",
                    style = MaterialTheme.typography.headlineSmall,
                    color = UniFlowTheme.colors.textPrimary
                )

                Text(
                    text = "${lesson.code} • ${lesson.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UniFlowTheme.colors.textSecondary
                )

                Spacer(Modifier.height(8.dp))

                ShortcutItem(
                    text = "Jegyzet",
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                            contentDescription = null,
                            tint = UniFlowTheme.colors.textPrimary
                        )
                    },
                    onClick = onAddNote
                )

                ShortcutItem(
                    text = "Emlékeztető",
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AlarmAdd,
                            contentDescription = null,
                            tint = UniFlowTheme.colors.textPrimary
                        )
                    },
                    onClick = onAddReminder
                )

                ShortcutItem(
                    text = "Fájl",
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = null,
                            tint = UniFlowTheme.colors.textPrimary
                        )
                    },
                    onClick = onAddFile
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mégse")
                }
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        icon()

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = UniFlowTheme.colors.textPrimary
        )
    }
}
