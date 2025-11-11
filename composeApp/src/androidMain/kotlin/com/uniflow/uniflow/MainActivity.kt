package com.uniflow.uniflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.uniflow.uniflow.home.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    HomeTop(
                        student = StudentInfo(
                            uniShort = "UJS",
                            fullName = "Pástó Vilmos Márk",
                            weekType = "Páros hét",
                        ),
                        location = "Tornaterem",
                        building = "B épület",
                        dateText = "2025.09.30.",
                        nextRoom = "G312",
                        nextTeacher = "XY",
                        upcoming = listOf(
                            LessonCard("TOR", "12:15–13:00", "B-02", "Tanár: XY"),
                            LessonCard("PS1", "13:00–13:45", "B-03", "Tanár: XY"),
                            LessonCard("OS1", "14:15–15:00", "C-12", "Tanár: XY"),
                            LessonCard("OSM", "15:05–15:55", "C-13", "Tanár: XY")
                        ),
                        nowTime = "12:30" // test value; show Active on TOR
                    )
                }
            }
        }
    }
}
