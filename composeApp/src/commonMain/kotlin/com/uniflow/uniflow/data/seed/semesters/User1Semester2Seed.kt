package com.uniflow.uniflow.data.seed.semesters

import com.uniflow.uniflow.data.seed.DemoAcademicCalendar
import com.uniflow.uniflow.data.seed.model.DemoLessonSeed
import com.uniflow.uniflow.data.seed.model.DemoSemesterSeed

val user1Semester2Seed = DemoSemesterSeed(
    username = "134288",
    termName = "1. emberke / 2. szemeszter",
    termStart = DemoAcademicCalendar.SPRING_START,
    termEnd = DemoAcademicCalendar.SPRING_END,
    lessons = listOf(
        // Hétfő (1)
        DemoLessonSeed(
            courseCode = "AIdb/TMA",
            courseName = "Multimediális alkalmazások fejlesztése",
            credits = 0,
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "09:30",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF01",
            teacherName = "Kiss",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/TAP",
            courseName = "Táblázatkezelő rendszerek és azok programozása",
            credits = 0,
            dayOfWeek = 1,
            startTime = "09:40",
            endTime = "11:10",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF02",
            teacherName = "Csóka",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Kedd (2)
        DemoLessonSeed(
            courseCode = "AIdb/PR2",
            courseName = "Programozás 2 - Programozás és adatstruktúrák",
            credits = 0,
            dayOfWeek = 2,
            startTime = "11:20",
            endTime = "12:50",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF01",
            teacherName = "Csóka",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/TEI",
            courseName = "Elméleti informatika",
            credits = 0,
            dayOfWeek = 2,
            startTime = "15:10",
            endTime = "16:40",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K106-KINF0",
            teacherName = "Gubo",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Szerda (3)
        DemoLessonSeed(
            courseCode = "AIdb/DM1",
            courseName = "Diszkrét matematika 1 - halmazelmélet, kombinatorika, Boole-algebra",
            credits = 0,
            dayOfWeek = 3,
            startTime = "11:20",
            endTime = "12:50",
            weekType = "EVERY",
            groupCode = "S",
            lessonType = "SEMINAR",
            roomName = "K007-szem3",
            teacherName = "Svitek",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MA2",
            courseName = "Matematika informatikusoknak 2",
            credits = 0,
            dayOfWeek = 3,
            startTime = "14:15",
            endTime = "15:00",
            weekType = "EVERY",
            groupCode = "S",
            lessonType = "SEMINAR",
            roomName = "DP001",
            teacherName = "Bukor",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "VSA1b",
            courseName = "Szabadidős sporttevékenységek 1b",
            credits = 0,
            dayOfWeek = 3,
            startTime = "15:10",
            endTime = "15:55",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "SPORT",
            roomName = "FITN",
            teacherName = "Zidek",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Csütörtök (4)
        DemoLessonSeed(
            courseCode = "AIdb/MA2",
            courseName = "Matematika informatikusoknak 2",
            credits = 0,
            dayOfWeek = 4,
            startTime = "10:30",
            endTime = "12:00",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K103-K3",
            teacherName = "Bukor",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MA2",
            courseName = "Matematika informatikusoknak 2",
            credits = 0,
            dayOfWeek = 4,
            startTime = "12:15",
            endTime = "13:00",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K103-K3",
            teacherName = "Bukor",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/TEI",
            courseName = "Elméleti informatika",
            credits = 0,
            dayOfWeek = 4,
            startTime = "13:15",
            endTime = "14:00",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K103-K3",
            teacherName = "Bukor",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/DM1",
            courseName = "Diszkrét matematika 1 - halmazelmélet, kombinatorika, Boole-algebra",
            credits = 0,
            dayOfWeek = 4,
            startTime = "14:15",
            endTime = "15:00",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K103-K3",
            teacherName = "Szalay",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Péntek (5)
        DemoLessonSeed(
            courseCode = "AIdb/PR2",
            courseName = "Programozás 2 - Programozás és adatstruktúrák",
            credits = 0,
            dayOfWeek = 5,
            startTime = "08:00",
            endTime = "09:57",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Kató",
            validFrom = "2024-02-19",
            validTo = "2024-05-18",
            note = "Csak adott dátumokon (lesson_occurrences).",
            occurrenceDates = listOf(
                "2026-02-23",
                "2026-03-01",
                "2026-03-22",
                "2026-04-19",
                "2026-05-10"
            )
        )
    )
)