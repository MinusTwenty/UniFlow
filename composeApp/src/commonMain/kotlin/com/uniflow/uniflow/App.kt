package com.uniflow.uniflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.russhwolf.settings.Settings
import com.uniflow.uniflow.auth.DbAuthRepository
import com.uniflow.uniflow.data.DatabaseDriverFactory
import com.uniflow.uniflow.data.provideDatabase
import com.uniflow.uniflow.data.seed.resetAndReseedDemoData
import com.uniflow.uniflow.home.AcademicTerm
import com.uniflow.uniflow.home.HomeTop
import com.uniflow.uniflow.home.LessonCard
import com.uniflow.uniflow.home.LessonLayoutMode
import com.uniflow.uniflow.home.LessonNoteUi
import com.uniflow.uniflow.home.LessonReminderUi
import com.uniflow.uniflow.home.LessonStatus
import com.uniflow.uniflow.home.ReminderType
import com.uniflow.uniflow.home.RemindersScreen
import com.uniflow.uniflow.home.StudentInfo
import com.uniflow.uniflow.home.TimetableScreen
import com.uniflow.uniflow.home.WeekUtil.today
import com.uniflow.uniflow.home.buildingNameFromRoom
import com.uniflow.uniflow.home.currentSecondsSinceMidnight
import com.uniflow.uniflow.home.loadLessonsForDay
import com.uniflow.uniflow.home.statusFor
import com.uniflow.uniflow.settings.ThemeSettings
import com.uniflow.uniflow.ui.settings.SettingsScreen
import com.uniflow.uniflow.ui.theme.UniFlowAppTheme
import com.uniflow.uniflow.ui.theme.UniFlowBackground
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

private enum class MainTab {
    HOME,
    TIMETABLE,
    REMINDERS,
    SETTINGS
}

private fun demoStudentInfo(userId: Long?): StudentInfo {
    return when (userId) {
        1L -> StudentInfo(uniShort = "UJS", fullName = "1. emberke", weekType = "")
        2L -> StudentInfo(uniShort = "UJS", fullName = "2. emberke", weekType = "")
        3L -> StudentInfo(uniShort = "UJS", fullName = "3. emberke", weekType = "")
        else -> StudentInfo(uniShort = "UJS", fullName = "-", weekType = "")
    }
}

