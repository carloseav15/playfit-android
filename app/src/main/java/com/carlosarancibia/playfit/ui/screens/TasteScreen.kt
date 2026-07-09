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
import androidx.compose.material3.Button
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
import com.carlosarancibia.playfit.ui.components.TasteRadarChart
import com.carlosarancibia.playfit.ui.components.TraitPillCloud
import com.carlosarancibia.playfit.ui.screens.TasteProfileTab

private enum class TasteTab { YourTaste, Activity }



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasteScreen(
    profile: ProductProfile,
    tasteModel: ProductTasteModel? = null,
    hasProfile: Boolean = true,
    isLoading: Boolean = false,
    error: String? = null,
    showingStaleData: Boolean = false,
    pendingSync: Boolean = false,
    onOpenGame: (String) -> Unit = {},
    onOpenMap: () -> Unit = {},
    onRemovePick: (String) -> Unit = {},
    onChangeSignal: (String, ProductDecisionFeedback) -> Unit = { _, _ -> },
    onDeleteSignal: (String, String) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    isRefreshing: Boolean = false,
) {
    var activeTab by remember { mutableStateOf(TasteTab.YourTaste) }

    if (isLoading && !hasProfile && tasteModel == null) {
        TasteBlockingState(
            title = "Loading your taste...",
            message = "Hydrating your profile and decision history.",
            onRefresh = onRefresh,
        )
        return
    }

    if (!hasProfile && error != null) {
        TasteBlockingState(
            title = "Taste could not load",
            message = error,
            onRefresh = onRefresh,
        )
        return
    }

    if (!hasProfile) {
        TasteBlockingState(
            title = "Set up your taste first",
            message = "Select your platforms and a few games so Playfit can build your taste map.",
            onRefresh = onRefresh,
        )
        return
    }

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

            if (pendingSync || showingStaleData || error != null) {
                TasteStatusBanner(
                    message = when {
                        pendingSync -> "Changes saved on this device; waiting to sync."
                        error != null -> "Showing available taste data. ${error}"
                        else -> "Showing saved taste data; pull to refresh."
                    },
                )
                Spacer(Modifier.height(PlayfitSpacing.md))
            }

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

@Composable
private fun TasteBlockingState(
    title: String,
    message: String,
    onRefresh: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PlayfitSpacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(PlayfitSpacing.lg))
            Button(onClick = onRefresh) {
                Text("Try again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TasteStatusBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(PlayfitSpacing.sm),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}






