package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.components.design.PlayfitOpacities

@Composable
fun AlternativeRow(
    entry: RankedSeedGame,
    onClick: () -> Unit,
) {
    val chevronColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PlayfitSpacing.md, horizontal = PlayfitSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
    ) {
        PlayfitCoverArt(
            gameId = entry.game.gameId,
            title = entry.game.title,
            coverUrl = entry.game.externalCoverUrl ?: entry.game.coverPath,
            modifier = Modifier
                .width(44.dp)
                .aspectRatio(0.67f),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.game.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (entry.game.primaryGenre.isNotBlank()) {
                Text(
                    text = entry.game.primaryGenre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = PlayfitOpacities.low),
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        width = 1.dp,
                        color = PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = PlayfitOpacities.muted),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${entry.affinityScore.toInt()}% Match",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = PlayfitExtendedTheme.colors.playfitPositive,
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View details",
                modifier = Modifier.size(16.dp),
                tint = chevronColor
            )
        }
    }
}
