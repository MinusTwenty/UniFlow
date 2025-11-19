package com.uniflow.uniflow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.uniflow.uniflow.home.HomeTop
import com.uniflow.uniflow.home.LessonCard
import com.uniflow.uniflow.home.StudentInfo

@Composable
fun App() {
    var isLoggedIn by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!isLoggedIn) {
                // LOGIN SCREEN
                LoginScreenWithValidation(
                    onLoginSuccess = { isLoggedIn = true }
                )
            } else {
                // MAIN SCREEN (HOME)
                HomeTop(
                    student = StudentInfo(
                        uniShort = "UJS",
                        fullName = "Pástó Vilmos Márk",
                        weekType = "" // parity is calculated inside HomeTop
                    ),
                    location = "Tornaterem",
                    building = "B épület",
                    dateText = "2025.09.30.",
                    nextRoom = "G312",
                    nextTeacher = "XY",
                    upcoming = demoLessons(),
                    nowTime = ""
                )
            }
        }
    }
}

// TEMP: demo lessons until API/import
private fun demoLessons(): List<LessonCard> = listOf(
    LessonCard(code = "TOR", time = "12:15-13:00", room = "B-02", teacher = "Tanár: XY"),
    LessonCard(code = "PS1", time = "13:00-13:45", room = "B-03", teacher = "Tanár: XY"),
    LessonCard(code = "OS1", time = "14:15-15:00", room = "C-12", teacher = "Tanár: XY"),
    LessonCard(code = "OSM", time = "15:00-15:55", room = "C-13", teacher = "Tanár: XY")
)
