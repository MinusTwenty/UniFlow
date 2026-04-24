package com.uniflow.uniflow.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun LessonDetailsDialog(
    lesson: LessonCard,
    notes: List<LessonNoteUi>,
    reminders: List<LessonReminderUi>,
    onDismiss: () -> Unit,
    onAddNote: () -> Unit,
    onAddReminder: () -> Unit,
    onAddFile: () -> Unit,
    onEditNote: (LessonNoteUi) -> Unit,
    onOpenReminder: (LessonReminderUi) -> Unit,
    onEditReminder: (LessonReminderUi) -> Unit,
    onDeleteNote: (LessonNoteUi) -> Unit,
    onDeleteReminder: (LessonReminderUi) -> Unit
) {
    var quickMenuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth()) {
            UniFlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(18.dp)
                            .padding(bottom = 56.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lesson.code,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = UniFlowTheme.colors.textPrimary
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = UniFlowTheme.colors.textSecondary
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

                        Spacer(Modifier.height(12.dp))

                        DetailRow("Idő", lesson.time)
                        DetailRow("Oktatás módja", lesson.lessonType)
                        DetailRow("Rendszeresség", lesson.weekType)
                        DetailRow("Helyiség", lesson.room)
                        DetailRow("Épület", lesson.building)
                        DetailRow("Oktató", lesson.teacher)
                        DetailRow("Kreditek száma", lesson.credits.toString())

                        if (!lesson.note.isNullOrBlank()) {
                            Spacer(Modifier.height(12.dp))
                            ExpandableSection(title = "Órai megjegyzés", initiallyExpanded = true) {
                                Text(
                                    text = lesson.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = UniFlowTheme.colors.textPrimary
                                )
                            }
                        }

                        if (reminders.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            ExpandableSection(title = "Emlékeztetők", initiallyExpanded = true) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    reminders.forEach { reminder ->
                                        LessonReminderItem(
                                            reminder = reminder,
                                            onOpen = { onOpenReminder(reminder) },
                                            onEdit = { onEditReminder(reminder) },
                                            onDelete = { onDeleteReminder(reminder) }
                                        )
                                    }
                                }
                            }
                        }

                        if (notes.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            ExpandableSection(title = "Jegyzetek", initiallyExpanded = true) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    notes.forEach { note ->
                                        LessonNoteItem(
                                            note = note,
                                            createdAtLabel = formatNoteCreatedAt(note.createdAt),
                                            onEdit = { onEditNote(note) },
                                            onDelete = { onDeleteNote(note) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LessonQuickMenuCard(
                        visible = quickMenuExpanded,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 60.dp),
                        onAddNote = {
                            quickMenuExpanded = false
                            onAddNote()
                        },
                        onAddReminder = {
                            quickMenuExpanded = false
                            onAddReminder()
                        },
                        onAddFile = {
                            quickMenuExpanded = false
                            onAddFile()
                        }
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 14.dp, bottom = 14.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { quickMenuExpanded = !quickMenuExpanded },
                        shape = CircleShape,
                        color = UniFlowTheme.colors.accent.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, UniFlowTheme.colors.glassBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Gyors hozzáadás",
                                tint = UniFlowTheme.colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = UniFlowTheme.colors.textSecondary
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = UniFlowTheme.colors.textPrimary
        )
    }
}
