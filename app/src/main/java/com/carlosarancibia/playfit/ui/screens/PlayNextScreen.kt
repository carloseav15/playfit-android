package com.carlosarancibia.playfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
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
import com.carlosarancibia.playfit.ui.components.PrimaryRecommendationCard
import com.carlosarancibia.playfit.ui.components.AlternativeRow
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
    val selectedPlatformIds by viewModel.selectedPlatformIds.collectAsState()
    var showSlowLoading by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loading, model) {
        showSlowLoading = false
        if (uiState.loading && model == null) {
            kotlinx.coroutines.delay(2500)
            showSlowLoading = true
        }
    }

    if (uiState.loading && model == null) {
        PlayNextLoading(showSlowLoading = showSlowLoading)
        return
    }

    if (uiState.error != null && model == null) {
        PlayNextError(onRetry = { viewModel.refreshRecommendations() })
        return
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Play Next",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                )
            )
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
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

                    if (uiState.refreshing && model?.primary == null) {
                        item {
                            PlayNextRefreshingCard()
                        }
                    } else if (model == null || (model.primary == null && model.alternatives.isNullOrEmpty())) {
                        item {
                            PlayNextEmpty(
                                excludedCount = excludedIds.size,
                                onShowSkipped = { viewModel.clearSkipped() },
                                onOpenSettings = onOpenSettings,
                                hasPlatforms = selectedPlatformIds.isNotEmpty() || !productState.user.onboarding.platforms.isNullOrEmpty(),
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
}






