package com.uniflow.uniflow.data.seed.semesters

import com.uniflow.uniflow.data.seed.DemoAcademicCalendar
import com.uniflow.uniflow.data.seed.model.DemoLessonSeed
import com.uniflow.uniflow.data.seed.model.DemoSemesterSeed

val user3Semester1Seed = DemoSemesterSeed(
    username = "222",
    termName = "3. emberke / 1. szemeszter",
    termStart = DemoAcademicCalendar.AUTUMN_START,
    termEnd = DemoAcademicCalendar.AUTUMN_END,
    lessons = listOf(
        // Kedd (2)
        DemoLessonSeed(
            courseCode = "VSA3a",
            courseName = "Szabadidős sporttevékenységek 3a",
            credits = 0,
            dayOfWeek = 2,
            startTime = "11:20",
            endTime = "12:05",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "SPORT",
            roomName = "FITN",
            teacherName = "Židek",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PSI",
            courseName = "Számítógépes hálózatok",
            credits = 0,
            dayOfWeek = 2,
            startTime = "12:15",
            endTime = "13:00",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "G313",
            teacherName = "Kocsis",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/OS1",
            courseName = "Operációs rendszerek 1",
            credits = 0,
            dayOfWeek = 2,
            startTime = "14:15",
            endTime = "15:45",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Várkonyiné Kóczy",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/OS1",
            courseName = "Operációs rendszerek 1",
            credits = 0,
            dayOfWeek = 2,
            startTime = "16:45",
            endTime = "17:30",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "DP001",
            teacherName = "Gubo",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),

        // Szerda (3)
        DemoLessonSeed(
            courseCode = "AIdb/PAP",
            courseName = "Párhuzamos programozás",
            credits = 0,
            dayOfWeek = 3,
            startTime = "13:00",
            endTime = "13:45",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Szénási",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PSI",
            courseName = "Számítógépes hálózatok",
            credits = 0,
            dayOfWeek = 3,
            startTime = "14:00",
            endTime = "15:30",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Takác",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PGG",
            courseName = "Számítógépes geometria és grafika",
            credits = 0,
            dayOfWeek = 3,
            startTime = "15:40",
            endTime = "17:10",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Takác",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),

        // Csütörtök (4)
        DemoLessonSeed(
            courseCode = "AIdb/PAP",
            courseName = "Párhuzamos programozás",
            credits = 0,
            dayOfWeek = 4,
            startTime = "08:50",
            endTime = "10:20",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "G313",
            teacherName = "Szénási",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END,
            note = "(+1) jelöléssel szerepelt az órarendben."
        ),
        DemoLessonSeed(
            courseCode = "AIdb/IOT",
            courseName = "Bevezetés az IoT rendszerekbe és felhő alapú számításokba",
            credits = 0,
            dayOfWeek = 4,
            startTime = "10:30",
            endTime = "11:15",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF01",
            teacherName = "Felde",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/IOT",
            courseName = "Bevezetés az IoT rendszerekbe és felhő alapú számításokba",
            credits = 0,
            dayOfWeek = 4,
            startTime = "11:20",
            endTime = "12:05",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "INF01",
            teacherName = "Felde",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/UIS",
            courseName = "Bevezetés az intelligens paradigmákba",
            credits = 0,
            dayOfWeek = 4,
            startTime = "13:40",
            endTime = "15:10",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Molnár",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PGG",
            courseName = "Számítógépes geometria és grafika",
            credits = 0,
            dayOfWeek = 4,
            startTime = "16:50",
            endTime = "17:35",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K106-KINF0",
            teacherName = "Takác",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),

        // Péntek (5)
        DemoLessonSeed(
            courseCode = "AIdb/UIS",
            courseName = "Bevezetés az intelligens paradigmákba",
            credits = 0,
            dayOfWeek = 5,
            startTime = "08:50",
            endTime = "09:35",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INFO3",
            teacherName = "Molnár",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        ),
        DemoLessonSeed(
            courseCode = "PHMb/FGR",
            courseName = "Pénzügyi intelligencia",
            credits = 0,
            dayOfWeek = 5,
            startTime = "13:15",
            endTime = "14:00",
            weekType = "EVERY",
            groupCode = "S",
            lessonType = "SEMINAR",
            roomName = "G309",
            teacherName = "Antalík",
            validFrom = DemoAcademicCalendar.AUTUMN_START,
            validTo = DemoAcademicCalendar.AUTUMN_END
        )
    )
)