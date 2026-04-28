package com.uniflow.uniflow.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uniflow.uniflow.ui.theme.UniFlowTheme

@Composable
fun TermSwitcher(
    terms: List<AcademicTerm>,
    activeTermId: Long?,
    onTermSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        terms.forEachIndexed { index, term ->
            val selected = term.id == activeTermId
            val shape = RoundedCornerShape(6.dp)

            Text(
                text = "Szemeszter ${index + 1}",
                modifier = Modifier
                    .background(
                        color = if (selected) MaterialTheme.colorScheme.primary else UniFlowTheme.colors.chipBackground,
                        shape = shape
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary else UniFlowTheme.colors.glassBorder,
                        shape = shape
                    )
                    .clickable { onTermSelected(term.id) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    UniFlowTheme.colors.chipText
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}
