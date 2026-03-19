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
import com.uniflow.uniflow.ui.theme.GlassChip
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

@Composable
fun HomeTop(
    modifier: Modifier = Modifier,
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
                        fontWeight = FontWeight.Bold,
                        color = UniFlowTheme.colors.textPrimary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = UniFlowTheme.colors.textPrimary
                    )
                    Spacer(Modifier.weight(1f))
                    LangChip("HU")
                }

                // Week chips in a single row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassChip(text = "ISO hét: ${iso.week}")
                    GlassChip(
                        text = if (parity == WeekUtil.WeekParity.Even) "Páros hét" else "Páratlan hét"
                    )
                    acad?.let {
                        GlassChip(text = "Akadémiai hét: $it")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Section: Location / meta
            UniFlowGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Óra adatok",
                        style = MaterialTheme.typography.headlineSmall,
                        color = UniFlowTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Hely: $location", color = UniFlowTheme.colors.textPrimary)
                            Text("Épület: $building", color = UniFlowTheme.colors.textPrimary)
                            Text("Dátum: $dateText", color = UniFlowTheme.colors.textPrimary)
                        }

                        Column {
                            Text("Köv. hely: $nextRoom", color = UniFlowTheme.colors.textPrimary)
                            Text("Tanár: $nextTeacher", color = UniFlowTheme.colors.textPrimary)
                        }
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
