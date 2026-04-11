package com.uniflow.uniflow.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
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
    teacher: String,
    dateText: String,
    nextRoom: String,
    nextTeacher: String,
    nextBuilding: String,
    upcoming: List<LessonCard>,
    nowTime: Int,
    currentDate: LocalDate,
    lessonLayoutMode: LessonLayoutMode,
    onLessonLayoutModeChange: (LessonLayoutMode) -> Unit,
    activeTerm: AcademicTerm?,
    availableTerms: List<AcademicTerm>,
    activeTermId: Long?,
    onTermSelected: (Long) -> Unit
) {
    // --- Week calculations ---
    val today = WeekUtil.today()
    val iso = WeekUtil.isoWeekInfo(today)
    val parity = WeekUtil.isoWeekParity(today)

    val acad = activeTerm?.let { term ->
        val start = kotlinx.datetime.LocalDate.parse(term.startDate)
        val end = kotlinx.datetime.LocalDate.parse(term.endDate)

        WeekUtil.academicWeek(
            date = today,
            semesterStart = start,
            semesterEnd = end
        )
    }

    val nowSec = nowTime



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp)
    ) {
        // Hallgató
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

            // Hetek
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
                GlassChip(text = "Dátum: $dateText")
            }
        }

        Spacer(Modifier.height(12.dp))

        //Óra adatok
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ){
                        Text("Épület: $building", color = UniFlowTheme.colors.textPrimary)
                        Text("Hely: $location", color = UniFlowTheme.colors.textPrimary)
                        Text("Tanár: $teacher", color = UniFlowTheme.colors.textPrimary)
                    }

                    Spacer(Modifier.width(20.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Köv. épület: $nextBuilding", color = UniFlowTheme.colors.textPrimary)
                        Text("Köv. hely: $nextRoom", color = UniFlowTheme.colors.textPrimary)
                        Text("Köv. tanár: $nextTeacher", color = UniFlowTheme.colors.textPrimary)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        SectionCard(title = "Szemeszter") {
            if (availableTerms.isEmpty()) {
                Text(
                    text = "Nincs elérhető szemeszter.",
                    color = UniFlowTheme.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                TermSwitcher(
                    terms = availableTerms,
                    activeTermId = activeTermId,
                    onTermSelected = onTermSelected
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        //Szünet
        run {
            val breakRemaining = findBreakRemainingSeconds(nowSec, upcoming)
            val activeRemaining = findActiveLessonRemainingSeconds(nowSec, upcoming)

            when {
                activeRemaining != null && activeRemaining > 0 -> {
                    SectionCard(title = "Aktuális óra") {
                        Text(
                            text = "Hátralévő idő az órából: ${formatHuDurationDynamic(activeRemaining)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                breakRemaining != null && breakRemaining > 0 -> {
                    SectionCard(title = "Szünet") {
                        Text(
                            text = "A következő óráig: ${formatHuDurationDynamic(breakRemaining)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }

            // Következő órák
            UniFlowGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Következő órák",
                            style = MaterialTheme.typography.headlineSmall,
                            color = UniFlowTheme.colors.textPrimary
                        )

                        LessonLayoutToggle(
                            selectedMode = lessonLayoutMode,
                            onModeChange = onLessonLayoutModeChange
                        )
                    }

                    val statuses = upcoming.map { it to statusFor(it, nowSec) }
                    val activeIndex = statuses.indexOfFirst { it.second == LessonStatus.ACTIVE }
                    val nextIndex = statuses.indexOfFirst { it.second == LessonStatus.UPCOMING }
                    val breakMinutes =
                        if (activeIndex >= 0 && nextIndex == activeIndex + 1)
                            breakBetween(upcoming[activeIndex], upcoming[nextIndex])
                        else null

                    if (upcoming.isEmpty()) {
                        Text(
                            text = "Erre a napra nincs óra.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UniFlowTheme.colors.textSecondary
                        )
                    } else {
                        when (lessonLayoutMode) {
                            LessonLayoutMode.HORIZONTAL -> {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(upcoming) { index, lesson ->
                                        val status = statuses.getOrNull(index)?.second ?: LessonStatus.PAST
                                        val breakToNext = if (index == activeIndex) breakMinutes else null

                                        LessonCardView(
                                            lesson = lesson,
                                            status = status,
                                            breakToNextMinutes = breakToNext,
                                            modifier = Modifier.width(200.dp)
                                        )
                                    }
                                }
                            }

                            LessonLayoutMode.VERTICAL -> {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    upcoming.forEachIndexed { index, lesson ->
                                        val status = statuses.getOrNull(index)?.second ?: LessonStatus.PAST
                                        val breakToNext = if (index == activeIndex) breakMinutes else null

                                        LessonCardView(
                                            lesson = lesson,
                                            status = status,
                                            breakToNextMinutes = breakToNext,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
