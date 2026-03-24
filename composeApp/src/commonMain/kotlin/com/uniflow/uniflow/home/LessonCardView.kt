package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun LessonCardView(
    lesson: LessonCard,
    status: LessonStatus,
    breakToNextMinutes: Int?,
    modifier: Modifier = Modifier
) {
    val stripeColor = when (status) {
        LessonStatus.ACTIVE -> UniFlowTheme.colors.accent
        LessonStatus.UPCOMING -> UniFlowTheme.colors.warning
        LessonStatus.PAST -> UniFlowTheme.colors.divider
    }

    UniFlowGlassCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripeColor)
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = lesson.code,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = UniFlowTheme.colors.textPrimary
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = lesson.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = UniFlowTheme.colors.textSecondary
                )

                Text(
                    text = lesson.room,
                    style = MaterialTheme.typography.bodyMedium,
                    color = UniFlowTheme.colors.textSecondary
                )

                Text(
                    text = lesson.teacher,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = UniFlowTheme.colors.textSecondary
                )

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
                        color = UniFlowTheme.colors.accent
                    )
                }
            }
        }
    }
}