package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

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
            // left status stripe
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripeColor)
            )

            // lesson details
            Column(
                modifier = Modifier
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

                // show only when active AND there is positive break time until the next lesson
                if (status == LessonStatus.ACTIVE && breakToNextMinutes != null && breakToNextMinutes > 0) {
                    Spacer(Modifier.height(4.dp))
                    val hours = breakToNextMinutes / 60
                    val minutes = breakToNextMinutes % 60
                    val timeText = buildString {
                        append("A következő óráig: ")
                        if (hours > 0) append("${hours}ó ")
                        append("${minutes}p")
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
