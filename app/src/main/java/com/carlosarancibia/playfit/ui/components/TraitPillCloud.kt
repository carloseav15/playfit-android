package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductTasteMapTrait
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.components.design.PlayfitOpacities

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TraitPillCloud(
    traits: List<ProductTasteMapTrait>,
    isLoved: Boolean,
) {
    if (traits.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = PlayfitOpacities.light),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(PlayfitSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No signals recorded yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.strong)
            )
        }
        return
    }

    val maxStrength = remember(traits) { traits.maxOfOrNull { it.strength } ?: 1.0 }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
        modifier = Modifier.fillMaxWidth()
    ) {
        traits.forEach { trait ->
            val ratio = if (maxStrength > 0) trait.strength / maxStrength else 0.5
            val isStrong = ratio >= 0.6
            val isMedium = ratio >= 0.3 && ratio < 0.6
            
            val borderCol = if (isLoved) {
                PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = if (isStrong) PlayfitOpacities.medium else PlayfitOpacities.light)
            } else {
                PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = if (isStrong) PlayfitOpacities.medium else PlayfitOpacities.light)
            }
            val bgCol = if (isLoved) {
                PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = if (isStrong) PlayfitOpacities.soft else PlayfitOpacities.faint)
            } else {
                PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = if (isStrong) PlayfitOpacities.soft else PlayfitOpacities.faint)
            }
            val textCol = if (isLoved) {
                PlayfitExtendedTheme.colors.playfitPositive
            } else {
                PlayfitExtendedTheme.colors.playfitNegative
            }
            
            val weight = when {
                isStrong -> FontWeight.ExtraBold
                isMedium -> FontWeight.Bold
                else -> FontWeight.Normal
            }
            
            val style = when {
                isStrong -> MaterialTheme.typography.bodyMedium
                isMedium -> MaterialTheme.typography.bodySmall
                else -> MaterialTheme.typography.labelSmall
            }

            Row(
                modifier = Modifier
                    .background(color = bgCol, shape = MaterialTheme.shapes.extraLarge)
                    .border(width = 1.dp, color = borderCol, shape = MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = trait.label,
                    style = style.copy(
                        fontWeight = weight,
                        color = textCol
                    )
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = textCol.copy(alpha = PlayfitOpacities.low),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${trait.strength.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textCol
                        )
                    )
                }
            }
        }
    }
}
