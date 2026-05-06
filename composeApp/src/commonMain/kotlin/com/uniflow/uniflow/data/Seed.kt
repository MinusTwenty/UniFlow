package com.uniflow.uniflow.data

import com.uniflow.database.UniFlowDatabase

fun seedUser1FirstSemester(db: UniFlowDatabase) {
    val user1 = db.authQueries.getUserByUsername("user1").executeAsOneOrNull() ?: return

    val termName = "1. év / 1. szemeszter"
    val term = db.termsQueries.getTermByName(termName).executeAsOneOrNull()
        ?: run {
            db.termsQueries.insertTerm(termName, "2023-09-11", "2023-12-09")
            db.termsQueries.getTermByName(termName).executeAsOne()
        }

    fun ensureTeacher(name: String) =
        db.teachersQueries.getTeacherByName(name).executeAsOneOrNull()
            ?: run {
                db.teachersQueries.insertTeacher(name)
                db.teachersQueries.getTeacherByName(name).executeAsOne()
            }

    fun ensureRoom(name: String) =
        db.roomsQueries.getRoomByName(name).executeAsOneOrNull()
            ?: run {
                db.roomsQueries.insertRoom(name)
                db.roomsQueries.getRoomByName(name).executeAsOne()
            }

    fun ensureCourse(code: String, name: String, credits: Long = 0L) =
        db.coursesQueries.getCourseByCode(code).executeAsOneOrNull()
            ?: run {
                db.coursesQueries.insertCourse(code, name, credits)
                db.coursesQueries.getCourseByCode(code).executeAsOne()
            }

    val paksi = ensureTeacher("Paksi")
    val marak = ensureTeacher("Marák")
    val vegh = ensureTeacher("Végh")
    val csoka = ensureTeacher("Csóka")
    val arki = ensureTeacher("Árki")
    val molnar = ensureTeacher("Molnár")
    val szalay = ensureTeacher("Szalay")
    val bognar = ensureTeacher("Bognár")
    val vontszemu = ensureTeacher("Vontszemű")
    val kiss = ensureTeacher("Kiss")
    val takac = ensureTeacher("Takác")
    val kato = ensureTeacher("Kató")

    val g312 = ensureRoom("G312")
    val inf01 = ensureRoom("INF01")
    val info3 = ensureRoom("INFO3")
    val k102k2 = ensureRoom("K102-K2")
    val dp002 = ensureRoom("DP002")
    val dp005 = ensureRoom("DP005")
    val k106 = ensureRoom("K106-KINF0")
    val k101k1 = ensureRoom("K101-K1")
    val k103k3 = ensureRoom("K103-K3")

    val mit = ensureCourse("AIdb/MIT", "Anyagok és technológiák informatikusok számára", 0)
    val phw = ensureCourse("AIdb/PHW", "Számítógépes hardver", 0)
    val pr1 = ensureCourse("AIdb/PR1", "Programozás 1 - Algoritmizáció és programozás", 0)
    val spr = ensureCourse("AIdb/SPR", "Programozás szeminárium", 0)
    val udi = ensureCourse("AIdb/UDI", "Bevezetés az informatikába", 0)
    val ma1 = ensureCourse("AIdb/MA1", "Matematika informatikusoknak 1", 0)
    val sport = ensureCourse("VSA1a", "Szabadidős sporttevékenységek 1a", 0)
    val tex = ensureCourse("AIdb/TEX", "Tipográfiai rendszerek és programozásuk", 0)

    fun enroll(courseId: Long) {
        try {
            db.enrollmentQueries.enroll(user1.id, term.id, courseId)
        } catch (_: Throwable) {
        }
    }

    listOf(mit.id, phw.id, pr1.id, spr.id, udi.id, ma1.id, sport.id, tex.id).forEach(::enroll)

    val validFrom = "2023-09-11"
    val validTo = "2023-12-09"

    // Hétfő (1)
    db.lessonsQueries.insertLesson(
        mit.id,
        term.id,
        1,
        "13:15",
        "14:00",
        "EVERY",
        "P",
        "PRACTICE",
        g312.id,
        paksi.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        phw.id,
        term.id,
        1,
        "16:40",
        "17:25",
        "EVERY",
        "C",
        "LECTURE",
        inf01.id,
        marak.id,
        validFrom,
        validTo,
        null
    )

    // Kedd (2)
    db.lessonsQueries.insertLesson(
        pr1.id,
        term.id,
        2,
        "08:00",
        "09:30",
        "EVERY",
        "C",
        "LECTURE",
        info3.id,
        vegh.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        spr.id,
        term.id,
        2,
        "09:40",
        "11:10",
        "EVERY",
        "C",
        "SEMINAR",
        inf01.id,
        csoka.id,
        validFrom,
        validTo,
        null
    )

    // Szerda (3)
    db.lessonsQueries.insertLesson(
        udi.id,
        term.id,
        3,
        "09:40",
        "11:00",
        "EVERY",
        "",
        "OTHER",
        k102k2.id,
        csoka.id,
        validFrom,
        validTo,
        "Egyes alkalmak külön dátumosak lehetnek."
    )
    db.lessonsQueries.insertLesson(
        udi.id,
        term.id,
        3,
        "13:15",
        "14:45",
        "EVERY",
        "S",
        "SEMINAR",
        g312.id,
        csoka.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        ma1.id,
        term.id,
        3,
        "16:50",
        "17:35",
        "EVERY",
        "C",
        "LECTURE",
        dp002.id,
        arki.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        ma1.id,
        term.id,
        3,
        "17:35",
        "18:20",
        "EVERY",
        "S",
        "SEMINAR",
        dp002.id,
        arki.id,
        validFrom,
        validTo,
        null
    )

    // Csütörtök (4)
    db.lessonsQueries.insertLesson(
        phw.id,
        term.id,
        4,
        "08:00",
        "09:30",
        "EVERY",
        "A",
        "LECTURE",
        dp005.id,
        molnar.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        ma1.id,
        term.id,
        4,
        "09:40",
        "11:10",
        "EVERY",
        "P",
        "PRACTICE",
        dp005.id,
        szalay.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        sport.id,
        term.id,
        4,
        "11:20",
        "12:05",
        "EVERY",
        "C",
        "SPORT",
        null,
        bognar.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        tex.id,
        term.id,
        4,
        "13:15",
        "14:45",
        "EVERY",
        "C",
        "LECTURE",
        k106.id,
        vontszemu.id,
        validFrom,
        validTo,
        null
    )
    db.lessonsQueries.insertLesson(
        udi.id,
        term.id,
        4,
        "17:40",
        "18:25",
        "EVERY",
        "P",
        "PRACTICE",
        k101k1.id,
        kiss.id,
        validFrom,
        validTo,
        null
    )

    // Péntek (5)
    db.lessonsQueries.insertLesson(
        mit.id,
        term.id,
        5,
        "13:05",
        "13:50",
        "EVERY",
        "P",
        "PRACTICE",
        k102k2.id,
        takac.id,
        validFrom,
        validTo,
        null
    )

    // PR1 pénteki gyakorlat csak bizonyos dátumokon (occurrences)
    db.lessonsQueries.insertLesson(
        pr1.id,
        term.id,
        5,
        "14:00",
        "17:54",
        "EVERY",
        "P",
        "PRACTICE",
        k103k3.id,
        kato.id,
        validFrom,
        validTo,
        "Csak adott dátumokon (lesson_occurrences)."
    )

    val pr1FridayLessons = db.lessonsQueries.getLessonsForCourseTerm(pr1.id, term.id).executeAsList()
        .filter { it.day_of_week == 5L && it.start_time == "14:00" && it.end_time == "17:54" }

    val pr1Friday = pr1FridayLessons.firstOrNull()
    if (pr1Friday != null) {
        val dates = listOf(
            "2023-09-22",
            "2023-09-29",
            "2023-10-06",
            "2023-10-27",
            "2023-11-24",
            "2023-12-08"
        )

        for (d in dates) {
            try {
                db.lessonOccurrencesQueries.insertOccurrence(pr1Friday.id, d)
            } catch (_: Throwable) {
            }
        }
    }
}