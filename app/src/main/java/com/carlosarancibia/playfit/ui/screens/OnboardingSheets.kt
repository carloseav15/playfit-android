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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.carlosarancibia.playfit.ui.components.design.PlayfitOpacities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizePlatformsSheet(
    selectedIds: Set<String>,
    platforms: List<Platform>,
    onToggle: (String) -> Unit,
    onToggleAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allSelected = platforms.isNotEmpty() && selectedIds.size == platforms.size

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PlayfitSpacing.lg)
                .padding(bottom = PlayfitSpacing.xl),
        ) {
            Text(
                text = "Customize Platforms",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(PlayfitSpacing.xs))
            Text(
                text = "Select individual platforms by brand.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlayfitSpacing.md))

            // Select all checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAll() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { onToggleAll() }
                )
                Text(
                    text = if (allSelected) "Deselect all platforms" else "Select all platforms",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(PlayfitSpacing.sm))

            val families = sortedPlatformFamilies(platforms)
            families.forEach { family ->
                val familyPlatforms = if (family == "other") {
                    val standard = com.carlosarancibia.playfit.model.platformStandardFamilies
                    platforms.filter { it.family !in standard }.sortedBy { it.sortOrder }
                } else {
                    platforms.filter { it.family == family }.sortedBy { it.sortOrder }
                }
                if (familyPlatforms.isNotEmpty()) {
                    Text(
                        text = familyDisplayName(family).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = PlayfitExtendedTheme.colors.playfitAccent,
                        modifier = Modifier.padding(top = PlayfitSpacing.md, bottom = PlayfitSpacing.xs),
                    )

                    val consoles = familyPlatforms.filter { it.kind != "handheld" }
                    val handhelds = familyPlatforms.filter { it.kind == "handheld" }

                    if (consoles.isNotEmpty()) {
                        if (handhelds.isNotEmpty()) {
                            Text(
                                text = "Console / Hybrid",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.strong),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        consoles.forEach { platform ->
                            PlatformSelectionRow(
                                platform = platform,
                                isSelected = platform.platformId in selectedIds,
                                onToggle = { onToggle(platform.platformId) }
                            )
                        }
                    }

                    if (handhelds.isNotEmpty()) {
                        Text(
                            text = "Handheld",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.strong),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        handhelds.forEach { platform ->
                            PlatformSelectionRow(
                                platform = platform,
                                isSelected = platform.platformId in selectedIds,
                                onToggle = { onToggle(platform.platformId) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(PlayfitSpacing.lg))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Done", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun PlatformSelectionRow(
    platform: Platform,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = platform.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameSearchSheet(
    title: String,
    eyebrow: String,
    isLiked: Boolean,
    query: String,
    searchResults: List<SeedGame>,
    onQueryChange: (String) -> Unit,
    onSelect: (SeedGame) -> Unit,
    onDismiss: () -> Unit,
    blockedGameIds: Set<String>,
    likedGameIds: Set<String>,
    dislikedGameIds: Set<String>,
    replaceGameId: String?,
    isSearchPending: Boolean,
    searchError: Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accentColor = if (isLiked) PlayfitExtendedTheme.colors.playfitAccent
    else PlayfitExtendedTheme.colors.playfitNegative

    val quickSuggestions = listOf("Elden Ring", "Hades", "Hollow Knight", "Portal 2", "The Witcher 3")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PlayfitSpacing.lg)
                .padding(bottom = PlayfitSpacing.xl)
        ) {
            // Header
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = accentColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(PlayfitSpacing.md))

            // Search text field
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search by title...") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    focusedLabelColor = accentColor,
                    cursorColor = accentColor,
                ),
            )

            Spacer(Modifier.height(PlayfitSpacing.md))

            // Quick suggestions
            Text(
                text = "QUICK SUGGESTIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.strong),
            )
            Spacer(Modifier.height(PlayfitSpacing.xs))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                quickSuggestions.forEach { suggestion ->
                    FilterChip(
                        selected = false,
                        onClick = { onQueryChange(suggestion) },
                        label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }

            Spacer(Modifier.height(PlayfitSpacing.md))

            // Results List
            Text(
                text = "RESULTS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.strong),
            )
            Spacer(Modifier.height(PlayfitSpacing.xs))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSearchPending) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Searching catalog...",
                            style = MaterialTheme.typography.bodySmall,
                            color = PlayfitExtendedTheme.colors.playfitAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (searchError) {
                    Text(
                        text = "Search failed. Check your connection and try again.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PlayfitExtendedTheme.colors.playfitNegative,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    searchResults.forEach { game ->
                        val isLoved = game.gameId in likedGameIds
                        val isDisliked = game.gameId in dislikedGameIds
                        val isBlocked = game.gameId in blockedGameIds

                        var isDisabled = false
                        var isCurrentSelection = game.gameId == replaceGameId
                        var statusLabel = ""

                        if (isLiked) {
                            isDisabled = isBlocked && !isCurrentSelection
                            if (isCurrentSelection) {
                                statusLabel = "Current selection"
                            } else if (isLoved && !isCurrentSelection) {
                                statusLabel = "Already selected as loved"
                            } else if (isDisliked) {
                                statusLabel = "Selected as disliked (will swap)"
                            }
                        } else {
                            isCurrentSelection = isDisliked
                            isDisabled = isLoved
                            if (isDisabled) {
                                statusLabel = "Selected as loved"
                            } else if (isCurrentSelection) {
                                statusLabel = "Current selection"
                            }
                        }

                        val metadataParts = mutableListOf<String>()
                        if (game.primaryGenre.isNotBlank()) metadataParts.add(game.primaryGenre)
                        if (!game.releaseYear.isNullOrBlank() && game.releaseYear != "0") metadataParts.add(game.releaseYear)
                        if (game.availablePlatformNames.isNotEmpty()) {
                            val platformNames = game.availablePlatformNames.take(3).joinToString(", ")
                            val suffix = if (game.availablePlatformNames.size > 3) "..." else ""
                            metadataParts.add(platformNames + suffix)
                        }
                        val metadataString = metadataParts.joinToString(" • ")

                        val rowBorderColor = if (isCurrentSelection) accentColor.copy(alpha = PlayfitOpacities.medium)
                                             else Color.Transparent

                        val rowAccessibilityLabel = buildString {
                            append(game.title)
                            if (metadataString.isNotBlank()) append(", $metadataString")
                            if (statusLabel.isNotBlank()) append(", $statusLabel")
                            if (isDisabled) append(", unavailable")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .border(1.dp, rowBorderColor, MaterialTheme.shapes.medium)
                                .background(
                                    if (isCurrentSelection) accentColor.copy(alpha = PlayfitOpacities.subtle)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PlayfitOpacities.light)
                                )
                                .clickable(enabled = !isDisabled) {
                                    onSelect(game)
                                }
                                .semantics { contentDescription = rowAccessibilityLabel }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val coverUrl = game.externalCoverUrl ?: game.coverPath
                            Box(modifier = Modifier.width(40.dp).aspectRatio(0.75f)) {
                                PlayfitCoverArt(
                                    gameId = game.gameId,
                                    title = game.title,
                                    coverUrl = coverUrl.ifBlank { null },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = game.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDisabled) MaterialTheme.colorScheme.onBackground.copy(alpha = PlayfitOpacities.moderate)
                                    else MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                if (statusLabel.isNotBlank()) {
                                    Text(
                                        text = statusLabel.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.half)
                                        else accentColor
                                    )
                                } else {
                                    Text(
                                        text = metadataString,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.heavy),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isCurrentSelection) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Currently selected",
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            } else if (!isDisabled) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.half),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }

                    if (searchResults.isEmpty() && query.isNotBlank()) {
                        Text(
                            text = "No games found. Try a different search.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else if (query.isBlank()) {
                        Text(
                            text = "Type a game title above to search.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
