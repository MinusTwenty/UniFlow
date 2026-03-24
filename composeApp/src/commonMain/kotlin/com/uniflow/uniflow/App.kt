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
import com.uniflow.uniflow.home.HomeTop
import com.uniflow.uniflow.home.LessonCard
import com.uniflow.uniflow.home.LessonLayoutMode
import com.uniflow.uniflow.home.StudentInfo
import com.uniflow.uniflow.home.WeekUtil.today
import com.uniflow.uniflow.settings.ThemeSettings
import com.uniflow.uniflow.ui.settings.SettingsScreen
import com.uniflow.uniflow.ui.theme.UniFlowAppTheme
import com.uniflow.uniflow.ui.theme.UniFlowBackground
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

private enum class MainTab {
    HOME,
    SETTINGS
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

    val db = remember { provideDatabase(databaseDriverFactory) }
    val activeTermId = 1L
    val selectedDayOfWeek = 4L
    val authRepository = remember { DbAuthRepository(db) }

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
                            val upcomingLessons = loggedInUserId?.let { userId ->
                                loadTodayLessons(
                                    db = db,
                                    userId = userId,
                                    termId = activeTermId,
                                    dayOfWeek = selectedDayOfWeek
                                )
                            } ?: emptyList()

                            val nextLesson = upcomingLessons.firstOrNull()

                            HomeTop(
                                modifier = Modifier.padding(innerPadding),
                                student = StudentInfo(
                                    uniShort = "UJS",
                                    fullName = "Pástó Vilmos Márk",
                                    weekType = ""
                                ),
                                location = nextLesson?.room ?: "-",
                                building = "-",
                                dateText = "Teszt nap",
                                nextRoom = nextLesson?.room ?: "-",
                                nextTeacher = nextLesson?.teacher?.removePrefix("Tanár: ") ?: "-",
                                upcoming = upcomingLessons,
                                nowTime = "",
                                lessonLayoutMode = lessonLayoutMode,
                                onLessonLayoutModeChange = { lessonLayoutMode = it }
                            )
                        }

                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                modifier = Modifier.padding(innerPadding),
                                selectedTheme = selectedTheme,
                                onThemeSelected = {
                                    selectedTheme = it
                                    themeSettings.saveTheme(it)
                                }
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
                code = course?.code ?: "N/A",
                time = "${lesson.start_time}-${lesson.end_time}",
                room = roomName,
                teacher = "Tanár: $teacherName"
            )
        }
        .distinctBy { "${it.code}|${it.time}|${it.room}|${it.teacher}" }
}