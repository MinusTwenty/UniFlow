package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

@OptIn(kotlin.time.ExperimentalTime::class)
@Composable
fun ReminderDetailsDialog(
    reminder: LessonReminderUi,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    val reminderType = ReminderType.fromDb(reminder.reminderType)
    val visual = reminderTypeVisual(reminderType)
    val dateTime = Instant.fromEpochMilliseconds(reminder.triggerAt)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val monthNamesLowercase = listOf(
        "január", "február", "március", "április", "május", "június",
        "július", "augusztus", "szeptember", "október", "november", "december"
    )
    val dateLabel =
        "${dateTime.date.year}. ${monthNamesLowercase[dateTime.date.month.number - 1]} ${dateTime.date.day}."
    val timeLabel = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"

    Dialog(onDismissRequest = onDismiss) {
        UniFlowGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(visual.accent.copy(alpha = 0.16f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = visual.icon,
                                contentDescription = null,
                                tint = visual.accent
                            )
                        }

                        Column {
                            Text(
                                text = reminderType.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = UniFlowTheme.colors.textSecondary
                            )

                            if (!reminder.isEnabled) {
                                Text(
                                    text = "Elvégezve",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = visual.accent
                                )
                            }
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

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Bezárás",
                                tint = UniFlowTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = UniFlowTheme.colors.textPrimary
                )

                if (!reminder.description.isNullOrBlank()) {
                    Spacer(Modifier.height(14.dp))
                    DetailField("Leírás", reminder.description)
                }

                Spacer(Modifier.height(14.dp))
                DetailField("Dátum", dateLabel)
                Spacer(Modifier.height(12.dp))
                DetailField("Időpont", timeLabel)
                Spacer(Modifier.height(12.dp))
                DetailField("Emlékeztető megjelenítése", if (reminder.isEnabled) "Aktív" else "Elvégezve")

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onToggleCompleted,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(if (reminder.isEnabled) "Megjelölés elvégzettnek" else "Visszaállítás aktívra")
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = UniFlowTheme.colors.danger
                    )
                    Spacer(Modifier.size(8.dp))
                    Text("Törlés", color = UniFlowTheme.colors.danger)
                }
            }
        }
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UniFlowTheme.colors.chipBackground, RoundedCornerShape(16.dp))
            .border(1.dp, UniFlowTheme.colors.glassBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = UniFlowTheme.colors.textSecondary
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = UniFlowTheme.colors.textPrimary
        )
    }
}
