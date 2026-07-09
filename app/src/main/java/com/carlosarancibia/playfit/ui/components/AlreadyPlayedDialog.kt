package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

private data class PlayedOption(
    val label: String,
    val feedback: ProductDecisionFeedback,
    val icon: ImageVector,
    val color: Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlreadyPlayedDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onSelect: (ProductDecisionFeedback) -> Unit,
) {
    if (!open) return

    val themeColors = PlayfitExtendedTheme.colors
    val options = remember(themeColors) {
        listOf(
            PlayedOption("Loved it", ProductDecisionFeedback.PlayedLoved, Icons.Default.Favorite, themeColors.playfitPositive),
            PlayedOption("Liked it", ProductDecisionFeedback.PlayedLiked, Icons.Default.ThumbUp, themeColors.playfitIndigo),
            PlayedOption("Mixed", ProductDecisionFeedback.PlayedMixed, Icons.Default.Star, themeColors.playfitWarning),
            PlayedOption("Dropped it", ProductDecisionFeedback.PlayedDropped, Icons.Default.Close, themeColors.playfitNegative),
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
            ) {
                listOf(options.take(2), options.drop(2)).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
                    ) {
                        pair.forEach { option ->
                            OutlinedButton(
                                onClick = {
                                    onSelect(option.feedback)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = null,
                                        tint = option.color,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(PlayfitSpacing.lg))
        }
    }
}
