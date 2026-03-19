package com.uniflow.uniflow.data.seed.model

data class DemoSemesterSeed(
    val username: String,
    val termName: String,
    val termStart: String,
    val termEnd: String,
    val lessons: List<DemoLessonSeed>
)