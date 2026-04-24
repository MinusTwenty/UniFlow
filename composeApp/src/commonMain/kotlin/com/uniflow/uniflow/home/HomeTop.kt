package com.uniflow.uniflow.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.GlassChip
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import kotlinx.datetime.LocalDate

@Composable
fun HomeTop(
    modifier: Modifier = Modifier,
    student: StudentInfo,
    location: String,
    building: String,
    teacher: String,
    dateText: String,
    nextRoom: String,
    nextTeacher: String,
    nextBuilding: String,
    upcoming: List<LessonCard>,
    nowTime: Int,
    currentDate: LocalDate,
    lessonLayoutMode: LessonLayoutMode,
    onLessonLayoutModeChange: (LessonLayoutMode) -> Unit,
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
    onToggleReminderCompleted: (LessonReminderUi) -> Unit
) {
    val today = WeekUtil.today()
    val iso = WeekUtil.isoWeekInfo(today)
    val parity = WeekUtil.isoWeekParity(today)
    var selectedLesson by remember { mutableStateOf<LessonCard?>(null) }
    var quickMenuAnchor by remember { mutableStateOf<QuickMenuAnchor?>(null) }
    var noteDialogLesson by remember { mutableStateOf<LessonCard?>(null) }
    var editingNote by remember { mutableStateOf<LessonNoteUi?>(null) }
    var deletingNote by remember { mutableStateOf<LessonNoteUi?>(null) }
    var reminderDialogLesson by remember { mutableStateOf<LessonCard?>(null) }
    var selectedReminder by remember { mutableStateOf<LessonReminderUi?>(null) }
    var editingReminder by remember { mutableStateOf<LessonReminderUi?>(null) }
    var deletingReminder by remember { mutableStateOf<LessonReminderUi?>(null) }

    val acad = activeTerm?.let { term ->
        val start = LocalDate.parse(term.startDate)
        val end = LocalDate.parse(term.endDate)

        WeekUtil.academicWeek(
            date = today,
            semesterStart = start,
            semesterEnd = end
        )
    }

    val nowSec = nowTime

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp)
    ) {
        SectionCard(title = "Hallgató") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = student.uniShort,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = UniFlowTheme.colors.textPrimary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = UniFlowTheme.colors.textPrimary
                )
                Spacer(Modifier.weight(1f))
                LangChip("HU")
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassChip(text = "ISO hét: ${iso.week}")
                GlassChip(
                    text = if (parity == WeekUtil.WeekParity.Even) "Páros hét" else "Páratlan hét"
                )
                acad?.let {
                    GlassChip(text = "Akadémiai hét: $it")
                }
                GlassChip(text = "Dátum: $dateText")
            }
        }

        Spacer(Modifier.height(12.dp))

        UniFlowGlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Óra adatok",
                    style = MaterialTheme.typography.headlineSmall,
                    color = UniFlowTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Épület: $building", color = UniFlowTheme.colors.textPrimary)
                        Text("Hely: $location", color = UniFlowTheme.colors.textPrimary)
                        Text("Tanár: $teacher", color = UniFlowTheme.colors.textPrimary)
                    }

                    Spacer(Modifier.width(20.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Köv. épület: $nextBuilding", color = UniFlowTheme.colors.textPrimary)
                        Text("Köv. hely: $nextRoom", color = UniFlowTheme.colors.textPrimary)
                        Text("Köv. tanár: $nextTeacher", color = UniFlowTheme.colors.textPrimary)
                    }
                }
            }
        }

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

        run {
            val breakRemaining = findBreakRemainingSeconds(nowSec, upcoming)
            val activeRemaining = findActiveLessonRemainingSeconds(nowSec, upcoming)

            when {
                activeRemaining != null && activeRemaining > 0 -> {
                    SectionCard(title = "Aktuális óra") {
                        Text(
                            text = "Hátralévő idő az órából: ${formatHuDurationDynamic(activeRemaining)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                breakRemaining != null && breakRemaining > 0 -> {
                    SectionCard(title = "Szünet") {
                        Text(
                            text = "A következő óráig: ${formatHuDurationDynamic(breakRemaining)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }

            UniFlowGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Következő órák",
                            style = MaterialTheme.typography.headlineSmall,
                            color = UniFlowTheme.colors.textPrimary
                        )

                        LessonLayoutToggle(
                            selectedMode = lessonLayoutMode,
                            onModeChange = onLessonLayoutModeChange
                        )
                    }

                    val statuses = upcoming.map { it to statusFor(it, nowSec) }
                    val activeIndex = statuses.indexOfFirst { it.second == LessonStatus.ACTIVE }
                    val nextIndex = statuses.indexOfFirst { it.second == LessonStatus.UPCOMING }
                    val breakMinutes =
                        if (activeIndex >= 0 && nextIndex == activeIndex + 1) {
                            breakBetween(upcoming[activeIndex], upcoming[nextIndex])
                        } else {
                            null
                        }

                    if (upcoming.isEmpty()) {
                        Text(
                            text = "Erre a napra nincs óra.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UniFlowTheme.colors.textSecondary
                        )
                    } else {
                        when (lessonLayoutMode) {
                            LessonLayoutMode.HORIZONTAL -> {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(upcoming) { index, lesson ->
                                        val status = statuses.getOrNull(index)?.second ?: LessonStatus.PAST
                                        val breakToNext = if (index == activeIndex) breakMinutes else null

                                        LessonCardView(
                                            lesson = lesson,
                                            status = status,
                                            breakToNextMinutes = breakToNext,
                                            modifier = Modifier.width(200.dp),
                                            onClick = { selectedLesson = lesson },
                                            onQuickMenuClick = { quickMenuAnchor = it }
                                        )
                                    }
                                }
                            }

                            LessonLayoutMode.VERTICAL -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    upcoming.forEachIndexed { index, lesson ->
                                        val status = statuses.getOrNull(index)?.second ?: LessonStatus.PAST
                                        val breakToNext = if (index == activeIndex) breakMinutes else null

                                        LessonCardView(
                                            lesson = lesson,
                                            status = status,
                                            breakToNextMinutes = breakToNext,
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = { selectedLesson = lesson },
                                            onQuickMenuClick = { quickMenuAnchor = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
