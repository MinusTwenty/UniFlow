package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun LessonCardView(
    lesson: LessonCard,
    status: LessonStatus,
    breakToNextMinutes: Int?,
    modifier: Modifier = Modifier
) {
    val colors = UniFlowTheme.colors
    val shape = RoundedCornerShape(24.dp)

    val typeColor = lessonTypeColor(
        lessonType = lesson.lessonType,
        defaultText = colors.textPrimary
    )

    val (cardBackground, cardBorder, contentAlpha, elevation) = when (status) {
        LessonStatus.UPCOMING -> CardVisualState(
            background = colors.glassSurface,
            border = colors.glassBorder,
            alpha = 1f,
            elevation = 4.dp
        )

        LessonStatus.PAST -> CardVisualState(
            background = desaturateColor(colors.glassSurface, amount = 0.60f).copy(alpha = 0.64f),
            border = colors.divider.copy(alpha = 0.65f),
            alpha = 0.62f,
            elevation = 0.dp
        )

        LessonStatus.ACTIVE -> CardVisualState(
            background = colors.glassSurface,
            border = typeColor.copy(alpha = 0.95f),
            alpha = 1f,
            elevation = 10.dp
        )
    }

    val stripeShape = RoundedCornerShape(
        topStart = 24.dp,
        bottomStart = 24.dp,
        topEnd = 6.dp,
        bottomEnd = 6.dp
    )

    Row(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(cardBackground)
            .border(
                width = if (status == LessonStatus.ACTIVE) 1.5.dp else 1.dp,
                color = cardBorder,
                shape = shape
            )
            .graphicsLayer(alpha = contentAlpha)
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .fillMaxHeight()
                .clip(stripeShape)
                .background(typeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = lessonTypeLabel(lesson.lessonType),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (lesson.lessonType == "SEMINAR") Color.Black else Color.White,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.graphicsLayer(rotationZ = -90f)
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = lesson.code,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colors.textPrimary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = lesson.time,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Text(
                text = lesson.room,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Text(
                text = lesson.teacher,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = colors.textSecondary
            )

            if (status == LessonStatus.ACTIVE && breakToNextMinutes != null && breakToNextMinutes > 0) {
                Spacer(Modifier.height(6.dp))

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
                    color = typeColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
private data class CardVisualState(
    val background: Color,
    val border: Color,
    val alpha: Float,
    val elevation: androidx.compose.ui.unit.Dp
)

private fun lessonTypeLabel(lessonType: String): String {
    return when (lessonType) {
        "LECTURE" -> "ELŐAD"
        "PRACTICE" -> "GYAK"
        "SEMINAR" -> "SZEM"
        "SPORT" -> "SPORT"
        else -> "EGYÉB"
    }
}

private fun lessonTypeColor(
    lessonType: String,
    defaultText: Color
): Color {
    return when (lessonType) {
        "LECTURE" -> Color(0xFF2E7D32)   // zöld
        "PRACTICE" -> Color(0xFF8BC34A)  // világos zöld
        "SEMINAR" -> Color(0xFFF5F5F5)   // fehér / törtfehér
        "SPORT" -> Color(0xFF4FC3F7)     // külön sport szín
        else -> defaultText.copy(alpha = 0.75f)
    }
}

private fun desaturateColor(color: Color, amount: Float): Color {
    val gray = (color.red + color.green + color.blue) / 3f

    val r = color.red + (gray - color.red) * amount
    val g = color.green + (gray - color.green) * amount
    val b = color.blue + (gray - color.blue) * amount

    return Color(r, g, b, color.alpha)
}