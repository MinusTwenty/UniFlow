package com.uniflow.uniflow.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

@Composable
fun HomeTop(
    student: StudentInfo,
    location: String,
    building: String,
    dateText: String,
    nextRoom: String,
    nextTeacher: String,
    upcoming: List<LessonCard>,
    nowTime: String // kept for signature compatibility, not used
) {
    // --- Week calculations ---
    val today = WeekUtil.today()
    val iso = WeekUtil.isoWeekInfo(today)
    val parity = WeekUtil.isoWeekParity(today)
    val acad = WeekUtil.academicWeek(
        date = today,
        semesterStart = LocalDate(2025, 9, 15),
        semesterEnd   = LocalDate(2025, 12, 13)
    )

    // --- Realtime "now" in seconds; updates every second ---
    var nowSec by remember { mutableStateOf(currentSecondsSinceMidnight()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowSec = currentSecondsSinceMidnight()
            delay(1000)
        }
    }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                // Week chips in a single row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip("ISO hét: ${iso.week}")
                    InfoChip(if (parity == WeekUtil.WeekParity.Even) "Páros hét" else "Páratlan hét")
                    acad?.let { InfoChip("Akadémiai hét: $it") }
                }
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

            // Section: Szünet – appears before first lesson and between lessons, counts down
            run {
                val remaining = findBreakRemainingSeconds(nowSec, upcoming)
                if (remaining != null && remaining > 0) {
                    SectionCard(title = "Szünet") {
                        Text(
                            text = "A következő óráig: ${formatHuDurationDynamic(remaining)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Section: Lessons
            SectionCard(title = "Következő órák") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statuses = upcoming.map { it to statusFor(it, nowSec) }
                    val activeIndex = statuses.indexOfFirst { it.second == LessonStatus.ACTIVE }
                    val nextIndex = statuses.indexOfFirst { it.second == LessonStatus.UPCOMING }
                    val breakMinutes =
                        if (activeIndex >= 0 && nextIndex == activeIndex + 1)
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
