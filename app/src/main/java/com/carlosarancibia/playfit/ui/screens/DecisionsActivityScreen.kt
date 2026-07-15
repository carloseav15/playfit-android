package com.carlosarancibia.playfit.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.ProductTasteHistoryEntry
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.ui.components.design.DecisionLabelBadge
import com.carlosarancibia.playfit.ui.components.design.DecisionTone
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.components.design.PlayfitOpacities

private enum class ActivityTab { All, Picks, Preferences }

private fun badgeLabel(entry: ProductTasteHistoryEntry): String = when (entry.decision) {
    "picks" -> "Saved Pick"
    "setup_favorite" -> "Setup: Loved"
    "setup_miss" -> "Setup: Missed"
    "loved" -> "Loved"
    "liked" -> "Liked"
    "mixed" -> "Mixed"
    "dropped" -> "Dropped"
    "not_for_me" -> "Not For Me"
    else -> "Rated"
}

private fun badgeTone(entry: ProductTasteHistoryEntry): DecisionTone = when (entry.tone) {
    "positive" -> DecisionTone.Positive
    "negative" -> DecisionTone.Negative
    "warning" -> DecisionTone.Warning
    else -> DecisionTone.Info
}

private fun formatDate(isoDate: String?): String {
    if (isoDate == null) return "Baseline"
    return try {
        isoDate.substringBefore("T").substringAfterLast("-")
            .let { day -> isoDate.substringBefore("T").take(7) + "-" + day }
    } catch (_: Exception) { "Baseline" }
}

internal val playedChangeSignalOptions = listOf(
    "Loved" to ProductDecisionFeedback.PlayedLoved,
    "Liked" to ProductDecisionFeedback.PlayedLiked,
    "Mixed" to ProductDecisionFeedback.PlayedMixed,
    "Dropped" to ProductDecisionFeedback.PlayedDropped,
    "Not For Me" to ProductDecisionFeedback.NotForMe,
)

@Composable
fun DecisionsActivityContent(
    tasteModel: ProductTasteModel?,
    onOpenGame: (String) -> Unit,
    onRemovePick: (String) -> Unit,
    onChangeSignal: (String, ProductDecisionFeedback) -> Unit,
    onDeleteSignal: (String, String) -> Unit,
) {
    var activeTab by remember { mutableStateOf(ActivityTab.All) }

    if (tasteModel == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No activity yet. Start rating games to see your history.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val history = tasteModel.historyEntries
    val filtered = history.filter { entry ->
        when (activeTab) {
            ActivityTab.All -> true
            ActivityTab.Picks -> entry.decision == "picks"
            ActivityTab.Preferences -> entry.decision != "picks"
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(PlayfitSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
        ) {
            ActivityTab.entries.forEach { tab ->
                val count = when (tab) {
                    ActivityTab.All -> history.size
                    ActivityTab.Picks -> history.count { it.decision == "picks" }
                    ActivityTab.Preferences -> history.count { it.decision != "picks" }
                }
                val label = when (tab) {
                    ActivityTab.All -> "All ($count)"
                    ActivityTab.Picks -> "Picks ($count)"
                    ActivityTab.Preferences -> "Preferences ($count)"
                }
                FilterChip(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    label = {
                        Text(
                            text = label,
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

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = PlayfitSpacing.xxl),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No entries for this filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                items(filtered, key = { it.gameId }) { entry ->
                    ActivityRow(
                        entry = entry,
                        onClick = { onOpenGame(entry.gameId) },
                        onRemovePick = {
                            if (entry.decision == "picks") onRemovePick(entry.gameId)
                        },
                        onChangeSignal = { feedback ->
                            onChangeSignal(entry.gameId, feedback)
                        },
                        onDeleteSignal = {
                            onDeleteSignal(entry.gameId, entry.source)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    entry: ProductTasteHistoryEntry,
    onClick: () -> Unit,
    onRemovePick: () -> Unit,
    onChangeSignal: (ProductDecisionFeedback) -> Unit,
    onDeleteSignal: () -> Unit,
) {
    var showManageDialog by remember { mutableStateOf(false) }
    var showChangeSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PlayfitSpacing.sm, horizontal = PlayfitSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayfitCoverArt(
            gameId = entry.gameId,
            title = entry.title,
            coverUrl = entry.coverUrl,
            modifier = Modifier
                .width(44.dp)
                .height(60.dp),
            decorative = false,
        )

        Spacer(Modifier.width(PlayfitSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DecisionLabelBadge(
                    label = badgeLabel(entry),
                    tone = badgeTone(entry),
                )
                if (entry.rating != null && entry.rating > 0) {
                    Text(
                        text = "\u2605 ${entry.rating.toInt()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PlayfitExtendedTheme.colors.playfitAccent,
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Text(
                text = formatDate(entry.updatedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IconButton(
            onClick = { showManageDialog = true },
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Manage signal for ${entry.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = PlayfitOpacities.half))

    if (showManageDialog) {
        AlertDialog(
            onDismissRequest = { showManageDialog = false },
            title = {
                Text(
                    text = "Manage Signal",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (entry.decision == "picks") {
                        TextButton(
                            onClick = {
                                showManageDialog = false
                                onRemovePick()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Remove from Picks",
                                color = PlayfitExtendedTheme.colors.playfitNegative,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        TextButton(
                            onClick = {
                                showManageDialog = false
                                showChangeSheet = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Change Signal",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        TextButton(
                            onClick = {
                                showManageDialog = false
                                onDeleteSignal()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = "Delete Signal",
                                color = PlayfitExtendedTheme.colors.playfitNegative,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showManageDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showChangeSheet) {
        ChangeSignalSheet(
            onDismiss = { showChangeSheet = false },
            onSelect = { feedback ->
                showChangeSheet = false
                onChangeSignal(feedback)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeSignalSheet(
    onDismiss: () -> Unit,
    onSelect: (ProductDecisionFeedback) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PlayfitSpacing.lg),
        ) {
            Text(
                text = "CHANGE SIGNAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            Text(
                text = "How do you feel about this game?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(PlayfitSpacing.lg))

            playedChangeSignalOptions.forEach { (label, feedback) ->
                TextButton(
                    onClick = {
                        onSelect(feedback)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(PlayfitSpacing.md))
        }
    }
}
