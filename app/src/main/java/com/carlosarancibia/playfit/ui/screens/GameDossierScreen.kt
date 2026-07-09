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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.ui.components.AlreadyPlayedDialog
import com.carlosarancibia.playfit.ui.components.FeedbackChips
import com.carlosarancibia.playfit.ui.components.MetricCard
import com.carlosarancibia.playfit.ui.components.ReasonList
import com.carlosarancibia.playfit.ui.components.ReasonTone
import com.carlosarancibia.playfit.ui.components.DossierActionBar
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.ShimmerBox
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel
import com.carlosarancibia.playfit.ui.viewmodel.ProductUtils

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GameDossierScreen(
    entry: RankedSeedGame,
    viewModel: PlayfitViewModel,
    onBack: () -> Unit,
    isPicked: Boolean = false,
) {
    var showPlayedDialog by remember { mutableStateOf(false) }
    var showFeedbackChips by remember { mutableStateOf(false) }

    val productState by viewModel.state.collectAsState()
    val uiState by viewModel.ui.collectAsState()

    val gameState = remember(productState.user.gameStates, entry.game.gameId) {
        productState.user.gameStates[entry.game.gameId]
    }
    val ownedPlatformIds = remember(productState.user.onboarding.platforms) {
        productState.user.onboarding.platforms.map { it.platformId }.toSet()
    }
    val gamePlatforms = remember(entry.game.availablePlatformIds, entry.game.availablePlatformNames) {
        entry.game.availablePlatformIds.zip(entry.game.availablePlatformNames)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game Dossier",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                )
            )
        },
        bottomBar = {
            DossierActionBar(
                isPicked = isPicked,
                isSaving = uiState.saving,
                onTogglePick = { viewModel.togglePick(entry.game.gameId) },
                onAlreadyPlayed = { showPlayedDialog = true },
                onNotForMe = {
                    viewModel.applyDecisionFeedback(
                        entry.game.gameId,
                        ProductDecisionFeedback.NotForMe,
                    )
                    showFeedbackChips = true
                },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PlayfitSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.lg),
            ) {

            item {
                PlayfitCoverArt(
                    gameId = entry.game.gameId,
                    title = entry.game.title,
                    coverUrl = entry.game.externalCoverUrl ?: entry.game.coverPath,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.game.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            
                            // Year & Genre Metadata line
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs)
                            ) {
                                val year = entry.game.releaseYear
                                val hasYear = !year.isNullOrBlank() && year != "null"
                                if (hasYear) {
                                    Text(
                                        text = year.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                if (hasYear && entry.game.primaryGenre.isNotBlank()) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    )
                                }
                                if (entry.game.primaryGenre.isNotBlank()) {
                                    Text(
                                        text = entry.game.primaryGenre.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Recommended",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = PlayfitExtendedTheme.colors.playfitAccent,
                            )
                            Text(
                                text = "${ProductUtils.decisionLabel(entry)} \u00B7 ${entry.affinityScore.toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = PlayfitExtendedTheme.colors.playfitAccent,
                                fontWeight = FontWeight.Black,
                            )
                        }
                    }

                    // Game State Badges (Picks, Played status, etc.)
                    val labels = remember(gameState) {
                        buildList {
                            if (gameState?.inPlayfitPicks == true) add("In Playfit Picks" to false)
                            if (gameState?.status != null) add("Status: ${gameState.status.apiValue.replace("_", " ")}" to false)
                            if (gameState?.rating != null) add("Rating: ${gameState.rating}" to false)
                            if (gameState?.excluded == true) add("Skipped for now" to true)
                        }
                    }
                    if (labels.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            labels.forEach { (label, isNegative) ->
                                val border = if (isNegative) {
                                    PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                }
                                val bg = if (isNegative) {
                                    PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                }
                                val textCol = if (isNegative) {
                                    PlayfitExtendedTheme.colors.playfitNegative
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                                Box(
                                    modifier = Modifier
                                        .background(color = bg, shape = MaterialTheme.shapes.small)
                                        .border(width = 1.dp, color = border, shape = MaterialTheme.shapes.small)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textCol
                                    )
                                }
                            }
                        }
                    }

                    // Platform Availability Badges (Owned platform checks)
                    if (gamePlatforms.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "AVAILABLE ON",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
                            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            gamePlatforms.forEach { (id, name) ->
                                val isOwned = ownedPlatformIds.contains(id)
                                val border = if (isOwned) {
                                    PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                }
                                val bg = if (isOwned) {
                                    PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.08f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
                                }
                                val textCol = if (isOwned) {
                                    PlayfitExtendedTheme.colors.playfitAccent
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                }
                                
                                Row(
                                     modifier = Modifier
                                         .background(color = bg, shape = MaterialTheme.shapes.small)
                                         .border(width = 1.dp, color = border, shape = MaterialTheme.shapes.small)
                                         .padding(horizontal = 8.dp, vertical = 4.dp),
                                     verticalAlignment = Alignment.CenterVertically,
                                     horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isOwned) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = textCol,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isOwned) FontWeight.Black else FontWeight.Medium,
                                        color = textCol
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
                ) {
                    MetricCard(
                        label = "Match Affinity",
                        value = ProductUtils.matchQualityLabel(entry.affinityScore),
                        detail = "${entry.affinityScore.toInt()}/100",
                        numericValue = entry.affinityScore,
                        modifier = Modifier.weight(1f),
                        barColor = PlayfitExtendedTheme.colors.playfitAccent,
                    )
                    MetricCard(
                        label = "Watch-out Score",
                        value = ProductUtils.watchOutLabel(entry.riskScore),
                        detail = "${entry.riskScore.toInt()}/100",
                        numericValue = entry.riskScore,
                        modifier = Modifier.weight(1f),
                        barColor = if (entry.riskScore > 45)
                            PlayfitExtendedTheme.colors.playfitNegative
                        else
                            PlayfitExtendedTheme.colors.playfitWarning,
                    )
                    MetricCard(
                        label = "Confidence Read",
                        value = ProductUtils.confidenceLabel(entry.confidence),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                ReasonList(
                    title = "Why it fits",
                    reasons = entry.fitReasons.ifEmpty {
                        listOf("Playfit needs more feedback before making a strong claim.")
                    },
                )
            }

            item {
                ReasonList(
                    title = "Watch-outs",
                    reasons = entry.cautionReasons.ifEmpty {
                        listOf("No major watch-out yet.")
                    },
                    tone = ReasonTone.Warning,
                )
            }

            if (showFeedbackChips) {
                item {
                    FeedbackChips(
                        onSelect = { feedback ->
                            viewModel.applyDecisionFeedback(entry.game.gameId, feedback)
                            showFeedbackChips = false
                        },
                    )
                }
            }

            item {
                Spacer(Modifier.height(96.dp))
            }
        }
    }
}

    if (showPlayedDialog) {
        AlreadyPlayedDialog(
            open = showPlayedDialog,
            onDismiss = { showPlayedDialog = false },
            onSelect = { feedback ->
                viewModel.applyDecisionFeedback(entry.game.gameId, feedback)
                showPlayedDialog = false
            },
        )
    }
}




