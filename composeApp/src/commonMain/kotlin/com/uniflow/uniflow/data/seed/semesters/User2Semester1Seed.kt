package com.uniflow.uniflow.data.seed.semesters

import com.uniflow.uniflow.data.seed.DemoAcademicCalendar
import com.uniflow.uniflow.data.seed.model.DemoLessonSeed
import com.uniflow.uniflow.data.seed.model.DemoSemesterSeed

val user2Semester1Seed = DemoSemesterSeed(
    username = "111",
    termName = "2. emberke / 1. szemeszter",
    termStart = DemoAcademicCalendar.AUTUMN_START,
    termEnd = DemoAcademicCalendar.AUTUMN_END,
    lessons = listOf(
        // Hétfő (1)
        DemoLessonSeed(
            courseCode = "AIdb/PR3",
            courseName = "Programozás 3 - Programozás Windows OR alatt",
            credits = 0,
            dayOfWeek = 1,
            startTime = "08:50",
            endTime = "09:35",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Végh",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/UMS",
            courseName = "Bevezetés a modellezés és szimulációba",
            credits = 0,
            dayOfWeek = 1,
            startTime = "11:20",
            endTime = "12:50",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INFO3",
            teacherName = "Annus",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),

        // Kedd (2)
        DemoLessonSeed(
            courseCode = "AIdb/PR3",
            courseName = "Programozás 3 - Programozás Windows OR alatt",
            credits = 0,
            dayOfWeek = 2,
            startTime = "08:00",
            endTime = "09:30",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K106-KINF0",
            teacherName = "Végh",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PPY",
            courseName = "Programozás Python nyelven",
            credits = 0,
            dayOfWeek = 2,
            startTime = "09:40",
            endTime = "11:10",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INFO3",
            teacherName = "Csóka",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/UMS",
            courseName = "Bevezetés a modellezés és szimulációba",
            credits = 0,
            dayOfWeek = 2,
            startTime = "12:15",
            endTime = "13:45",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Kmeť",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MA3",
            courseName = "Matematika informatikusoknak 3",
            credits = 0,
            dayOfWeek = 2,
            startTime = "15:10",
            endTime = "15:55",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Filip",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),

        // Szerda (3)
        DemoLessonSeed(
            courseCode = "AIdb/TWS",
            courseName = "Weboldalak készítése",
            credits = 0,
            dayOfWeek = 3,
            startTime = "08:50",
            endTime = "09:35",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Szénási",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/TWS",
            courseName = "Weboldalak készítése",
            credits = 0,
            dayOfWeek = 3,
            startTime = "11:20",
            endTime = "12:50",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K106-KINF0",
            teacherName = "Paksi",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MA3",
            courseName = "Matematika informatikusoknak 3",
            credits = 0,
            dayOfWeek = 3,
            startTime = "15:00",
            endTime = "16:30",
            weekType = "EVERY",
            groupCode = "S",
            lessonType = "SEMINAR",
            roomName = "DP002",
            teacherName = "Svitek",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "VSA2a",
            courseName = "Szabadidős sporttevékenységek 2a",
            credits = 0,
            dayOfWeek = 3,
            startTime = "17:30",
            endTime = "18:15",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "SPORT",
            roomName = "FITN",
            teacherName = "Židek",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),

        // Csütörtök (4)
        DemoLessonSeed(
            courseCode = "AIdb/GED",
            courseName = "Számítógépes grafika - Grafikus editorok",
            credits = 0,
            dayOfWeek = 4,
            startTime = "09:40",
            endTime = "11:10",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF01",
            teacherName = "Gubo",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MAN",
            courseName = "Emberi erőforrás menedzsment",
            credits = 0,
            dayOfWeek = 4,
            startTime = "15:00",
            endTime = "18:00",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K103-K3",
            teacherName = "Karácsony",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        )
    )
)