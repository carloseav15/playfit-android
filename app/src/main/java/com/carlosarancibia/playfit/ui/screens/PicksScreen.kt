package com.carlosarancibia.playfit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.ui.components.AlreadyPlayedDialog
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel
import com.carlosarancibia.playfit.ui.viewmodel.ProductUtils

@Composable
fun PicksScreen(
    picks: List<RankedSeedGame>,
    viewModel: PlayfitViewModel,
    onOpenGame: (String) -> Unit,
    onNavigateToPlayNext: () -> Unit = {},
    hasProfile: Boolean = true,
) {
    var managePickId by remember { mutableStateOf<String?>(null) }
    val haptic = LocalHapticFeedback.current

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
                Text(
                    text = "Saved Picks",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Saved recommendations worth keeping close.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!hasProfile) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = PlayfitSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Set up your taste first",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(PlayfitSpacing.sm))
                        Text(
                            text = "Select your platforms and a few favorite games so we can build your recommendations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(PlayfitSpacing.lg))
                        Button(onClick = onNavigateToPlayNext) {
                            Text("Start Play Next", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (picks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = PlayfitSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No saved picks yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(PlayfitSpacing.sm))
                        Text(
                            text = "Save recommendations here when they match your gaming criteria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(PlayfitSpacing.lg))
                        Button(onClick = onNavigateToPlayNext) {
                            Text("Find Recommendations", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(picks, key = { it.game.gameId }) { pick ->
                    PickCard(
                        entry = pick,
                        onOpenDetail = { onOpenGame(pick.game.gameId) },
                        onManage = { managePickId = pick.game.gameId },
                    )
                }
            }

            item {
                Spacer(Modifier.height(PlayfitSpacing.xxl))
            }
        }
    }

    managePickId?.let { gameId ->
        val entry = picks.firstOrNull { it.game.gameId == gameId }
        if (entry != null) {
            ManagePickDialog(
                entry = entry,
                onDismiss = { managePickId = null },
                onAlreadyPlayed = { feedback ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.applyDecisionFeedback(gameId, feedback)
                    managePickId = null
                },
                onNoSkipThis = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.applyDecisionFeedback(gameId, ProductDecisionFeedback.NotForMe)
                    managePickId = null
                },
                onRemovePick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.togglePick(gameId)
                    managePickId = null
                },
            )
        }
    }
}

@Composable
private fun PickCard(
    entry: RankedSeedGame,
    onOpenDetail: () -> Unit,
    onManage: () -> Unit,
) {
    PlayfitGlassCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayfitCoverArt(
                gameId = entry.game.gameId,
                title = entry.game.title,
                coverUrl = entry.game.externalCoverUrl,
                modifier = Modifier.width(76.dp),
            )
            Spacer(Modifier.width(PlayfitSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.game.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "${entry.affinityScore.toInt()}% Match",
                    style = MaterialTheme.typography.labelSmall,
                    color = PlayfitExtendedTheme.colors.playfitAccent,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            OutlinedButton(onClick = onOpenDetail) {
                Text("See details", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ManagePickDialog(
    entry: RankedSeedGame,
    onDismiss: () -> Unit,
    onAlreadyPlayed: (ProductDecisionFeedback) -> Unit,
    onNoSkipThis: () -> Unit,
    onRemovePick: () -> Unit,
) {
    var showPlayedDialog by remember { mutableStateOf(false) }

    if (showPlayedDialog) {
        AlreadyPlayedDialog(
            open = true,
            onDismiss = { showPlayedDialog = false },
            onSelect = { feedback ->
                onAlreadyPlayed(feedback)
                showPlayedDialog = false
            },
        )
        return
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Manage Pick", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm)) {
                Text(
                    text = entry.game.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedButton(
                    onClick = { showPlayedDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Already Played It", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onNoSkipThis,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("No, skip this", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onRemovePick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PlayfitExtendedTheme.colors.playfitNegative,
                    ),
                ) {
                    Text("Remove Pick", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
