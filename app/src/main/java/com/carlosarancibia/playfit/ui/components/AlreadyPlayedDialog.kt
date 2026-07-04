package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing

private val options = listOf(
    PlayedOption("Loved it", ProductDecisionFeedback.PlayedLoved, "\u2665"),
    PlayedOption("Liked it", ProductDecisionFeedback.PlayedLiked, "\uD83D\uDC4D"),
    PlayedOption("Mixed", ProductDecisionFeedback.PlayedMixed, "\u2248"),
    PlayedOption("Dropped it", ProductDecisionFeedback.PlayedDropped, "\uD83D\uDC4E"),
)

private data class PlayedOption(
    val label: String,
    val feedback: ProductDecisionFeedback,
    val icon: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlreadyPlayedDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onSelect: (ProductDecisionFeedback) -> Unit,
) {
    if (!open) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PlayfitSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ALREADY PLAYED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            Text(
                text = "How did it land?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(PlayfitSpacing.xs))
            Text(
                text = "Let us know how your experience was. This helps refine your future recommendations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = PlayfitSpacing.md),
            )
            Spacer(Modifier.height(PlayfitSpacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
            ) {
                options.forEach { option ->
                    OutlinedButton(
                        onClick = {
                            onSelect(option.feedback)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = option.icon,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(PlayfitSpacing.lg))
        }
    }
}
