package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val reasonOptions = listOf(
    "Wrong mood" to com.carlosarancibia.playfit.model.ProductDecisionFeedback.NotForMe,
    "Too long" to com.carlosarancibia.playfit.model.ProductDecisionFeedback.NotForMe,
    "Too hard" to com.carlosarancibia.playfit.model.ProductDecisionFeedback.NotForMe,
    "Not my genre" to com.carlosarancibia.playfit.model.ProductDecisionFeedback.NotForMe,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackChips(
    onSelect: (com.carlosarancibia.playfit.model.ProductDecisionFeedback) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "What got in the way?",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                reasonOptions.forEach { (label, feedback) ->
                    FilterChip(
                        selected = false,
                        onClick = { onSelect(feedback) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        ),
                    )
                }
            }
        }
    }
}
