package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// Top header composable (no Zimbra button, no Events button)
@Composable
fun HomeTop(
    student: StudentInfo,
    location: String,
    building: String,
    dateText: String,
    nextRoom: String,
    nextTeacher: String,
    upcoming: List<LessonCard>,
    nowTime: String // "HH:mm"
) {
    val nowMinutes = parseTimeToMinutes(nowTime) ?: 0

    Surface(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Section: Student info
            SectionCard(title = "Hallgató") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.uniShort,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.weight(1f))
                    LangChip("HU")
                }
                Spacer(Modifier.height(8.dp))
                InfoChip(student.weekType)
            }

            Spacer(Modifier.height(12.dp))

            // Section: Location / meta
            SectionCard(title = "Óra adatok") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Hely: $location", style = MaterialTheme.typography.bodyMedium)
                        Text("Épület: $building", style = MaterialTheme.typography.bodyMedium)
                        Text("Dátum: $dateText", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Köv. hely: $nextRoom", style = MaterialTheme.typography.bodyMedium)
                        Text("Tanár: $nextTeacher", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Section: Szünet (only when currently between lessons)
            findCurrentBreak(nowMinutes, upcoming)?.let { br ->
                SectionCard(title = "Szünet") {
                    Text(
                        text = "Hátralévő: ${br.remainingMinutes} perc (köv. óra: ${br.nextLessonCode} ${minutesToHHmm(br.nextStartMinutes)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Section: Lessons
            SectionCard(title = "Következő órák") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statuses = upcoming.map { it to statusFor(it, nowMinutes) }
                    val activeIndex = statuses.indexOfFirst { it.second == LessonStatus.ACTIVE }
                    val nextIndex = statuses.indexOfFirst { it.second == LessonStatus.UPCOMING }
                    val breakMinutes =
                        if (activeIndex >= 0 && nextIndex >= 0 && nextIndex == activeIndex + 1)
                            breakBetween(upcoming[activeIndex], upcoming[nextIndex])
                        else null

                    upcoming.take(4).forEachIndexed { index, lesson ->
                        val status = statuses.getOrNull(index)?.second ?: LessonStatus.PAST
                        val breakToNext = if (index == activeIndex) breakMinutes else null
                        LessonCardView(
                            lesson = lesson,
                            status = status,
                            breakToNextMinutes = breakToNext,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonCardView(lesson: LessonCard, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.heightIn(min = 72.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(lesson.code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(lesson.time, style = MaterialTheme.typography.bodySmall)
            Text(lesson.room, style = MaterialTheme.typography.bodySmall)
            Text(lesson.teacher, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun LessonCardView(
    lesson: LessonCard,
    status: LessonStatus,
    breakToNextMinutes: Int?,
    modifier: Modifier = Modifier
) {
    val stripeColor = when (status) {
        LessonStatus.ACTIVE -> MaterialTheme.colorScheme.primary
        LessonStatus.UPCOMING -> MaterialTheme.colorScheme.secondary
        LessonStatus.PAST -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left color stripe for status
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripeColor)
            )

            // Lesson details
            Column(
                Modifier
                    .padding(10.dp)
                    .weight(1f)
            ) {
                Text(
                    lesson.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))
                Text(lesson.time, style = MaterialTheme.typography.bodySmall)
                Text(lesson.room, style = MaterialTheme.typography.bodySmall)
                Text(
                    lesson.teacher,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (status == LessonStatus.ACTIVE && breakToNextMinutes != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Szünet a következőig: ${breakToNextMinutes} perc",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: LessonStatus, breakToNextMinutes: Int?) {
    val label = when (status) {
        LessonStatus.ACTIVE -> "Aktív"
        LessonStatus.UPCOMING -> {
            if (breakToNextMinutes != null) "Következő • szünet ${breakToNextMinutes}p"
            else "Következő"
        }
        LessonStatus.PAST -> "Lezajlott"
    }
    Surface(
        color = MaterialTheme.colorScheme.inversePrimary,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
