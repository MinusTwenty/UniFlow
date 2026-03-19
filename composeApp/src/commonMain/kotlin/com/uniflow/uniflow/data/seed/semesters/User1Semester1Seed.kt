package com.uniflow.uniflow.data.seed.semesters

import com.uniflow.uniflow.data.seed.model.DemoLessonSeed
import com.uniflow.uniflow.data.seed.model.DemoSemesterSeed

val user1Semester1Seed = DemoSemesterSeed(
    username = "134288",
    termName = "1. év / 1. szemeszter",
    termStart = "2023-09-11",
    termEnd = "2023-12-09",
    lessons = listOf(
        // Hétfő (1)
        DemoLessonSeed(
            courseCode = "AIdb/MIT",
            courseName = "Anyagok és technológiák informatikusok számára",
            credits = 0,
            dayOfWeek = 1,
            startTime = "13:15",
            endTime = "14:00",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "G312",
            teacherName = "Paksi",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PHW",
            courseName = "Számítógépes hardver",
            credits = 0,
            dayOfWeek = 1,
            startTime = "16:40",
            endTime = "17:25",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INF01",
            teacherName = "Marák",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),

        // Kedd (2)
        DemoLessonSeed(
            courseCode = "AIdb/PR1",
            courseName = "Programozás 1 - Algoritmizáció és programozás",
            credits = 0,
            dayOfWeek = 2,
            startTime = "08:00",
            endTime = "09:30",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "INFO3",
            teacherName = "Végh",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/SPR",
            courseName = "Programozás szeminárium",
            credits = 0,
            dayOfWeek = 2,
            startTime = "09:40",
            endTime = "11:10",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "SEMINAR",
            roomName = "INF01",
            teacherName = "Csóka",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),

        // Szerda (3)
        DemoLessonSeed(
            courseCode = "AIdb/UDI",
            courseName = "Bevezetés az informatikába",
            credits = 0,
            dayOfWeek = 3,
            startTime = "09:40",
            endTime = "11:00",
            weekType = "EVERY",
            groupCode = "",
            lessonType = "OTHER",
            roomName = "K102-K2",
            teacherName = "Csóka",
            validFrom = "2023-09-11",
            validTo = "2023-12-09",
            note = "Egyes alkalmak külön dátumosak lehetnek."
        ),
        DemoLessonSeed(
            courseCode = "AIdb/UDI",
            courseName = "Bevezetés az informatikába",
            credits = 0,
            dayOfWeek = 3,
            startTime = "13:15",
            endTime = "14:45",
            weekType = "EVERY",
            groupCode = "S",
            lessonType = "SEMINAR",
            roomName = "G312",
            teacherName = "Csóka",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MA1",
            courseName = "Matematika informatikusoknak 1",
            credits = 0,
            dayOfWeek = 3,
            startTime = "16:50",
            endTime = "17:35",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "DP002",
            teacherName = "Árki",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MA1",
            courseName = "Matematika informatikusoknak 1",
            credits = 0,
            dayOfWeek = 3,
            startTime = "17:35",
            endTime = "18:20",
            weekType = "EVERY",
            groupCode = "S",
            lessonType = "SEMINAR",
            roomName = "DP002",
            teacherName = "Árki",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),

        // Csütörtök (4)
        DemoLessonSeed(
            courseCode = "AIdb/PHW",
            courseName = "Számítógépes hardver",
            credits = 0,
            dayOfWeek = 4,
            startTime = "08:00",
            endTime = "09:30",
            weekType = "EVERY",
            groupCode = "A",
            lessonType = "LECTURE",
            roomName = "DP005",
            teacherName = "Molnár",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/MA1",
            courseName = "Matematika informatikusoknak 1",
            credits = 0,
            dayOfWeek = 4,
            startTime = "09:40",
            endTime = "11:10",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "DP005",
            teacherName = "Szalay",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "VSA1a",
            courseName = "Szabadidős sporttevékenységek 1a",
            credits = 0,
            dayOfWeek = 4,
            startTime = "11:20",
            endTime = "12:05",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "SPORT",
            roomName = null,
            teacherName = "Bognár",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/TEX",
            courseName = "Tipográfiai rendszerek és programozásuk",
            credits = 0,
            dayOfWeek = 4,
            startTime = "13:15",
            endTime = "14:45",
            weekType = "EVERY",
            groupCode = "C",
            lessonType = "LECTURE",
            roomName = "K106-KINF0",
            teacherName = "Vontszemű",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/UDI",
            courseName = "Bevezetés az informatikába",
            credits = 0,
            dayOfWeek = 4,
            startTime = "17:40",
            endTime = "18:25",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K101-K1",
            teacherName = "Kiss",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),

        // Péntek (5)
        DemoLessonSeed(
            courseCode = "AIdb/MIT",
            courseName = "Anyagok és technológiák informatikusok számára",
            credits = 0,
            dayOfWeek = 5,
            startTime = "13:05",
            endTime = "13:50",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K102-K2",
            teacherName = "Takác",
            validFrom = "2023-09-11",
            validTo = "2023-12-09"
        ),
        DemoLessonSeed(
            courseCode = "AIdb/PR1",
            courseName = "Programozás 1 - Algoritmizáció és programozás",
            credits = 0,
            dayOfWeek = 5,
            startTime = "14:00",
            endTime = "17:54",
            weekType = "EVERY",
            groupCode = "P",
            lessonType = "PRACTICE",
            roomName = "K103-K3",
            teacherName = "Kató",
            validFrom = "2023-09-11",
            validTo = "2023-12-09",
            note = "Csak adott dátumokon (lesson_occurrences).",
            occurrenceDates = listOf(
                "2023-09-22",
                "2023-09-29",
                "2023-10-06",
                "2023-10-27",
                "2023-11-24",
                "2023-12-08"
            )
        )
    )
)