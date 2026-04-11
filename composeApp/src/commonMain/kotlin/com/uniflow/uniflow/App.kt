package com.uniflow.uniflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.uniflow.uniflow.home.LessonStatus
import com.uniflow.uniflow.home.statusFor
import com.uniflow.uniflow.home.StudentInfo
import com.uniflow.uniflow.home.WeekUtil.today
import com.uniflow.uniflow.home.currentSecondsSinceMidnight
import com.uniflow.uniflow.settings.ThemeSettings
import com.uniflow.uniflow.ui.settings.SettingsScreen
import com.uniflow.uniflow.ui.theme.UniFlowAppTheme
import com.uniflow.uniflow.ui.theme.UniFlowBackground
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

private enum class MainTab {
    HOME,
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

    val allTerms = remember {
        db.termsQueries.getAllTerms().executeAsList()
    }

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
                                label = { Text("Főoldal") },
                                icon = {}
                            )
                            NavigationBarItem(
                                selected = selectedTab == MainTab.SETTINGS,
                                onClick = { selectedTab = MainTab.SETTINGS },
                                label = { Text("Beállítások") },
                                icon = {}
                            )
                        }
                    }
                ) { innerPadding ->
                    when (selectedTab) {
                        MainTab.HOME -> {
                            val upcomingLessons = if (loggedInUserId != null && activeTerm != null) {
                                loadTodayLessons(
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

                            val statuses = upcomingLessons.map { it to statusFor(it, nowSec) }

                            HomeTop(
                                modifier = Modifier.padding(innerPadding),
                                demoStudentInfo(loggedInUserId),
                                location = currentLesson?.room ?: "-",
                                building = buildingNameFromRoom(currentLesson?.room),
                                dateText = todayDisplayText(),
                                teacher = currentLesson?.teacher?.removePrefix("Tanár: ") ?: "-",
                                nextRoom = nextLesson?.room ?: "-",
                                nextTeacher = nextLesson?.teacher?.removePrefix("Tanár: ") ?: "-",
                                nextBuilding = buildingNameFromRoom(nextLesson?.room),
                                upcoming = upcomingLessons,
                                nowTime = nowSec,
                                currentDate = today,
                                lessonLayoutMode = lessonLayoutMode,
                                onLessonLayoutModeChange = { lessonLayoutMode = it },
                                activeTerm = activeTerm,
                                availableTerms = availableTerms,
                                activeTermId = activeTermId,
                                onTermSelected = { activeTermId = it }
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

private fun loadTodayLessons(
    db: com.uniflow.database.UniFlowDatabase,
    userId: Long,
    termId: Long,
    dayOfWeek: Long
): List<LessonCard> {
    return db.lessonsQueries
        .getUserLessonsForDay(
            user_id = userId,
            term_id = termId,
            day_of_week = dayOfWeek
        )
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
                code = course?.code?.substringAfter("/") ?: "N/A",
                time = "${lesson.start_time}-${lesson.end_time}",
                room = roomName,
                teacher = "Tanár: $teacherName",
                lessonType = lesson.lesson_type,
                groupCode = lesson.group_code
            )
        }
        .distinctBy {
            "${it.code}|${it.time}|${it.room}|${it.teacher}|${it.lessonType}|${it.groupCode}"
        }
}

private fun buildingNameFromRoom(room: String?): String {
    if (room.isNullOrBlank()) return "-"

    val normalized = room.trim().uppercase()

    return when {
        normalized == "FITN" -> "Tornaterem"

        normalized.startsWith("DP") -> "Tiszti pav."
        normalized.startsWith("INFO") -> "Tiszti pav."
        normalized.startsWith("INF") -> "Tiszti pav."

        normalized.startsWith("K") -> "Konferencia "
        normalized.startsWith("G") -> "GIK épület"

        else -> "-"
    }
}