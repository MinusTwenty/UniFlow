package com.uniflow.uniflow.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
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
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalTime::class)
@Composable
fun RemindersScreen(
    modifier: Modifier = Modifier,
    reminders: List<LessonReminderUi>,
    onUpdateReminder: (LessonReminderUi, String, String, ReminderType, Long) -> Unit,
    onToggleReminderCompleted: (LessonReminderUi) -> Unit,
    onDeleteReminder: (LessonReminderUi) -> Unit
) {
    var selectedReminder by remember { mutableStateOf<LessonReminderUi?>(null) }
    var editingReminder by remember { mutableStateOf<LessonReminderUi?>(null) }

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tomorrow = now.plus(DatePeriod(days = 1))
    val nextWeekEnd = now.plus(DatePeriod(days = 7))

    val todayReminders = reminders.filter { reminderDate(it) == now }
    val tomorrowReminders = reminders.filter { reminderDate(it) == tomorrow }
    val upcomingReminders = reminders.filter {
        val date = reminderDate(it)
        date > tomorrow && date <= nextWeekEnd
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp)
    ) {
        Text(
            text = "Emlékeztetők",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = UniFlowTheme.colors.textPrimary
        )

        Spacer(Modifier.height(12.dp))

        ReminderSection("Ma", todayReminders, onOpen = { selectedReminder = it }, onEdit = { editingReminder = it }, onDelete = onDeleteReminder)
        Spacer(Modifier.height(12.dp))
        ReminderSection("Holnap", tomorrowReminders, onOpen = { selectedReminder = it }, onEdit = { editingReminder = it }, onDelete = onDeleteReminder)
        Spacer(Modifier.height(12.dp))
        ReminderSection("Következő 7 napban", upcomingReminders, onOpen = { selectedReminder = it }, onEdit = { editingReminder = it }, onDelete = onDeleteReminder)
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
                onDeleteReminder(reminder)
                selectedReminder = null
            }
        )
    }

    editingReminder?.let { reminder ->
        AddReminderDialog(
            lesson = LessonCard(
                lessonId = 0L,
                code = "Emlékeztető",
                title = "Szerkesztés",
                time = "",
                room = "",
                building = "",
                teacher = "",
                lessonType = "",
                note = null,
                groupCode = "",
                credits = 0L,
                weekType = ""
            ),
            onDismiss = { editingReminder = null },
            initialReminder = reminder,
            dialogTitle = "Emlékeztető szerkesztése",
            confirmText = "Mentés",
            onSave = { title, description, reminderType, triggerAt ->
                onUpdateReminder(reminder, title, description, reminderType, triggerAt)
                editingReminder = null
            }
        )
    }
}

@Composable
private fun ReminderSection(
    title: String,
    reminders: List<LessonReminderUi>,
    onOpen: (LessonReminderUi) -> Unit,
    onEdit: (LessonReminderUi) -> Unit,
    onDelete: (LessonReminderUi) -> Unit
) {
    UniFlowGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = UniFlowTheme.colors.textPrimary
            )

            if (reminders.isEmpty()) {
                Text(
                    text = "Nincs emlékeztető ebben a szakaszban.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UniFlowTheme.colors.textSecondary
                )
            } else {
                reminders.forEach { reminder ->
                    LessonReminderItem(
                        reminder = reminder,
                        onOpen = { onOpen(reminder) },
                        onEdit = { onEdit(reminder) },
                        onDelete = { onDelete(reminder) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun reminderDate(reminder: LessonReminderUi) =
    Instant.fromEpochMilliseconds(reminder.triggerAt)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
