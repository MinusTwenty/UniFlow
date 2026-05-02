package com.uniflow.uniflow.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.uniflow.uniflow.ui.theme.UniFlowGlassCard
import com.uniflow.uniflow.ui.theme.UniFlowTheme
import kotlin.math.roundToInt

@Composable
fun LessonQuickMenuOverlay(
    anchor: QuickMenuAnchor,
    onDismiss: () -> Unit,
    onAddNote: () -> Unit,
    onAddReminder: () -> Unit,
    onAddFile: () -> Unit
) {
    val density = LocalDensity.current
    val gapPx = remember(density) {
        (anchor.bounds.height * 1.5).roundToInt()
    }
    val popupPositionProvider = remember(anchor.bounds, gapPx) {
        QuickMenuPopupPositionProvider(
            bounds = anchor.bounds,
            gapPx = gapPx
        )
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(140)) + expandIn(
                expandFrom = Alignment.BottomEnd,
                animationSpec = tween(180)
            ),
            exit = fadeOut(animationSpec = tween(120)) + scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(120)
            )
        ) {
            UniFlowGlassCard {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    QuickMenuItem(
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                                contentDescription = null,
                                tint = UniFlowTheme.colors.textPrimary
                            )
                        },
                        text = "Jegyzet",
                        onClick = onAddNote
                    )

                    QuickMenuItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AlarmAdd,
                                contentDescription = null,
                                tint = UniFlowTheme.colors.textPrimary
                            )
                        },
                        text = "Emlékeztető",
                        onClick = onAddReminder
                    )

                    QuickMenuItem(
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AttachFile,
                                contentDescription = null,
                                tint = UniFlowTheme.colors.textPrimary
                            )
                        },
                        text = "Fájl",
                        onClick = onAddFile
                    )
                }
            }
        }
    }
}

private class QuickMenuPopupPositionProvider(
    private val bounds: Rect,
    private val gapPx: Int
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val desiredX = bounds.right.roundToInt() - popupContentSize.width
        val desiredY = bounds.top.roundToInt() - popupContentSize.height - gapPx

        val clampedX = desiredX.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val clampedY = desiredY.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))

        return IntOffset(clampedX, clampedY)
    }
}

@Composable
private fun QuickMenuItem(
    icon: @Composable () -> Unit,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .wrapContentWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = text,
            color = UniFlowTheme.colors.textPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
