package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.ui.components.AlreadyPlayedDialog
import com.carlosarancibia.playfit.ui.components.FeedbackChips
import com.carlosarancibia.playfit.ui.components.ReasonList
import com.carlosarancibia.playfit.ui.components.ReasonTone
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.ScoreBadge
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.viewmodel.ProductUtils

@Composable
fun PrimaryRecommendationCard(
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
                coverUrl = entry.game.externalCoverUrl ?: entry.game.coverPath,
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
            if (showFeedbackChips) {
                TextButton(
                    onClick = {
                        onNotForMe()
                        showFeedbackChips = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Skip feedback",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
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
