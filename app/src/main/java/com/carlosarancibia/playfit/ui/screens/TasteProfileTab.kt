package com.carlosarancibia.playfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductProfile
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.ui.components.TasteRadarChart
import com.carlosarancibia.playfit.ui.components.TraitPillCloud
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

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

@Composable
fun TasteProfileTab(
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