@OptIn(ExperimentalTime::class)
fun todayDisplayText(): String {
    val now = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    return "${now.year}.${now.month.number.toString().padStart(2, '0')}.${now.day.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalTime::class)
@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory
) {
    val themeSettings = remember { ThemeSettings(Settings()) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var loggedInUserId by remember { mutableStateOf<Long?>(null) }
    var selectedTheme by remember { mutableStateOf(themeSettings.getSavedTheme()) }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var lessonLayoutMode by remember { mutableStateOf(LessonLayoutMode.HORIZONTAL) }
    var nowSec by remember { mutableStateOf(currentSecondsSinceMidnight()) }
    var today by remember { mutableStateOf(today()) }
    val db = remember { provideDatabase(databaseDriverFactory) }

    val availableTerms = remember(loggedInUserId) {
        if (loggedInUserId == null) {
            emptyList()
        } else {
            val enrollments = db.enrollmentQueries
                .getEnrollmentsForUser(loggedInUserId!!)
                .executeAsList()

            val userTermIds = enrollments
                .map { it.term_id }
                .distinct()

            db.termsQueries
                .getAllTerms()
                .executeAsList()
                .filter { it.id in userTermIds }
                .sortedBy { it.start_date }
                .map {
                    AcademicTerm(
                        id = it.id,
                        name = it.name,
                        startDate = it.start_date,
                        endDate = it.end_date
                    )
                }
        }
    }

    var activeTermId by remember(loggedInUserId) {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(loggedInUserId, availableTerms) {
        activeTermId = availableTerms.firstOrNull()?.id
    }

    val activeTerm = availableTerms.firstOrNull { it.id == activeTermId }
    val selectedDayOfWeek = today.dayOfWeek.isoDayNumber.toLong()
    val authRepository = remember { DbAuthRepository(db) }

    val onLogout = {
        loggedInUserId = null
        isLoggedIn = false
        selectedTab = MainTab.HOME
    }

    val onDemoReset = {
        resetAndReseedDemoData(db)
        loggedInUserId = null
        isLoggedIn = false
        selectedTab = MainTab.HOME
    }

    val onSaveNote: (LessonCard, String) -> Unit = { lesson, text ->
        val userId = loggedInUserId
        if (userId != null && text.isNotBlank()) {
            db.lessonNotesQueries.insertLessonNote(
                user_id = userId,
                lesson_id = lesson.lessonId,
                content = text.trim(),
                created_at = Clock.System.now().toEpochMilliseconds()
            )
        }
    }

    val onDeleteNote: (LessonNoteUi) -> Unit = { note ->
        db.lessonNotesQueries.deleteLessonNote(id = note.id)
    }

    val onUpdateNote: (LessonNoteUi, String) -> Unit = { note, newText ->
        if (newText.isNotBlank()) {
            db.lessonNotesQueries.updateLessonNote(
                content = newText.trim(),
                created_at = Clock.System.now().toEpochMilliseconds(),
                id = note.id
            )
        }
    }

    val notesForLesson: (LessonCard) -> List<LessonNoteUi> = { lesson ->
        val userId = loggedInUserId
        if (userId == null) {
            emptyList()
        } else {
            db.lessonNotesQueries
                .getNotesForLesson(user_id = userId, lesson_id = lesson.lessonId)
                .executeAsList()
                .map {
                    LessonNoteUi(
                        id = it.id,
                        content = it.content,
                        createdAt = it.created_at
                    )
                }
        }
    }

    val onSaveReminder: (LessonCard, String, String, ReminderType, Long) -> Unit =
        { lesson, title, description, reminderType, triggerAt ->
            val userId = loggedInUserId
            if (userId != null && title.isNotBlank()) {
                val now = Clock.System.now().toEpochMilliseconds()

                db.lessonRemindersQueries.insertLessonReminder(
                    user_id = userId,
                    lesson_id = lesson.lessonId,
                    title = title.trim(),
                    description = description.trim().ifBlank { null },
                    reminder_type = reminderType.dbValue,
                    trigger_at = triggerAt,
                    is_enabled = 1L,
                    created_at = now
                )
            }
        }

    val onUpdateReminder: (LessonReminderUi, String, String, ReminderType, Long) -> Unit =
        { reminder, title, description, reminderType, triggerAt ->
            db.lessonRemindersQueries.updateLessonReminder(
                title = title.trim(),
                description = description.trim().ifBlank { null },
                reminder_type = reminderType.dbValue,
                trigger_at = triggerAt,
                is_enabled = if (reminder.isEnabled) 1L else 0L,
                id = reminder.id
            )
        }

    val onToggleReminderCompleted: (LessonReminderUi) -> Unit = { reminder ->
        db.lessonRemindersQueries.setReminderEnabled(
            is_enabled = if (reminder.isEnabled) 0L else 1L,
            id = reminder.id
        )
    }

    val onDeleteReminder: (LessonReminderUi) -> Unit = { reminder ->
        db.lessonRemindersQueries.deleteLessonReminder(id = reminder.id)
    }

    val remindersForLesson: (LessonCard) -> List<LessonReminderUi> = { lesson ->
        val userId = loggedInUserId
        if (userId == null) {
            emptyList()
        } else {
            db.lessonRemindersQueries
                .getRemindersForLesson(user_id = userId, lesson_id = lesson.lessonId)
                .executeAsList()
                .map {
                    LessonReminderUi(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        reminderType = it.reminder_type,
                        triggerAt = it.trigger_at,
                        isEnabled = it.is_enabled == 1L,
                        createdAt = it.created_at
                    )
                }
        }
    }

    val allReminders = remember(loggedInUserId, nowSec) {
        val userId = loggedInUserId
        if (userId == null) {
            emptyList()
        } else {
            db.lessonRemindersQueries
                .getAllRemindersForUser(user_id = userId)
                .executeAsList()
                .map {
                    LessonReminderUi(
                        id = it.id,
                        title = it.title,
                        description = it.description,
                        reminderType = it.reminder_type,
                        triggerAt = it.trigger_at,
                        isEnabled = it.is_enabled == 1L,
                        createdAt = it.created_at
                    )
                }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            nowSec = currentSecondsSinceMidnight()
            today = today()
            kotlinx.coroutines.delay(1000)
        }
    }

    UniFlowAppTheme(mode = selectedTheme) {
        UniFlowBackground {
            if (!isLoggedIn) {
                LoginScreenWithValidation(
                    repository = authRepository,
                    onLoginSuccess = { userId ->
                        loggedInUserId = userId
                        isLoggedIn = true
                    }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == MainTab.HOME,
                                onClick = { selectedTab = MainTab.HOME },
                                label = { Text("Kezdőlap") },
                                icon = { Icon(Icons.Filled.Home, contentDescription = null) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainTab.TIMETABLE,
                                onClick = { selectedTab = MainTab.TIMETABLE },
                                label = { Text("Órarend") },
                                icon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainTab.REMINDERS,
                                onClick = { selectedTab = MainTab.REMINDERS },
                                label = { Text("Emlékeztetők") },
                                icon = { Icon(Icons.Filled.Notifications, contentDescription = null) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainTab.SETTINGS,
                                onClick = { selectedTab = MainTab.SETTINGS },
                                label = { Text("Beállítások") },
                                icon = { Icon(Icons.Filled.Settings, contentDescription = null) }
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        MainTab.HOME -> {
                            val upcomingLessons = if (loggedInUserId != null && activeTerm != null) {
                                loadLessonsForDay(
                                    db = db,
                                    userId = loggedInUserId!!,
                                    termId = activeTerm.id,
                                    dayOfWeek = selectedDayOfWeek
                                )
                            } else {
                                emptyList()
                            }

                            val currentLesson = upcomingLessons.firstOrNull { lesson ->
                                statusFor(lesson, nowSec) == LessonStatus.ACTIVE
                            }

                            val nextLesson = upcomingLessons.firstOrNull { lesson ->
                                statusFor(lesson, nowSec) == LessonStatus.UPCOMING
                            }

                            HomeTop(
                                modifier = Modifier.padding(innerPadding),
                                student = demoStudentInfo(loggedInUserId),
                                location = currentLesson?.room ?: "-",
                                building = buildingNameFromRoom(currentLesson?.room),
                                dateText = todayDisplayText(),
                                teacher = currentLesson?.teacher ?: "-",
                                nextRoom = nextLesson?.room ?: "-",
                                nextTeacher = nextLesson?.teacher ?: "-",
                                nextBuilding = buildingNameFromRoom(nextLesson?.room),
                                upcoming = upcomingLessons,
                                nowTime = nowSec,
                                currentDate = today,
                                lessonLayoutMode = lessonLayoutMode,
                                onLessonLayoutModeChange = { lessonLayoutMode = it },
                                activeTerm = activeTerm,
                                availableTerms = availableTerms,
                                activeTermId = activeTermId,
                                onTermSelected = { activeTermId = it },
                                onSaveNote = onSaveNote,
                                notesForLesson = notesForLesson,
                                onDeleteNote = onDeleteNote,
                                onUpdateNote = onUpdateNote,
                                onSaveReminder = onSaveReminder,
                                onUpdateReminder = onUpdateReminder,
                                remindersForLesson = remindersForLesson,
                                onDeleteReminder = onDeleteReminder,
                                onToggleReminderCompleted = onToggleReminderCompleted
                            )
                        }

                        MainTab.TIMETABLE -> {
                            TimetableScreen(
                                modifier = Modifier.padding(innerPadding),
                                db = db,
                                userId = loggedInUserId,
                                activeTerm = activeTerm,
                                availableTerms = availableTerms,
                                activeTermId = activeTermId,
                                onTermSelected = { activeTermId = it },
                                onSaveNote = onSaveNote,
                                notesForLesson = notesForLesson,
                                onDeleteNote = onDeleteNote,
                                onUpdateNote = onUpdateNote,
                                onSaveReminder = onSaveReminder,
                                onUpdateReminder = onUpdateReminder,
                                remindersForLesson = remindersForLesson,
                                onDeleteReminder = onDeleteReminder,
                                onToggleReminderCompleted = onToggleReminderCompleted
                            )
                        }

                        MainTab.REMINDERS -> {
                            RemindersScreen(
                                modifier = Modifier.padding(innerPadding),
                                reminders = allReminders,
                                onUpdateReminder = onUpdateReminder,
                                onToggleReminderCompleted = onToggleReminderCompleted,
                                onDeleteReminder = onDeleteReminder
                            )
                        }

                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                modifier = Modifier.padding(innerPadding),
                                selectedTheme = selectedTheme,
                                onThemeSelected = {
                                    selectedTheme = it
                                    themeSettings.saveTheme(it)
                                },
                                onLogout = onLogout,
                                onDemoReset = onDemoReset
                            )
                        }
                    }
                }
            }
        }
    }
}
