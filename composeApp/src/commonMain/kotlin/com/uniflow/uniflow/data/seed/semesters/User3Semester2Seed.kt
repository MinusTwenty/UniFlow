package com.uniflow.uniflow.data.seed.semesters

import com.uniflow.uniflow.data.seed.DemoAcademicCalendar
import com.uniflow.uniflow.data.seed.model.DemoLessonSeed
import com.uniflow.uniflow.data.seed.model.DemoSemesterSeed

val user3Semester2Seed = DemoSemesterSeed(
    username = "222",
    termName = "3. emberke / 2. szemeszter",
    termStart = DemoAcademicCalendar.SPRING_START,
    termEnd = DemoAcademicCalendar.SPRING_END,
    lessons = listOf(
        // Kedd (2)
        DemoLessonSeed(
            courseCode = "AIdb/OS2",
            courseName = "Operációs rendszerek 2",
            credits = 0,
            dayOfWeek = 2,
            startTime = "14:15",
            endTime = "15:45",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Várkonyiné Kóczy",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/OS2",
            courseName = "Operációs rendszerek 2",
            credits = 0,
            dayOfWeek = 2,
            startTime = "16:50",
            endTime = "17:35",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K102-K2",
            teacherName = "Gubo",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),

        // Szerda (3)
        DemoLessonSeed(
            courseCode = "AIdb/INB",
            courseName = "Informatikai biztonság",
            credits = 0,
            dayOfWeek = 3,
            startTime = "14:15",
            endTime = "15:45",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K106-KINF0",
            teacherName = "Annus",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "AIdb/INB",
            courseName = "Informatikai biztonság",
            credits = 0,
            dayOfWeek = 3,
            startTime = "15:50",
            endTime = "16:35",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K106-KINF0",
            teacherName = "Annus",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        ),
        DemoLessonSeed(
            courseCode = "VSA3b",
            courseName = "Szabadidős sporttevékenységek 3b",
            credits = 0,
            dayOfWeek = 3,
            startTime = "16:00",
            endTime = "16:45",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "SPORT",
            roomName = "FITN",
            teacherName = "Židek",
            validFrom = DemoAcademicCalendar.SPRING_START,
            validTo = DemoAcademicCalendar.SPRING_END
        )
    )
)