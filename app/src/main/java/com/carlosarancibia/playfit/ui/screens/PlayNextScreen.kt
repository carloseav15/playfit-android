package com.carlosarancibia.playfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.ui.components.AlreadyPlayedDialog
import com.carlosarancibia.playfit.ui.components.FeedbackChips
import com.carlosarancibia.playfit.ui.components.ReasonList
import com.carlosarancibia.playfit.ui.components.ReasonTone
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import androidx.compose.runtime.collectAsState
import com.carlosarancibia.playfit.ui.components.design.ScoreBadge
import com.carlosarancibia.playfit.ui.components.design.ShimmerBox
import com.carlosarancibia.playfit.ui.components.design.ShimmerCard
import com.carlosarancibia.playfit.ui.components.design.SignalSummaryBar
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitUiState
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel
import com.carlosarancibia.playfit.ui.viewmodel.ProductUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayNextScreen(
    viewModel: PlayfitViewModel,
    uiState: PlayfitUiState,
    onOpenGame: (String) -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val modelState = viewModel.playNext.collectAsState()
    val model = modelState.value
    val productState by viewModel.state.collectAsState()
    val profile = productState.user.profile
    val excludedIds by viewModel.excludedIds.collectAsState()

    if (uiState.loading && model == null) {
        PlayNextLoading()
        return
    }

    if (uiState.error != null && model == null) {
        PlayNextError(onRetry = { viewModel.refreshRecommendations() })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        PullToRefreshBox(
            isRefreshing = uiState.refreshing,
            onRefresh = { viewModel.refreshRecommendations() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PlayfitSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.lg),
            ) {
                item {
                    Spacer(Modifier.height(PlayfitSpacing.lg))
                    Text(
                        text = "Play Next",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Find what to play next, save promising picks, and keep the reasons visible. Only games available on your selected platforms are suggested.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (profile != null) {
                        Spacer(Modifier.height(PlayfitSpacing.sm))
                        SignalSummaryBar(
                            preferencesCount = profile.signals.size,
                            likedCount = profile.likedGenres.size + profile.likedTags.size,
                            avoidedCount = profile.avoidedGenres.size + profile.dislikedTags.size,
                        )
                    }
                    if (uiState.pendingSync) {
                        Spacer(Modifier.height(PlayfitSpacing.sm))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "\u2939",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "Changes saved on this device; waiting to sync",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (model == null || (model.primary == null && model.alternatives.isNullOrEmpty())) {
                    item {
                        PlayNextEmpty(
                            excludedCount = excludedIds.size,
                            onShowSkipped = { viewModel.clearSkipped() },
                            onOpenSettings = onOpenSettings,
                            hasPlatforms = !productState.user.onboarding.platforms.isNullOrEmpty(),
                        )
                    }
                }

                model?.primary?.let { primary ->
                    item {
                        PrimaryRecommendationCard(
                            entry = primary,
                            isPicked = primary.game.gameId in (model.savedPickIds),
                            onAddPick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.togglePick(primary.game.gameId)
                            },
                            onNotForMe = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.applyDecisionFeedback(
                                    primary.game.gameId,
                                    ProductDecisionFeedback.NotForMe,
                                )
                            },
                            onAlreadyPlayed = { feedback ->
                                viewModel.applyDecisionFeedback(primary.game.gameId, feedback)
                            },
                            onShowAnotherOption = {
                                viewModel.skipRecommendation(primary.game.gameId)
                            },
                            onOpenDetail = { onOpenGame(primary.game.gameId) },
                            onFeedback = { feedback ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.applyDecisionFeedback(primary.game.gameId, feedback)
                            },
                        )
                    }
                }

                if (!model?.alternatives.isNullOrEmpty()) {
                    item {
                        Text(
                            text = "Also worth considering",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Other potential candidates matching your preferences.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                model?.alternatives?.let { alts ->
                    item {
                        PlayfitGlassCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                alts.forEachIndexed { index, alt ->
                                    AlternativeRow(
                                        entry = alt,
                                        onClick = { onOpenGame(alt.game.gameId) }
                                    )
                                    if (index < alts.lastIndex) {
                                        androidx.compose.material3.HorizontalDivider(
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(horizontal = PlayfitSpacing.md)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(PlayfitSpacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun PlayNextLoading() {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PlayfitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
        ) {
            item {
                Spacer(Modifier.height(PlayfitSpacing.lg))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(28.dp),
                )
                Spacer(Modifier.height(8.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                )
                Spacer(Modifier.height(PlayfitSpacing.md))
            }
            items(3) {
                ShimmerCard()
            }
        }
    }
}

@Composable
private fun PlayNextError(onRetry: () -> Unit) {
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
                text = "Play Next could not load",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            Text(
                text = "The catalog connection failed. Please try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(PlayfitSpacing.lg))
            Button(onClick = onRetry) {
                Text("Try again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlayNextRefreshingCard() {
    PlayfitGlassCard {
        Column(modifier = Modifier.padding(PlayfitSpacing.md)) {
            Text(
                text = "Refreshing recommendations...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(PlayfitSpacing.xs))
            Text(
                text = "Your action is being saved. Playfit is finding the next candidate in the background.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlayNextEmpty(
    excludedCount: Int = 0,
    onShowSkipped: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    hasPlatforms: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PlayfitSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No games to recommend yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = if (hasPlatforms)
                "Try adding more platforms or rating more games so we can find a recommendation."
            else
                "Select your gaming platforms so we can find recommendations for you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(PlayfitSpacing.md))
        Button(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PlayfitExtendedTheme.colors.playfitAccent,
            ),
        ) {
            Text(
                text = if (hasPlatforms) "Manage Platforms" else "Add Platforms",
                fontWeight = FontWeight.Bold,
            )
        }

        if (excludedCount > 0) {
            Spacer(Modifier.height(PlayfitSpacing.md))
            Text(
                text = "All current candidates were skipped in this session.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        RoundedCornerShape(10.dp),
                    )
                    .padding(10.dp),
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            OutlinedButton(onClick = onShowSkipped) {
                Text("Show skipped again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PrimaryRecommendationCard(
    entry: RankedSeedGame,
    isPicked: Boolean,
    onAddPick: () -> Unit,
    onNotForMe: () -> Unit,
    onAlreadyPlayed: (ProductDecisionFeedback) -> Unit,
    onShowAnotherOption: () -> Unit,
    onOpenDetail: () -> Unit,
    onFeedback: (ProductDecisionFeedback) -> Unit = {},
) {
    var showPlayedDialog by remember { mutableStateOf(false) }
    var showFeedbackChips by remember { mutableStateOf(false) }

    PlayfitGlassCard {
        Column(verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md)) {
            // Cover art - full width, portrait ratio (matches iOS layout)
            PlayfitCoverArt(
                gameId = entry.game.gameId,
                title = entry.game.title,
                coverUrl = entry.game.externalCoverUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f),
            )

            // Title row + ScoreBadge (iOS: HStack with title left, badge right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Play this next",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = entry.game.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                ScoreBadge(score = entry.affinityScore / 100.0)
            }

            // Match label + "See analysis" link (iOS: DecisionLabelBadge + NavigationLink)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = ProductUtils.matchQualityLabel(entry.affinityScore),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PlayfitExtendedTheme.colors.playfitPositive,
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onOpenDetail) {
                    Text(
                        text = "See analysis",
                        fontWeight = FontWeight.Bold,
                        color = PlayfitExtendedTheme.colors.playfitAccent,
                    )
                }
            }

            // Fit reasons (stacked vertically, not side-by-side — matches iOS)
            if (entry.fitReasons.isNotEmpty()) {
                ReasonList(
                    title = "Why this fits",
                    reasons = entry.fitReasons,
                )
            }

            // Caution reasons
            val cautions = entry.cautionReasons.take(4)
            if (cautions.isNotEmpty()) {
                ReasonList(
                    title = "Watch-outs",
                    reasons = cautions,
                    tone = ReasonTone.Warning,
                )
            }

            // Add to Picks button
            Button(
                onClick = onAddPick,
                enabled = !isPicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PlayfitExtendedTheme.colors.playfitAccent,
                ),
            ) {
                Text(
                    text = if (isPicked) "Saved to Picks" else "Add to Playfit Picks",
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            // Feedback chips (inline in card, matches iOS showReasonPicker)
            if (showFeedbackChips) {
                FeedbackChips(
                    onSelect = { feedback ->
                        onFeedback(feedback)
                        showFeedbackChips = false
                    },
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
            ) {
                OutlinedButton(
                    onClick = { showPlayedDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Already Played", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        onNotForMe()
                        showFeedbackChips = true
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("No, skip this", fontWeight = FontWeight.Bold)
                }
            }

            TextButton(
                onClick = onShowAnotherOption,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Show me another option",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (showPlayedDialog) {
        AlreadyPlayedDialog(
            open = showPlayedDialog,
            onDismiss = { showPlayedDialog = false },
            onSelect = { feedback ->
                onAlreadyPlayed(feedback)
                showPlayedDialog = false
            },
        )
    }
}

@Composable
private fun AlternativeRow(
    entry: RankedSeedGame,
    onClick: () -> Unit,
) {
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
            coverUrl = entry.game.externalCoverUrl,
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
                        color = PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = PlayfitExtendedTheme.colors.playfitPositive.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
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
            
            // Programmatic vector chevron
            androidx.compose.foundation.Canvas(modifier = Modifier.size(16.dp)) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * 0.35f, size.height * 0.2f)
                    lineTo(size.width * 0.65f, size.height * 0.5f)
                    lineTo(size.width * 0.35f, size.height * 0.8f)
                }
                drawPath(
                    path = path,
                    color = Color.Gray.copy(alpha = 0.7f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}
