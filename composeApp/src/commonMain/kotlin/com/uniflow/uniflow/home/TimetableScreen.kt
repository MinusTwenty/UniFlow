package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniflow.database.UniFlowDatabase
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme

data class TimetableDayColumn(
    val dayOfWeek: Long,
    val title: String,
    val lessons: List<LessonCard>
)

@Composable
fun TimetableScreen(
    modifier: Modifier = Modifier,
    db: UniFlowDatabase,
    userId: Long?,
    activeTerm: AcademicTerm?,
    availableTerms: List<AcademicTerm>,
    activeTermId: Long?,
    onTermSelected: (Long) -> Unit,
    onSaveNote: (LessonCard, String) -> Unit,
    notesForLesson: (LessonCard) -> List<LessonNoteUi>,
    onDeleteNote: (LessonNoteUi) -> Unit,
    onUpdateNote: (LessonNoteUi, String) -> Unit,
    onSaveReminder: (LessonCard, String, String, ReminderType, Long) -> Unit,
    onUpdateReminder: (LessonReminderUi, String, String, ReminderType, Long) -> Unit,
    remindersForLesson: (LessonCard) -> List<LessonReminderUi>,
    onDeleteReminder: (LessonReminderUi) -> Unit,
    onToggleReminderCompleted: (LessonReminderUi) -> Unit,
) {
    var viewMode by remember { mutableStateOf(TimetableViewMode.WEEKLY) }
    var selectedDay by remember { mutableStateOf(1L) }
    var selectedLesson by remember { mutableStateOf<LessonCard?>(null) }
    var quickMenuAnchor by remember { mutableStateOf<QuickMenuAnchor?>(null) }
    var noteDialogLesson by remember { mutableStateOf<LessonCard?>(null) }
    var editingNote by remember { mutableStateOf<LessonNoteUi?>(null) }
    var deletingNote by remember { mutableStateOf<LessonNoteUi?>(null) }
    var reminderDialogLesson by remember { mutableStateOf<LessonCard?>(null) }
    var selectedReminder by remember { mutableStateOf<LessonReminderUi?>(null) }
    var editingReminder by remember { mutableStateOf<LessonReminderUi?>(null) }
    var deletingReminder by remember { mutableStateOf<LessonReminderUi?>(null) }

    val weeklyColumns = remember(userId, activeTermId) {
        val weekdays = listOf(
            1L to "Hétfő",
            2L to "Kedd",
            3L to "Szerda",
            4L to "Csütörtök",
            5L to "Péntek"
        )

        weekdays.map { (dayNumber, title) ->
            val lessons = if (userId == null || activeTerm == null) {
                emptyList()
            } else {
                loadLessonsForDay(
                    db = db,
                    userId = userId,
                    termId = activeTerm.id,
                    dayOfWeek = dayNumber
                )
            }

            TimetableDayColumn(
                dayOfWeek = dayNumber,
                title = title,
                lessons = lessons
            )
        }
    }

    val dailyLessons = remember(userId, activeTermId, selectedDay) {
        if (userId == null || activeTerm == null) {
            emptyList()
        } else {
            loadLessonsForDay(
                db = db,
                userId = userId,
                termId = activeTerm.id,
                dayOfWeek = selectedDay
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp)
    ) {
        Text(
            text = "Órarend",
            style = MaterialTheme.typography.headlineMedium,
            color = UniFlowTheme.colors.textPrimary
        )

        Spacer(Modifier.height(12.dp))

        SectionCard(title = "Szemeszter") {
            if (availableTerms.isEmpty()) {
                Text(
                    text = "Nincs elérhető szemeszter.",
                    color = UniFlowTheme.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                TermSwitcher(
                    terms = availableTerms,
                    activeTermId = activeTermId,
                    onTermSelected = onTermSelected
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        SectionCard(title = "Nézet") {
            TimetableViewToggle(
                selected = viewMode,
                onChange = { viewMode = it }
            )
        }

        Spacer(Modifier.height(12.dp))

        when (viewMode) {
            TimetableViewMode.WEEKLY -> {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    weeklyColumns.forEach { column ->
                        TimetableDayCard(
                            column = column,
                            onLessonClick = { selectedLesson = it },
                            onQuickMenuClick = { quickMenuAnchor = it }
                        )
                    }
                }
            }

            TimetableViewMode.DAILY -> {
                DailyTimetableContent(
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it },
                    lessons = dailyLessons,
                    onLessonClick = { selectedLesson = it },
                    onQuickMenuClick = { quickMenuAnchor = it }
                )
            }
        }
    }

    selectedLesson?.let { lesson ->
        LessonDetailsDialog(
            lesson = lesson,
            notes = notesForLesson(lesson),
            reminders = remindersForLesson(lesson),
            onDismiss = { selectedLesson = null },
            onAddNote = {
                noteDialogLesson = lesson
            },
            onAddReminder = {
                reminderDialogLesson = lesson
            },
            onAddFile = { },
            onEditNote = { note ->
                editingNote = note
            },
            onOpenReminder = { reminder ->
                selectedReminder = reminder
            },
            onEditReminder = { reminder ->
                editingReminder = reminder
            },
            onDeleteNote = { note ->
                deletingNote = note
            },
            onDeleteReminder = { reminder ->
                deletingReminder = reminder
            }
        )
    }

    deletingNote?.let { note ->
        ConfirmActionDialog(
            title = "Jegyzet törlése",
            message = "Biztosan törölni szeretnéd ezt a jegyzetet?",
            confirmText = "Törlés",
            dismissText = "Mégse",
            onConfirm = {
                onDeleteNote(note)
                deletingNote = null
            },
            onDismiss = {
                deletingNote = null
            }
        )
    }

    editingNote?.let { note ->
        EditNoteDialog(
            note = note,
            onDismiss = { editingNote = null },
            onSave = { newText ->
                onUpdateNote(note, newText)
                editingNote = null
            }
        )
    }

    noteDialogLesson?.let { lesson ->
        AddNoteDialog(
            lesson = lesson,
            onDismiss = { noteDialogLesson = null },
            onSave = { text ->
                onSaveNote(lesson, text)
                noteDialogLesson = null
            }
        )
    }

    reminderDialogLesson?.let { lesson ->
        AddReminderDialog(
            lesson = lesson,
            onDismiss = { reminderDialogLesson = null },
            onSave = { title, description, type, triggerAt ->
                onSaveReminder(lesson, title, description, type, triggerAt)
                reminderDialogLesson = null
            }
        )
    }

    selectedReminder?.let { reminder ->
        ReminderDetailsDialog(
            reminder = reminder,
            onDismiss = { selectedReminder = null },
            onEdit = {
                editingReminder = reminder
                selectedReminder = null
            },
            onToggleCompleted = {
                onToggleReminderCompleted(reminder)
                selectedReminder = null
            },
            onDelete = {
                deletingReminder = reminder
                selectedReminder = null
            }
        )
    }

    editingReminder?.let { reminder ->
        selectedLesson?.let { lesson ->
            AddReminderDialog(
                lesson = lesson,
                onDismiss = { editingReminder = null },
                initialReminder = reminder,
                dialogTitle = "Emlékeztető szerkesztése",
                confirmText = "Mentés",
                onSave = { title, description, type, triggerAt ->
                    onUpdateReminder(reminder, title, description, type, triggerAt)
                    editingReminder = null
                }
            )
        }
    }

    deletingReminder?.let { reminder ->
        ConfirmActionDialog(
            title = "Emlékeztető törlése",
            message = "Biztosan törölni szeretnéd ezt az emlékeztetőt?",
            confirmText = "Törlés",
            dismissText = "Mégse",
            onConfirm = {
                onDeleteReminder(reminder)
                deletingReminder = null
            },
            onDismiss = {
                deletingReminder = null
            }
        )
    }

    quickMenuAnchor?.let { anchor ->
        LessonQuickMenuOverlay(
            anchor = anchor,
            onDismiss = { quickMenuAnchor = null },
            onAddNote = {
                quickMenuAnchor = null
                noteDialogLesson = anchor.lesson
            },
            onAddReminder = {
                quickMenuAnchor = null
                reminderDialogLesson = anchor.lesson
            },
            onAddFile = {
                quickMenuAnchor = null
            }
        )
    }
}

@Composable
private fun TimetableDayCard(
    column: TimetableDayColumn,
    onLessonClick: (LessonCard) -> Unit,
    onQuickMenuClick: (QuickMenuAnchor) -> Unit
) {
    UniFlowGlassCard(
        modifier = Modifier.width(260.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = column.title,
                style = MaterialTheme.typography.titleMedium,
                color = UniFlowTheme.colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(12.dp))

            if (column.lessons.isEmpty()) {
                Text(
                    text = "Nincs óra",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UniFlowTheme.colors.textSecondary
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    column.lessons.forEach { lesson ->
                        LessonCardView(
                            lesson = lesson,
                            status = LessonStatus.UPCOMING,
                            breakToNextMinutes = null,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onLessonClick(lesson) },
                            onQuickMenuClick = { onQuickMenuClick(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTimetableContent(
    selectedDay: Long,
    onDaySelected: (Long) -> Unit,
    lessons: List<LessonCard>,
    onLessonClick: (LessonCard) -> Unit,
    onQuickMenuClick: (QuickMenuAnchor) -> Unit
) {
    val days = listOf(
        1L to "H",
        2L to "K",
        3L to "Sze",
        4L to "Cs",
        5L to "P"
    )

    SectionCard(title = "Napi nézet") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEach { (dayValue, label) ->
                DayButton(
                    text = label,
                    selected = selectedDay == dayValue,
                    onClick = { onDaySelected(dayValue) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (lessons.isEmpty()) {
            Text(
                text = "Erre a napra nincs óra.",
                style = MaterialTheme.typography.bodyMedium,
                color = UniFlowTheme.colors.textSecondary
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                lessons.forEach { lesson ->
                    LessonCardView(
                        lesson = lesson,
                        status = LessonStatus.UPCOMING,
                        breakToNextMinutes = null,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onLessonClick(lesson) },
                        onQuickMenuClick = { onQuickMenuClick(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)

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
