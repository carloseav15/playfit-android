package com.carlosarancibia.playfit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.PlatformPreset
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.fallbackPlatforms
import com.carlosarancibia.playfit.model.familyDisplayName
import com.carlosarancibia.playfit.model.platformPresets
import com.carlosarancibia.playfit.model.sortedPlatformFamilies
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

@Composable
fun PlatformStep(
    selectedIds: Set<String>,
    platforms: List<Platform>,
    onTogglePreset: (PlatformPreset) -> Unit,
    onCustomize: () -> Unit,
) {
    Column {
        Text(
            text = "Where do you play?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = "We will only recommend games available on your active platforms.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.lg))

        Text(
            text = "QUICK GROUPS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.sm))

        // Grid of presets (2-column column of rows)
        val presets = platformPresets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in presets.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformPresetCard(
                        preset = presets[i],
                        selectedIds = selectedIds,
                        platforms = platforms,
                        onClick = { onTogglePreset(presets[i]) },
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < presets.size) {
                        PlatformPresetCard(
                            preset = presets[i + 1],
                            selectedIds = selectedIds,
                            platforms = platforms,
                            onClick = { onTogglePreset(presets[i + 1]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(PlayfitSpacing.lg))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onCustomize) {
                Text(
                    text = "Customize Platforms...",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformPresetCard(
    preset: PlatformPreset,
    selectedIds: Set<String>,
    platforms: List<Platform>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presetPlatforms = platforms.filter(preset.match)
    val presetIds = presetPlatforms.map { it.platformId }.toSet()
    val selectedCount = presetIds.count { it in selectedIds }
    val isSelected = presetIds.isNotEmpty() && selectedCount == presetIds.size
    val isPartiallySelected = selectedCount > 0 && !isSelected

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        isPartiallySelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPartiallySelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }
    val tintColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when (preset.id) {
        "pc" -> Icons.Filled.Computer
        "retro" -> Icons.Filled.Tv
        else -> Icons.Filled.VideogameAsset
    }

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
        ),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PlayfitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = tintColor,
            )

            Column(verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs)) {
                Text(
                    text = preset.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = preset.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val statusLabel = when {
                isSelected -> "Selected"
                isPartiallySelected -> "$selectedCount of ${presetIds.size}"
                else -> "${presetIds.size} systems"
            }
            val statusColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant

            Text(
                text = statusLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
            )
        }
    }
}

@Composable
fun LovedGamesStep(
    likedGames: List<SeedGame?>,
    onSlotClick: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column {
        Text(
            text = "Pick three games you loved",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = "Start with games that clicked. We will look for similar games.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0..2) {
                val game = likedGames.getOrNull(i)
                GameSlotCard(
                    game = game,
                    indexLabel = "Select ${i + 1}",
                    isLiked = true,
                    onClick = { onSlotClick(i) },
                    onRemove = { onRemove(i) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MissedGameStep(
    dislikedGame: SeedGame?,
    onSlotClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Text(
            text = "Pick one game that wasn't for you",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = "Tell us a popular game you didn't enjoy so we know what to avoid.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.lg))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            GameSlotCard(
                game = dislikedGame,
                indexLabel = "Select Game",
                isLiked = false,
                onClick = onSlotClick,
                onRemove = onRemove,
                modifier = Modifier.fillMaxWidth(0.45f)
            )
        }
    }
}

@Composable
fun GameSlotCard(
    game: SeedGame?,
    indexLabel: String,
    isLiked: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isLiked) PlayfitExtendedTheme.colors.playfitAccent
    else PlayfitExtendedTheme.colors.playfitNegative
    val cardBgColor = if (isLiked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    else PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.05f)
    val dashColor = if (isLiked) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    else PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.25f)

    if (game != null) {
        Box(
            modifier = modifier
                .aspectRatio(0.72f)
                .clip(MaterialTheme.shapes.small)
                .clickable { onClick() }
        ) {
            val coverUrl = game.externalCoverUrl ?: game.coverPath
            PlayfitCoverArt(
                gameId = game.gameId,
                title = game.title,
                coverUrl = coverUrl.ifBlank { null },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.scrim.copy(alpha = 0.75f)),
                            startY = 100f
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = game.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove ${game.title}",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    } else {
        // Empty slot with premium dashed border drawn via pathEffect
        Box(
            modifier = modifier
                .aspectRatio(0.72f)
                .clip(MaterialTheme.shapes.small)
                .background(cardBgColor)
                .clickable { onClick() }
                .drawBehind {
                    val stroke = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                    drawRoundRect(
                        color = dashColor,
                        style = stroke,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isLiked) PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.12f)
                            else PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = indexLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
