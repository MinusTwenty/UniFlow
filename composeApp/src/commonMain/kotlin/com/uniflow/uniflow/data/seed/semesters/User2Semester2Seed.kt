package com.uniflow.uniflow.data.seed.semesters

import com.uniflow.uniflow.data.seed.DemoAcademicCalendar
import com.uniflow.uniflow.data.seed.model.DemoLessonSeed
import com.uniflow.uniflow.data.seed.model.DemoSemesterSeed

val user2Semester2Seed = DemoSemesterSeed(
    username = "111",
    termName = "2. emberke / 2. szemeszter",
    termStart = DemoAcademicCalendar.SPRING_START,
    termEnd = DemoAcademicCalendar.SPRING_END,
    lessons = listOf(
        // Hétfő (1)
        DemoLessonSeed(
            courseCode = "AIdb/APO",
            courseName = "Számítógépes architektúrák",
            credits = 0,
            dayOfWeek = 1,
            startTime = "09:40",
            endTime = "10:25",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF03",
            teacherName = "Annus",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PR4",
            courseName = "Programozás 4 - Objektum-orientált programozás",
            credits = 0,
            dayOfWeek = 1,
            startTime = "14:00",
            endTime = "15:30",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "G313",
            teacherName = "Kiss",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Kedd (2)
        DemoLessonSeed(
            courseCode = "AIdb/ROB",
            courseName = "Robotika",
            credits = 0,
            dayOfWeek = 2,
            startTime = "08:00",
            endTime = "09:30",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF02",
            teacherName = "Paksi",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Szerda (3)
        DemoLessonSeed(
            courseCode = "AIdb/PR4",
            courseName = "Programozás 4 - Objektum-orientált programozás",
            credits = 0,
            dayOfWeek = 3,
            startTime = "09:40",
            endTime = "10:25",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Szénási",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "VSA2b",
            courseName = "Szabadidős sporttevékenységek 2b",
            credits = 0,
            dayOfWeek = 3,
            startTime = "10:30",
            endTime = "11:15",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "SPORT",
            roomName = "FITN",
            teacherName = "Židek",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/DBA",
            courseName = "Adatbázis alkalmazások fejlesztése",
            credits = 0,
            dayOfWeek = 3,
            startTime = "13:15",
            endTime = "14:45",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF02",
            teacherName = "Marák",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/DM2",
            courseName = "Diszkrét matematika 2 - Gráfalgoritmusok",
            credits = 0,
            dayOfWeek = 3,
            startTime = "15:00",
            endTime = "16:30",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "DP005",
            teacherName = "Árki",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Csütörtök (4)
        DemoLessonSeed(
            courseCode = "AIdb/DM2",
            courseName = "Diszkrét matematika 2 - Gráfalgoritmusok",
            credits = 0,
            dayOfWeek = 4,
            startTime = "13:15",
            endTime = "14:45",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Szalay",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/APO",
            courseName = "Számítógépes architektúrák",
            credits = 0,
            dayOfWeek = 4,
            startTime = "14:55",
            endTime = "16:25",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Molnár",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/HOP",
            courseName = "Gazdasági jog",
            credits = 0,
            dayOfWeek = 4,
            startTime = "15:55",
            endTime = "18:55",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Katona",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END,
            note = "N.T jelöléssel szerepelt az órarendben."
        ),
        DemoLessonSeed(
            courseCode = "AIdb/DBS",
            courseName = "Adatbázis információs rendszerek",
            credits = 0,
            dayOfWeek = 4,
            startTime = "16:30",
            endTime = "17:15",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Kiss",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/DBS",
            courseName = "Adatbázis információs rendszerek",
            credits = 0,
            dayOfWeek = 4,
            startTime = "17:25",
            endTime = "18:55",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF03",
            teacherName = "Kiss",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        )
    )
)