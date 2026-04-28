package com.uniflow.uniflow

import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import com.uniflow.uniflow.auth.DbAuthRepository
import com.uniflow.uniflow.data.DatabaseDriverFactory
import com.uniflow.uniflow.data.provideDatabase
import com.uniflow.uniflow.data.seed.resetAndReseedDemoData
import com.uniflow.uniflow.home.AcademicTerm
import com.uniflow.uniflow.home.HomeTop
import com.uniflow.uniflow.home.LessonCard
import com.uniflow.uniflow.home.LessonNotificationScheduler
import com.uniflow.uniflow.home.LessonLayoutMode
import com.uniflow.uniflow.home.LessonNoteUi
import com.uniflow.uniflow.home.LessonReminderUi
import com.uniflow.uniflow.home.LessonStatus
import com.uniflow.uniflow.home.ReminderType
import com.uniflow.uniflow.home.ReminderNotificationScheduler
import com.uniflow.uniflow.home.RemindersScreen
import com.uniflow.uniflow.home.StudentInfo
import com.uniflow.uniflow.home.TimetableScreen
import com.uniflow.uniflow.home.WeekUtil.today
import com.uniflow.uniflow.home.buildUpcomingLessonSchedules
import com.uniflow.uniflow.home.buildingNameFromRoom
import com.uniflow.uniflow.home.currentSecondsSinceMidnight
import com.uniflow.uniflow.home.loadLessonsForDay
import com.uniflow.uniflow.home.statusFor
import com.uniflow.uniflow.settings.LessonNotificationLeadTime
import com.uniflow.uniflow.settings.LessonNotificationSettings
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

