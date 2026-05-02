package com.uniflow.uniflow.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private enum class ReminderAlertOption(val title: String) {
    AT_TIME("Az időpontban"),
    ONE_HOUR("1 órával előtte"),
    ONE_DAY("1 nappal előtte"),
    NEVER("Soha")
}

@OptIn(ExperimentalTime::class, ExperimentalLayoutApi::class)
@Composable
fun AddReminderDialog(
    lesson: LessonCard,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, reminderType: ReminderType, triggerAt: Long) -> Unit,
    initialReminder: LessonReminderUi? = null,
    confirmText: String = "Emlékeztető mentése",
    dialogTitle: String = if (initialReminder == null) "Új emlékeztető" else "Emlékeztető szerkesztése"
) {
    val now = remember {
        initialReminder?.let {
            Instant.fromEpochMilliseconds(it.triggerAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }
    var title by remember(initialReminder) { mutableStateOf(initialReminder?.title ?: "") }
    var description by remember(initialReminder) {
        mutableStateOf(initialReminder?.description.orEmpty())
    }
    var selectedType by remember(initialReminder) {
        mutableStateOf(initialReminder?.let { ReminderType.fromDb(it.reminderType) } ?: ReminderType.GENERAL)
    }
    var selectedDate by remember(initialReminder) { mutableStateOf(now.date) }
    var visibleMonth by remember(initialReminder) { mutableStateOf(LocalDate(now.year, now.month, 1)) }
    var selectedHour by remember(initialReminder) { mutableStateOf(now.hour) }
    var selectedMinute by remember(initialReminder) { mutableStateOf((now.minute / 5) * 5) }
    var alertOption by remember { mutableStateOf(ReminderAlertOption.AT_TIME) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        val focusManager = LocalFocusManager.current

        UniFlowGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                        }
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = UniFlowTheme.colors.textPrimary
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "${lesson.code} • ${lesson.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UniFlowTheme.colors.textSecondary
                )

                Spacer(Modifier.height(18.dp))

                SectionTitle(title = "Emlékeztető címe")
                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (error != null) error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cím") },
                    singleLine = true,
                    colors = reminderFieldColors()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("Leírás") },
                    colors = reminderFieldColors()
                )

                Spacer(Modifier.height(18.dp))

                SectionTitle(title = "Kategória")
                Spacer(Modifier.height(10.dp))

                ReminderTypeSelector(
                    selected = selectedType,
                    onSelected = { selectedType = it }
                )

                Spacer(Modifier.height(18.dp))

                SectionTitle(title = "Időpont")
                Spacer(Modifier.height(10.dp))

                SectionCard {
                    CalendarHeader(
                        visibleMonth = visibleMonth,
                        onPreviousMonth = { visibleMonth = visibleMonth.minus(DatePeriod(months = 1)) },
                        onNextMonth = { visibleMonth = visibleMonth.plus(DatePeriod(months = 1)) }
                    )

                    Spacer(Modifier.height(12.dp))

                    CalendarGrid(
                        visibleMonth = visibleMonth,
                        selectedDate = selectedDate,
                        onDateSelected = { day ->
                            selectedDate = day
                            visibleMonth = LocalDate(day.year, day.month, 1)
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = formatSelectedDate(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        color = UniFlowTheme.colors.textPrimary
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TimeAdjustCard(
                            modifier = Modifier.weight(1f),
                            label = "Óra",
                            valueText = selectedHour.twoDigits(),
                            helperText = "24 órás",
                            onIncrement = { selectedHour = (selectedHour + 1) % 24 },
                            onDecrement = { selectedHour = if (selectedHour == 0) 23 else selectedHour - 1 }
                        )

                        TimeAdjustCard(
                            modifier = Modifier.weight(1f),
                            label = "Perc",
                            valueText = selectedMinute.twoDigits(),
                            helperText = "5 perces",
                            onIncrement = { selectedMinute = (selectedMinute + 5) % 60 },
                            onDecrement = { selectedMinute = if (selectedMinute == 0) 55 else selectedMinute - 5 }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                SectionTitle(title = "Emlékeztetés")
                Spacer(Modifier.height(10.dp))

                ReminderAlertSelector(
                    selected = alertOption,
                    onSelected = { alertOption = it }
                )

                error?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = it,
                        color = UniFlowTheme.colors.danger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = {
                        val triggerAt = buildReminderEpochMillis(
                            selectedDate = selectedDate,
                            selectedHour = selectedHour,
                            selectedMinute = selectedMinute,
                            alertOption = alertOption
                        )
                        when {
                            title.isBlank() -> error = "A cím megadása kötelező."
                            triggerAt == null -> error = "Nem sikerült összeállítani a kiválasztott időpontot."
                            triggerAt <= Clock.System.now().toEpochMilliseconds() ->
                                error = "Az emlékeztetés időpontja már elmúlt."
                            else -> {
                                error = null
                                onSave(title.trim(), description.trim(), selectedType, triggerAt)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(confirmText)
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
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
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = UniFlowTheme.colors.textPrimary
    )
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(UniFlowTheme.colors.chipBackground)
            .border(1.dp, UniFlowTheme.colors.glassBorder, RoundedCornerShape(20.dp))
            .padding(14.dp),
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderTypeSelector(
    selected: ReminderType,
    onSelected: (ReminderType) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReminderType.entries.forEach { type ->
            val visual = reminderTypeVisual(type)
            CategoryCard(
                modifier = Modifier.width(112.dp),
                title = type.label,
                iconTint = visual.accent,
                icon = visual.icon,
                selected = selected == type,
                accentColor = visual.accent,
                onClick = { onSelected(type) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderAlertSelector(
    selected: ReminderAlertOption,
    onSelected: (ReminderAlertOption) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ReminderAlertOption.entries.forEach { option ->
            CategoryCard(
                modifier = Modifier.width(112.dp),
                title = option.title,
                icon = Icons.Filled.NotificationsActive,
                iconTint = MaterialTheme.colorScheme.primary,
                selected = selected == option,
                accentColor = MaterialTheme.colorScheme.primary,
                onClick = { onSelected(option) }
            )
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) accentColor.copy(alpha = 0.18f) else UniFlowTheme.colors.chipBackground)
            .border(
                1.dp,
                if (selected) accentColor else UniFlowTheme.colors.glassBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = UniFlowTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalendarHeader(
    visibleMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Előző hónap",
                tint = UniFlowTheme.colors.textPrimary
            )
        }

        Text(
            text = formatMonthYear(visibleMonth),
            style = MaterialTheme.typography.titleMedium,
            color = UniFlowTheme.colors.textPrimary
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Következő hónap",
                tint = UniFlowTheme.colors.textPrimary
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    visibleMonth: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = LocalDate(visibleMonth.year, visibleMonth.month, 1)
    val daysInMonth = firstDayOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day
    val leadingDays = firstDayOfMonth.dayOfWeek.isoDayNumber - 1

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            dayHeaders.forEach { day ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = UniFlowTheme.colors.textSecondary
                    )
                }
            }
        }

        var dayNumber = 1
        repeat(6) { weekIndex ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(7) { columnIndex ->
                    val cellIndex = weekIndex * 7 + columnIndex
                    if (cellIndex < leadingDays || dayNumber > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).height(42.dp))
                    } else {
                        val date = LocalDate(visibleMonth.year, visibleMonth.month, dayNumber)
                        val isSelected = date == selectedDate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else UniFlowTheme.colors.glassBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else UniFlowTheme.colors.textPrimary
                            )
                        }
                        dayNumber += 1
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeAdjustCard(
    modifier: Modifier = Modifier,
    label: String,
    valueText: String,
    helperText: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(UniFlowTheme.colors.glassSurface)
            .border(1.dp, UniFlowTheme.colors.glassBorder, RoundedCornerShape(18.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = UniFlowTheme.colors.textSecondary)
        Spacer(Modifier.height(6.dp))
        IconButton(onClick = onIncrement) {
            Icon(Icons.Filled.Add, "$label növelése", tint = UniFlowTheme.colors.textPrimary)
        }
        Text(text = valueText, style = MaterialTheme.typography.headlineMedium, color = UniFlowTheme.colors.textPrimary)
        Spacer(Modifier.height(2.dp))
        Text(text = helperText, style = MaterialTheme.typography.bodySmall, color = UniFlowTheme.colors.textSecondary)
        Spacer(Modifier.height(2.dp))
        IconButton(onClick = onDecrement) {
            Icon(Icons.Filled.Remove, "$label csökkentése", tint = UniFlowTheme.colors.textPrimary)
        }
    }
}

@Composable
private fun reminderFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = UniFlowTheme.colors.textPrimary,
    unfocusedTextColor = UniFlowTheme.colors.textPrimary,
    focusedLabelColor = UniFlowTheme.colors.textSecondary,
    unfocusedLabelColor = UniFlowTheme.colors.textSecondary,
    focusedBorderColor = UniFlowTheme.colors.glassBorder,
    unfocusedBorderColor = UniFlowTheme.colors.glassBorder,
    focusedContainerColor = UniFlowTheme.colors.glassSurface,
    unfocusedContainerColor = UniFlowTheme.colors.glassSurface,
    cursorColor = MaterialTheme.colorScheme.primary
)

@OptIn(ExperimentalTime::class)
private fun buildReminderEpochMillis(
    selectedDate: LocalDate,
    selectedHour: Int,
    selectedMinute: Int,
    alertOption: ReminderAlertOption
): Long? {
    return runCatching {
        val dateTime = LocalDateTime.parse("${selectedDate}T${selectedHour.twoDigits()}:${selectedMinute.twoDigits()}:00")
        val selectedAt = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        when (alertOption) {
            ReminderAlertOption.AT_TIME -> selectedAt
            ReminderAlertOption.ONE_HOUR -> selectedAt - 60 * 60 * 1000L
            ReminderAlertOption.ONE_DAY -> selectedAt - 24 * 60 * 60 * 1000L
            ReminderAlertOption.NEVER -> selectedAt
        }
    }.getOrNull()
}

private fun formatMonthYear(date: LocalDate): String = "${monthNames[date.month.number - 1]} ${date.year}"

private fun formatSelectedDate(date: LocalDate): String {
    val weekdayName = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "hétfő"
        DayOfWeek.TUESDAY -> "kedd"
        DayOfWeek.WEDNESDAY -> "szerda"
        DayOfWeek.THURSDAY -> "csütörtök"
        DayOfWeek.FRIDAY -> "péntek"
        DayOfWeek.SATURDAY -> "szombat"
        DayOfWeek.SUNDAY -> "vasárnap"
    }
    return "${date.year}. ${monthNamesLowercase[date.month.number - 1]} ${date.day}. • $weekdayName"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private val dayHeaders = listOf("H", "K", "Sze", "Cs", "P", "Szo", "V")
private val monthNames = listOf("Január", "Február", "Március", "Április", "Május", "Június", "Július", "Augusztus", "Szeptember", "Október", "November", "December")
private val monthNamesLowercase = listOf("január", "február", "március", "április", "május", "június", "július", "augusztus", "szeptember", "október", "november", "december")
