package com.carlosarancibia.playfit.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.ProductProfile
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

private enum class TasteTab { YourTaste, Activity }

private fun confidencePercent(profile: ProductProfile): Int {
    return when {
        profile.ratedCount >= 20 -> 90
        profile.ratedCount >= 10 -> 78
        profile.ratedCount >= 5 -> 60
        profile.ratedCount >= 3 -> 40
        else -> 20
    }
}

private fun confidenceLabel(profile: ProductProfile): String {
    return when {
        profile.ratedCount >= 20 -> "Strong signal"
        profile.ratedCount >= 5 -> "Building signal"
        else -> "First look"
    }
}

private fun summaryText(profile: ProductProfile): String {
    return when {
        profile.ratedCount >= 5 ->
            "Playfit leans toward your favorites, but still needs more signals to sharpen the edge cases."
        profile.ratedCount >= 3 ->
            "Playfit is still balancing your likes and misses; a few more decisions will make the next pick steadier."
        else ->
            "Add at least 3 liked games and 1 missed game to refine your recommendations."
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasteScreen(
    profile: ProductProfile,
    tasteModel: ProductTasteModel? = null,
    onOpenGame: (String) -> Unit = {},
    onOpenMap: () -> Unit = {},
    onRemovePick: (String) -> Unit = {},
    onChangeSignal: (String, ProductDecisionFeedback) -> Unit = { _, _ -> },
    onDeleteSignal: (String, String) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    isRefreshing: Boolean = false,
) {
    var activeTab by remember { mutableStateOf(TasteTab.YourTaste) }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PlayfitSpacing.md),
        ) {
            Spacer(Modifier.height(PlayfitSpacing.md))
            Text(
                text = "Your Taste",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(PlayfitSpacing.sm))

            Row(
                horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
            ) {
                TasteTab.entries.forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = {
                            Text(
                                text = when (tab) {
                                    TasteTab.YourTaste -> "Your Taste"
                                    TasteTab.Activity -> "Activity"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PlayfitExtendedTheme.colors.playfitAccent,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(PlayfitSpacing.md))

            when (activeTab) {
                TasteTab.YourTaste -> TasteProfileTab(
                    profile = profile,
                    tasteModel = tasteModel,
                    onOpenMap = onOpenMap,
                )
                TasteTab.Activity -> DecisionsActivityContent(
                    tasteModel = tasteModel,
                    onOpenGame = onOpenGame,
                    onRemovePick = onRemovePick,
                    onChangeSignal = onChangeSignal,
                    onDeleteSignal = onDeleteSignal,
                )
            }
        }
    }
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TasteProfileTab(
    profile: ProductProfile,
    tasteModel: ProductTasteModel? = null,
    onOpenMap: () -> Unit = {},
) {
    val confidencePct = confidencePercent(profile)
    val confidenceLbl = confidenceLabel(profile)

    val traits = tasteModel?.mapTraits.orEmpty()
    val lovedTraits = remember(traits) {
        traits.filter { it.positiveCount >= it.negativeCount && (it.positiveCount > 0 || it.strength > 0) }
            .sortedByDescending { it.strength }
    }
    val avoidedTraits = remember(traits) {
        traits.filter { it.negativeCount > it.positiveCount }
            .sortedByDescending { it.strength }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.lg),
    ) {
        Text(
            text = "What Playfit is learning from your active decisions. $confidenceLbl.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PlayfitGlassCard {
            Column {
                Text(
                    text = "TASTE CONFIDENCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(PlayfitSpacing.sm))
                LinearProgressIndicator(
                    progress = { confidencePct / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = PlayfitExtendedTheme.colors.playfitAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(PlayfitSpacing.xs))
                Text(
                    text = "${confidencePct}% confidence based on current signals.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        PlayfitGlassCard {
            Column {
                Text(
                    text = "PROFILE SUMMARY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.12.sp,
                    color = PlayfitExtendedTheme.colors.playfitAccent,
                )
                Spacer(Modifier.height(PlayfitSpacing.sm))
                Text(
                    text = summaryText(profile),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(PlayfitSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    val prefsCount = tasteModel?.evidenceCount ?: profile.ratedCount
                    val likedCount = tasteModel?.positiveCount ?: (profile.likedGenres.size + profile.likedTags.size)
                    val avoidedCount = tasteModel?.negativeCount ?: (profile.avoidedGenres.size + profile.dislikedTags.size)

                    StatItem("Preferences", "$prefsCount")
                    StatItem("Liked", "$likedCount", isPositive = true)
                    StatItem("Avoided", "$avoidedCount", isNegative = true)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                )
                .clickable(onClick = onOpenMap)
                .padding(PlayfitSpacing.md),
        ) {
            Text(
                text = "Interactive Affinity Map \u2192",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PlayfitExtendedTheme.colors.playfitAccent,
            )
        }

        // Programmatic Radar Chart (Gaming Taste DNA)
        if (traits.isNotEmpty()) {
            Text(
                text = "Gaming Taste DNA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            PlayfitGlassCard(modifier = Modifier.fillMaxWidth()) {
                TasteRadarChart(traits = traits)
            }
        }

        // Loved Traits Pill Cloud
        Text(
            text = "Loved Traits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        TraitPillCloud(traits = lovedTraits, isLoved = true)

        // Avoided Traits Pill Cloud
        Text(
            text = "Avoided Traits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        TraitPillCloud(traits = avoidedTraits, isLoved = false)

        Spacer(Modifier.height(PlayfitSpacing.xxl))
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    isPositive: Boolean = false,
    isNegative: Boolean = false,
) {
    val textCol = when {
        isPositive -> PlayfitExtendedTheme.colors.playfitPositive
        isNegative -> PlayfitExtendedTheme.colors.playfitNegative
        else -> PlayfitExtendedTheme.colors.playfitAccent
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = textCol,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TasteRadarChart(
    traits: List<com.carlosarancibia.playfit.model.ProductTasteMapTrait>,
    modifier: Modifier = Modifier
) {
    if (traits.isEmpty()) return

    // Pick top positive (up to 4) and negative (up to 1) traits matching Web memo
    val radarTraits = remember(traits) {
        val posList = traits.filter { it.direction == "positive" && it.strength > 0 }
            .sortedByDescending { it.strength }
        val negList = traits.filter { it.direction == "negative" && it.strength > 0 }
            .sortedByDescending { it.strength }
            
        if (negList.isNotEmpty()) {
            posList.take(4) + negList.take(1)
        } else {
            posList.take(5)
        }
    }

    val n = radarTraits.size
    if (n < 3) return // Radar charts require at least 3 axes

    val maxVal = remember(traits) { traits.maxOfOrNull { it.strength } ?: 1.0 }
    
    val accentColor = PlayfitExtendedTheme.colors.playfitAccent
    val positiveColor = PlayfitExtendedTheme.colors.playfitPositive
    val negativeColor = PlayfitExtendedTheme.colors.playfitNegative
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.height * 0.33f

        // 1. Draw Grid Levels (circular polygon grids)
        val gridLevels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
        gridLevels.forEach { level ->
            val path = androidx.compose.ui.graphics.Path()
            for (i in 0 until n) {
                val angle = (i * 2 * Math.PI) / n - Math.PI / 2
                val currentRadius = level * radius
                val x = center.x + currentRadius * Math.cos(angle).toFloat()
                val y = center.y + currentRadius * Math.sin(angle).toFloat()
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            drawPath(
                path = path,
                color = outlineColor,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw Spokes (axes lines)
        for (i in 0 until n) {
            val angle = (i * 2 * Math.PI) / n - Math.PI / 2
            val outerX = center.x + radius * Math.cos(angle).toFloat()
            val outerY = center.y + radius * Math.sin(angle).toFloat()
            
            drawLine(
                color = outlineColor,
                start = center,
                end = androidx.compose.ui.geometry.Offset(outerX, outerY),
                strokeWidth = 1.dp.toPx()
            )

            // Draw axis text labels slightly further out
            val labelRadius = radius + 20.dp.toPx()
            val labelX = center.x + labelRadius * Math.cos(angle).toFloat()
            val labelY = center.y + labelRadius * Math.sin(angle).toFloat()
            
            val trait = radarTraits[i]
            val labelText = trait.label
            
            val textPaint = android.text.TextPaint().apply {
                color = (if (trait.direction == "positive") positiveColor else negativeColor).toArgb()
                textSize = textStyle.fontSize.toPx()
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
            }
            
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                labelX,
                labelY + 4.dp.toPx(),
                textPaint
            )
        }

        // 3. Draw Radar Value Polygon
        val valuePath = androidx.compose.ui.graphics.Path()
        for (i in 0 until n) {
            val trait = radarTraits[i]
            val angle = (i * 2 * Math.PI) / n - Math.PI / 2
            val valRatio = if (maxVal > 0) trait.strength / maxVal else 0.5
            val currentRadius = valRatio.toFloat() * radius
            val x = center.x + currentRadius * Math.cos(angle).toFloat()
            val y = center.y + currentRadius * Math.sin(angle).toFloat()
            if (i == 0) {
                valuePath.moveTo(x, y)
            } else {
                valuePath.lineTo(x, y)
            }
        }
        valuePath.close()

        drawPath(
            path = valuePath,
            color = accentColor.copy(alpha = 0.22f)
        )
        drawPath(
            path = valuePath,
            color = accentColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 4. Draw Value Dots
        for (i in 0 until n) {
            val trait = radarTraits[i]
            val angle = (i * 2 * Math.PI) / n - Math.PI / 2
            val valRatio = if (maxVal > 0) trait.strength / maxVal else 0.5
            val currentRadius = valRatio.toFloat() * radius
            val x = center.x + currentRadius * Math.cos(angle).toFloat()
            val y = center.y + currentRadius * Math.sin(angle).toFloat()
            
            drawCircle(
                color = if (trait.direction == "positive") positiveColor else negativeColor,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TraitPillCloud(
    traits: List<com.carlosarancibia.playfit.model.ProductTasteMapTrait>,
    isLoved: Boolean,
) {
    if (traits.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(PlayfitSpacing.md),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No signals recorded yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = if (isStrong) 0.3f else 0.15f)
            } else {
                PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = if (isStrong) 0.3f else 0.15f)
            }
            val bgCol = if (isLoved) {
                PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = if (isStrong) 0.12f else 0.06f)
            } else {
                PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = if (isStrong) 0.12f else 0.06f)
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
            
            val size = when {
                isStrong -> 13.sp
                isMedium -> 12.sp
                else -> 11.sp
            }

            Row(
                modifier = Modifier
                    .background(color = bgCol, shape = RoundedCornerShape(20.dp))
                    .border(width = 1.dp, color = borderCol, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = trait.label,
                    style = TextStyle(
                        fontSize = size,
                        fontWeight = weight,
                        color = textCol
                    )
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = textCol.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${trait.strength.toInt()}",
                        style = TextStyle(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = textCol
                        )
                    )
                }
            }
        }
    }
}