private fun defaultActiveTermId(
    terms: List<AcademicTerm>,
    today: LocalDate
): Long? {
    return terms
        .firstOrNull { term ->
            val start = LocalDate.parse(term.startDate)
            val end = LocalDate.parse(term.endDate)
            today >= start && today <= end
        }
        ?.id
        ?: terms.maxByOrNull { LocalDate.parse(it.startDate) }?.id
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
    val lessonNotificationSettings = remember { LessonNotificationSettings(Settings()) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var loggedInUserId by remember { mutableStateOf<Long?>(null) }
    var selectedTheme by remember { mutableStateOf(themeSettings.getSavedTheme()) }
    var lessonNotificationLeadTime by remember {
        mutableStateOf(lessonNotificationSettings.getSavedLeadTime())
    }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var lessonLayoutMode by remember { mutableStateOf(LessonLayoutMode.HORIZONTAL) }
    var nowSec by remember { mutableStateOf(currentSecondsSinceMidnight()) }
    var today by remember { mutableStateOf(today()) }
    val lessonNotificationRefreshBucket = nowSec / 60
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

    LaunchedEffect(loggedInUserId, availableTerms, today) {
        val currentSelectionStillValid = activeTermId?.let { selectedId ->
            availableTerms.any { it.id == selectedId }
        } == true

        if (currentSelectionStillValid) return@LaunchedEffect

        activeTermId = defaultActiveTermId(
            terms = availableTerms,
            today = today
        )
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

                val insertedId = db.lessonRemindersQueries.lastInsertedReminderId().executeAsOne()
                ReminderNotificationScheduler.schedule(
                    LessonReminderUi(
                        id = insertedId,
                        lessonId = lesson.lessonId,
                        title = title.trim(),
                        description = description.trim().ifBlank { null },
                        reminderType = reminderType.dbValue,
                        triggerAt = triggerAt,
                        isEnabled = true,
                        createdAt = now,
                        lessonCode = lesson.code
                    )
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

            val updatedReminder = reminder.copy(
                title = title.trim(),
                description = description.trim().ifBlank { null },
                reminderType = reminderType.dbValue,
                triggerAt = triggerAt
            )
            if (updatedReminder.isEnabled) {
                ReminderNotificationScheduler.schedule(updatedReminder)
            } else {
                ReminderNotificationScheduler.cancel(updatedReminder.id)
            }
        }

    val onToggleReminderCompleted: (LessonReminderUi) -> Unit = { reminder ->
        db.lessonRemindersQueries.setReminderEnabled(
            is_enabled = if (reminder.isEnabled) 0L else 1L,
            id = reminder.id
        )

        if (reminder.isEnabled) {
            ReminderNotificationScheduler.cancel(reminder.id)
        } else {
            ReminderNotificationScheduler.schedule(reminder.copy(isEnabled = true))
        }
    }

    val onDeleteReminder: (LessonReminderUi) -> Unit = { reminder ->
        db.lessonRemindersQueries.deleteLessonReminder(id = reminder.id)
        ReminderNotificationScheduler.cancel(reminder.id)
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
                        lessonId = it.lesson_id,
                        title = it.title,
                        description = it.description,
                        reminderType = it.reminder_type,
                        triggerAt = it.trigger_at,
                        isEnabled = it.is_enabled == 1L,
                        createdAt = it.created_at,
                        lessonCode = lesson.code
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
                    val lesson = db.lessonsQueries
                        .getLessonById(id = it.lesson_id)
                        .executeAsOneOrNull()
                    val courseCode = lesson?.course_id?.let { courseId ->
                        db.coursesQueries
                            .getAllCourses()
                            .executeAsList()
                            .firstOrNull { course -> course.id == courseId }
                            ?.code
                            ?.substringAfter("/")
                    }

                    LessonReminderUi(
                        id = it.id,
                        lessonId = it.lesson_id,
                        title = it.title,
                        description = it.description,
                        reminderType = it.reminder_type,
                        triggerAt = it.trigger_at,
                        isEnabled = it.is_enabled == 1L,
                        createdAt = it.created_at,
                        lessonCode = courseCode
                    )
                }
        }
    }

    LaunchedEffect(Unit) {
        ReminderNotificationScheduler.requestPermissionIfNeeded()
        LessonNotificationScheduler.requestPermissionIfNeeded()
        while (true) {
            nowSec = currentSecondsSinceMidnight()
            today = today()
            kotlinx.coroutines.delay(1000)
        }
    }

    LaunchedEffect(loggedInUserId) {
        val userId = loggedInUserId ?: return@LaunchedEffect
        db.lessonRemindersQueries
            .getAllRemindersForUser(user_id = userId)
            .executeAsList()
            .forEach { reminder ->
                val lesson = db.lessonsQueries.getLessonById(id = reminder.lesson_id).executeAsOneOrNull()
                val courseCode = lesson?.course_id?.let { courseId ->
                    db.coursesQueries
                        .getAllCourses()
                        .executeAsList()
                        .firstOrNull { course -> course.id == courseId }
                        ?.code
                        ?.substringAfter("/")
                }

                if (reminder.is_enabled == 1L) {
                    ReminderNotificationScheduler.schedule(
                        LessonReminderUi(
                            id = reminder.id,
                            lessonId = reminder.lesson_id,
                            title = reminder.title,
                            description = reminder.description,
                            reminderType = reminder.reminder_type,
                            triggerAt = reminder.trigger_at,
                            isEnabled = true,
                            createdAt = reminder.created_at,
                            lessonCode = courseCode
                        )
                    )
                } else {
                    ReminderNotificationScheduler.cancel(reminder.id)
                }
            }
    }

    LaunchedEffect(
        loggedInUserId,
        activeTermId,
        lessonNotificationLeadTime,
        today,
        lessonNotificationRefreshBucket
    ) {
        val userId = loggedInUserId
        val termId = activeTermId
        if (userId == null || termId == null || lessonNotificationLeadTime == LessonNotificationLeadTime.OFF) {
            LessonNotificationScheduler.cancelAll()
            return@LaunchedEffect
        }

        val lessons = db.lessonsQueries
            .getUserLessonsForTerm(user_id = userId, term_id = termId)
            .executeAsList()
            .map { lesson ->
                val course = db.coursesQueries
                    .getAllCourses()
                    .executeAsList()
                    .firstOrNull { it.id == lesson.course_id }

                val roomName = lesson.room_id?.let { roomId ->
                    db.roomsQueries.getRoomById(roomId).executeAsOneOrNull()?.name
                } ?: "-"

                val teacherName = lesson.teacher_id?.let { teacherId ->
                    db.teachersQueries.getTeacherById(teacherId).executeAsOneOrNull()?.name
                } ?: "-"

                LessonCard(
                    lessonId = lesson.id,
                    dayOfWeek = lesson.day_of_week,
                    code = course?.code?.substringAfter("/") ?: "N/A",
                    title = course?.name ?: "Ismeretlen tantárgy",
                    time = "${lesson.start_time}-${lesson.end_time}",
                    room = roomName,
                    building = buildingNameFromRoom(roomName),
                    teacher = teacherName,
                    lessonType = lesson.lesson_type,
                    groupCode = lesson.group_code,
                    credits = course?.credits ?: 0L,
                    weekType = lesson.week_type,
                    note = lesson.note,
                    validFrom = lesson.valid_from,
                    validTo = lesson.valid_to
                )
            }
            .distinctBy {
                "${it.code}|${it.dayOfWeek}|${it.time}|${it.room}|${it.teacher}|${it.lessonType}|${it.groupCode}|${it.weekType}|${it.validFrom}|${it.validTo}"
            }

        val schedules = buildUpcomingLessonSchedules(
            lessons = lessons,
            leadTime = lessonNotificationLeadTime,
            fromDate = today
        )
        LessonNotificationScheduler.scheduleAll(schedules)
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
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val compactTabLabels = maxWidth <= 390.dp

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == MainTab.HOME,
                                    onClick = { selectedTab = MainTab.HOME },
                                    label = { TabLabel(if (compactTabLabels) "Főlap" else "Kezdőlap") },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = null) }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == MainTab.TIMETABLE,
                                    onClick = { selectedTab = MainTab.TIMETABLE },
                                    label = { TabLabel(if (compactTabLabels) "Órák" else "Órarend") },
                                    icon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == MainTab.REMINDERS,
                                    onClick = { selectedTab = MainTab.REMINDERS },
                                    label = { TabLabel(if (compactTabLabels) "Emlék." else "Emlékeztetők") },
                                    icon = { Icon(Icons.Filled.Notifications, contentDescription = null) }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == MainTab.SETTINGS,
                                    onClick = { selectedTab = MainTab.SETTINGS },
                                    label = { TabLabel(if (compactTabLabels) "Beáll." else "Beállítások") },
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
                                    lessonNotificationLeadTime = lessonNotificationLeadTime,
                                    onLessonNotificationLeadTimeSelected = {
                                        lessonNotificationLeadTime = it
                                        lessonNotificationSettings.saveLeadTime(it)
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
}

@Composable
private fun TabLabel(text: String) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
