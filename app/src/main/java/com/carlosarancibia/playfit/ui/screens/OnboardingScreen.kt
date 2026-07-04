package com.carlosarancibia.playfit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (platforms: Set<String>, liked: List<String>, disliked: List<String>) -> Unit,
    onCancel: (() -> Unit)? = null,
    onSearchGames: suspend (String) -> List<SeedGame> = { emptyList() },
) {
    var step by remember { mutableStateOf(0) }
    var selectedPlatformIds by remember { mutableStateOf(setOf<String>()) }
    
    // Represent selection slots as size-bound lists containing null-capable entries
    var likedGames by remember { mutableStateOf(listOf<SeedGame?>(null, null, null)) }
    var dislikedGames by remember { mutableStateOf(listOf<SeedGame?>(null)) }

    // Search sheet states
    var showSearchSheet by remember { mutableStateOf(false) }
    var searchSlotIndex by remember { mutableStateOf<Int?>(null) } // 0..2 for liked, 3 for disliked
    var replaceGameId by remember { mutableStateOf<String?>(null) }

    // Search query & results
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<SeedGame>>(emptyList()) }
    var searchError by remember { mutableStateOf(false) }
    var isSearchPending by remember { mutableStateOf(false) }

    var showPlatformSheet by remember { mutableStateOf(false) }

    val platforms = fallbackPlatforms

    LaunchedEffect(searchQuery) {
        val trimmed = searchQuery.trim()
        if (trimmed.length >= 2) {
            isSearchPending = true
            searchError = false
            kotlinx.coroutines.delay(300)
            val results = try {
                onSearchGames(trimmed)
            } catch (_: Exception) {
                searchError = true
                emptyList()
            }
            searchResults = results
            isSearchPending = false
        } else {
            searchResults = emptyList()
            searchError = false
            isSearchPending = false
        }
    }

    val onTogglePlatform = { platformId: String ->
        selectedPlatformIds = if (platformId in selectedPlatformIds) {
            selectedPlatformIds - platformId
        } else {
            selectedPlatformIds + platformId
        }
    }

    val onToggleAllPlatforms = {
        val allSelected = selectedPlatformIds.size == platforms.size
        selectedPlatformIds = if (allSelected) {
            emptySet()
        } else {
            platforms.map { it.platformId }.toSet()
        }
    }

    val onTogglePreset = { preset: PlatformPreset ->
        val presetPlatforms = platforms.filter(preset.match)
        val presetIds = presetPlatforms.map { it.platformId }.toSet()
        if (presetIds.isNotEmpty()) {
            val allSelected = presetIds.all { it in selectedPlatformIds }
            selectedPlatformIds = if (allSelected) {
                selectedPlatformIds - presetIds
            } else {
                selectedPlatformIds + presetIds
            }
        }
    }

    val onSelectGame = { game: SeedGame ->
        val index = searchSlotIndex
        if (index != null) {
            if (index in 0..2) {
                // Loved Game selection at slot 'index'
                val currentList = likedGames.toMutableList()
                currentList[index] = game
                likedGames = currentList
                
                // Swap rule: remove from dislikes if selected as loved
                if (dislikedGames.firstOrNull()?.gameId == game.gameId) {
                    dislikedGames = listOf(null)
                }
            } else if (index == 3) {
                // Avoided Game selection
                dislikedGames = listOf(game)
                
                // Swap rule: remove from loved if selected as avoided
                val lovedIndex = likedGames.indexOfFirst { it?.gameId == game.gameId }
                if (lovedIndex != -1) {
                    val currentList = likedGames.toMutableList()
                    currentList[lovedIndex] = null
                    likedGames = currentList
                }
            }
        }

        showSearchSheet = false
        searchSlotIndex = null
        replaceGameId = null
        searchQuery = ""
        searchResults = emptyList()
    }

    val onRemoveLikedGame = { index: Int ->
        val currentList = likedGames.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = null
        }
        likedGames = currentList
    }

    val onRemoveDislikedGame = {
        dislikedGames = listOf(null)
    }

    val canContinue = when (step) {
        0 -> selectedPlatformIds.isNotEmpty()
        1 -> likedGames.filterNotNull().size == 3
        2 -> dislikedGames.filterNotNull().size == 1
        else -> false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayfitSpacing.lg, vertical = PlayfitSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 0) {
                        TextButton(onClick = {
                            step--
                        }) {
                            Text(
                                text = "← Back",
                                fontWeight = FontWeight.Bold,
                                color = PlayfitExtendedTheme.colors.playfitAccent
                            )
                        }
                    } else if (onCancel != null) {
                        TextButton(onClick = onCancel) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (step == 2) {
                        val indigoGradient = Brush.linearGradient(
                            colors = listOf(
                                PlayfitExtendedTheme.colors.playfitAccent,
                                Color(0xFF6366F1)
                            )
                        )
                        Button(
                            onClick = {
                                onComplete(
                                    selectedPlatformIds,
                                    likedGames.filterNotNull().map { it.gameId },
                                    dislikedGames.filterNotNull().map { it.gameId }
                                )
                            },
                            enabled = canContinue,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.3f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(if (canContinue) indigoGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Find Play Next",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                step++
                            },
                            enabled = canContinue,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PlayfitExtendedTheme.colors.playfitAccent,
                                disabledContainerColor = PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "Continue",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            GlowBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = PlayfitSpacing.lg)
                    .padding(top = PlayfitSpacing.xl, bottom = PlayfitSpacing.md),
                verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md)
            ) {
                Text(
                    text = "SET UP YOUR TASTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = PlayfitExtendedTheme.colors.playfitAccent,
                    letterSpacing = 0.15.sp
                )

                OnboardingHeader(
                    currentStep = step,
                    selectedPlatformsCount = selectedPlatformIds.size,
                    likedCount = likedGames.filterNotNull().size,
                    dislikedCount = dislikedGames.filterNotNull().size
                )

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "OnboardingStepTransition",
                    modifier = Modifier.fillMaxWidth()
                ) { currentStep ->
                    when (currentStep) {
                        0 -> PlatformStep(
                            selectedIds = selectedPlatformIds,
                            platforms = platforms,
                            onTogglePreset = onTogglePreset,
                            onCustomize = { showPlatformSheet = true }
                        )
                        1 -> LovedGamesStep(
                            likedGames = likedGames,
                            onSlotClick = { idx ->
                                searchSlotIndex = idx
                                replaceGameId = likedGames.getOrNull(idx)?.gameId
                                showSearchSheet = true
                            },
                            onRemove = onRemoveLikedGame
                        )
                        2 -> MissedGameStep(
                            dislikedGame = dislikedGames.firstOrNull(),
                            onSlotClick = {
                                searchSlotIndex = 3
                                replaceGameId = dislikedGames.firstOrNull()?.gameId
                                showSearchSheet = true
                            },
                            onRemove = onRemoveDislikedGame
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for Customizing Platforms
    if (showPlatformSheet) {
        CustomizePlatformsSheet(
            selectedIds = selectedPlatformIds,
            platforms = platforms,
            onToggle = onTogglePlatform,
            onToggleAll = onToggleAllPlatforms,
            onDismiss = { showPlatformSheet = false }
        )
    }

    // Modal Sheet for Game Search
    if (showSearchSheet) {
        val slotIndex = searchSlotIndex
        val isLiked = slotIndex != null && slotIndex in 0..2
        GameSearchSheet(
            title = if (isLiked) {
                if (replaceGameId != null) "Change loved game" else "Search loved game"
            } else "Search missed game",
            eyebrow = if (isLiked) "Loved Games" else "Missed Game",
            isLiked = isLiked,
            query = searchQuery,
            searchResults = searchResults,
            onQueryChange = { searchQuery = it },
            onSelect = onSelectGame,
            onDismiss = {
                showSearchSheet = false
                searchSlotIndex = null
                replaceGameId = null
                searchQuery = ""
                searchResults = emptyList()
            },
            blockedGameIds = if (isLiked) {
                dislikedGames.filterNotNull().map { it.gameId }.toSet() +
                likedGames.filterIndexed { idx, _ -> idx != slotIndex }.filterNotNull().map { it.gameId }.toSet()
            } else {
                likedGames.filterNotNull().map { it.gameId }.toSet()
            },
            likedGameIds = likedGames.filterNotNull().map { it.gameId }.toSet(),
            dislikedGameIds = dislikedGames.filterNotNull().map { it.gameId }.toSet(),
            isSearchPending = isSearchPending,
            searchError = searchError
        )
    }
}

@Composable
fun OnboardingHeader(
    currentStep: Int,
    selectedPlatformsCount: Int,
    likedCount: Int,
    dislikedCount: Int
) {
    val stepData = listOf(
        Triple("Platforms", "$selectedPlatformsCount selected", 0),
        Triple("Loved Games", "$likedCount/3", 1),
        Triple("Missed Game", "$dislikedCount/1", 2)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PlayfitSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stepData.forEach { (label, count, stepIndex) ->
            val isCompleted = currentStep > stepIndex
            val isActive = currentStep == stepIndex

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                // Horizontal track bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(PlayfitExtendedTheme.colors.playfitPositive)
                        )
                    } else if (isActive) {
                        val infiniteTransition = rememberInfiniteTransition(label = "progressBarPulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )
                        val activeBrush = Brush.linearGradient(
                            colors = listOf(
                                PlayfitExtendedTheme.colors.playfitAccent,
                                Color(0xFFEC4899)
                            )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(brush = activeBrush, alpha = pulseAlpha)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val labelColor = when {
                        isActive -> PlayfitExtendedTheme.colors.playfitAccent
                        isCompleted -> PlayfitExtendedTheme.colors.playfitPositive
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                    Text(
                        text = label.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = labelColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = count,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PlatformStep(
    selectedIds: Set<String>,
    platforms: List<Platform>,
    onTogglePreset: (PlatformPreset) -> Unit,
    onCustomize: () -> Unit,
) {
    Column {
        Text(
            text = "Where do you play?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = "We will only recommend games available on your active platforms.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.lg))

        Text(
            text = "QUICK GROUPS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(PlayfitSpacing.sm))

        // Grid of presets (2-column column of rows)
        val presets = platformPresets
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in presets.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlatformPresetCard(
                        preset = presets[i],
                        selectedIds = selectedIds,
                        platforms = platforms,
                        onClick = { onTogglePreset(presets[i]) },
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < presets.size) {
                        PlatformPresetCard(
                            preset = presets[i + 1],
                            selectedIds = selectedIds,
                            platforms = platforms,
                            onClick = { onTogglePreset(presets[i + 1]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(PlayfitSpacing.lg))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onCustomize) {
                Text(
                    text = "Customize Platforms...",
                    fontWeight = FontWeight.Bold,
                    color = PlayfitExtendedTheme.colors.playfitAccent
                )
            }
        }
    }
}

@Composable
fun PlatformPresetCard(
    preset: PlatformPreset,
    selectedIds: Set<String>,
    platforms: List<Platform>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presetPlatforms = platforms.filter(preset.match)
    val presetIds = presetPlatforms.map { it.platformId }.toSet()
    val selectedCount = presetIds.count { it in selectedIds }
    val isSelected = presetIds.isNotEmpty() && selectedCount == presetIds.size
    val isPartiallySelected = selectedCount > 0 && !isSelected

    val accentColor = PlayfitExtendedTheme.colors.playfitAccent
    val borderColor = when {
        isSelected -> accentColor.copy(alpha = 0.4f)
        isPartiallySelected -> accentColor.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    }
    val backgroundColor = when {
        isSelected -> accentColor.copy(alpha = 0.08f)
        isPartiallySelected -> accentColor.copy(alpha = 0.04f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .background(
                Brush.radialGradient(
                    colors = if (isSelected) listOf(accentColor.copy(alpha = 0.08f), Color.Transparent)
                    else listOf(Color.Transparent, Color.Transparent),
                    radius = 200f
                )
            )
            .padding(16.dp)
            .fillMaxWidth()
            .height(112.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) accentColor else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    when (preset.id) {
                        "pc" -> LaptopIcon(modifier = Modifier.size(18.dp), color = iconColor)
                        "retro" -> TvIcon(modifier = Modifier.size(18.dp), color = iconColor)
                        else -> GamepadIcon(modifier = Modifier.size(18.dp), color = iconColor)
                    }
                }
            }

            val statusLabel = when {
                isSelected -> "Selected"
                isPartiallySelected -> "$selectedCount of ${presetIds.size}"
                else -> "${presetIds.size} systems"
            }
            val statusColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

            Text(
                text = statusLabel.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = statusColor,
                letterSpacing = 0.8.sp
            )
        }
    }
}

@Composable
private fun LovedGamesStep(
    likedGames: List<SeedGame?>,
    onSlotClick: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    Column {
        Text(
            text = "Pick three games you loved",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = "Start with games that clicked. We will look for similar games.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (i in 0..2) {
                val game = likedGames.getOrNull(i)
                GameSlotCard(
                    game = game,
                    indexLabel = "Select ${i + 1}",
                    isLiked = true,
                    onClick = { onSlotClick(i) },
                    onRemove = { onRemove(i) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MissedGameStep(
    dislikedGame: SeedGame?,
    onSlotClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column {
        Text(
            text = "Pick one game that wasn't for you",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = "Tell us a popular game you didn't enjoy so we know what to avoid.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.lg))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            GameSlotCard(
                game = dislikedGame,
                indexLabel = "Select Game",
                isLiked = false,
                onClick = onSlotClick,
                onRemove = onRemove,
                modifier = Modifier.fillMaxWidth(0.45f)
            )
        }
    }
}

@Composable
fun GameSlotCard(
    game: SeedGame?,
    indexLabel: String,
    isLiked: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isLiked) PlayfitExtendedTheme.colors.playfitAccent
    else PlayfitExtendedTheme.colors.playfitNegative
    val cardBgColor = if (isLiked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    else PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.05f)
    val dashColor = if (isLiked) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    else PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.25f)

    if (game != null) {
        Box(
            modifier = modifier
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
        ) {
            val coverUrl = game.externalCoverUrl ?: game.coverPath
            PlayfitCoverArt(
                gameId = game.gameId,
                title = game.title,
                coverUrl = coverUrl.ifBlank { null },
                modifier = Modifier.fillMaxSize()
            )

            // Hover overlay emulation (gradient at bottom + title)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                            startY = 100f
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CHANGE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = game.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Remove Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u2715",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        // Empty slot with premium dashed border drawn via pathEffect
        Box(
            modifier = modifier
                .aspectRatio(0.72f)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBgColor)
                .clickable { onClick() }
                .drawBehind {
                    val stroke = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                    drawRoundRect(
                        color = dashColor,
                        style = stroke,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                    )
                }
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isLiked) PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.12f)
                            else PlayfitExtendedTheme.colors.playfitNegative.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = indexLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizePlatformsSheet(
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
                androidx.compose.material3.Checkbox(
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
                val familyPlatforms = platforms.filter { it.family == family }.sortedBy { it.sortOrder }
                if (familyPlatforms.isNotEmpty()) {
                    Text(
                        text = familyDisplayName(family).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = PlayfitExtendedTheme.colors.playfitAccent,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(top = PlayfitSpacing.md, bottom = PlayfitSpacing.xs),
                    )

                    val consoles = familyPlatforms.filter { it.kind != "handheld" }
                    val handhelds = familyPlatforms.filter { it.kind == "handheld" }

                    if (consoles.isNotEmpty()) {
                        if (handhelds.isNotEmpty()) {
                            Text(
                                text = "Console / Hybrid",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = PlayfitExtendedTheme.colors.playfitAccent,
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = platform.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = if (isSelected) "\u2713 Selected" else "+ Add",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = if (isSelected) PlayfitExtendedTheme.colors.playfitPositive
            else PlayfitExtendedTheme.colors.playfitAccent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GameSearchSheet(
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
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Text("\u2715", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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

                        var isDisabled = false
                        var isCurrentSelection = false
                        var statusLabel = ""

                        if (isLiked) {
                            isDisabled = isLoved
                            if (isDisabled) {
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
                        if (!game.releaseYear.isNullOrBlank()) metadataParts.add(game.releaseYear)
                        if (game.availablePlatformNames.isNotEmpty()) {
                            val platformNames = game.availablePlatformNames.take(3).joinToString(", ")
                            val suffix = if (game.availablePlatformNames.size > 3) "..." else ""
                            metadataParts.add(platformNames + suffix)
                        }
                        val metadataString = metadataParts.joinToString(" • ")

                        val rowBorderColor = if (isCurrentSelection) accentColor.copy(alpha = 0.3f)
                                             else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, rowBorderColor, RoundedCornerShape(12.dp))
                                .background(
                                    if (isCurrentSelection) accentColor.copy(alpha = 0.08f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                                .clickable(enabled = !isDisabled) {
                                    onSelect(game)
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val coverUrl = game.externalCoverUrl ?: game.coverPath
                            Box(modifier = Modifier.size(width = 40.dp, height = 56.dp)) {
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
                                    color = if (isDisabled) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                if (statusLabel.isNotBlank()) {
                                    Text(
                                        text = statusLabel.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        else accentColor
                                    )
                                } else {
                                    Text(
                                        text = metadataString,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isCurrentSelection) {
                                Text(
                                    text = "\u2713",
                                    color = accentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            } else if (!isDisabled) {
                                Text(
                                    text = "\u2192",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    fontSize = 16.sp
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

// ── Vector Icon Drawings ───────────────────────────────────────────────────

@Composable
fun GamepadIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = height * 0.15f,
                    right = width,
                    bottom = height * 0.85f,
                    radiusX = width * 0.25f,
                    radiusY = height * 0.25f
                )
            )
        }
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))

        val dpadCenter = Offset(width * 0.25f, height * 0.5f)
        val dpadSize = width * 0.12f
        drawRect(
            color = color,
            topLeft = Offset(dpadCenter.x - dpadSize / 3, dpadCenter.y - dpadSize),
            size = Size(dpadSize * 2 / 3, dpadSize * 2)
        )
        drawRect(
            color = color,
            topLeft = Offset(dpadCenter.x - dpadSize, dpadCenter.y - dpadSize / 3),
            size = Size(dpadSize * 2, dpadSize * 2 / 3)
        )

        val btnCenter = Offset(width * 0.75f, height * 0.5f)
        val btnRad = width * 0.07f
        drawCircle(color = color, radius = btnRad, center = Offset(btnCenter.x + btnRad * 1.2f, btnCenter.y - btnRad * 0.6f))
        drawCircle(color = color, radius = btnRad, center = Offset(btnCenter.x - btnRad * 1.2f, btnCenter.y + btnRad * 0.6f))
    }
}

@Composable
fun LaptopIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val screenWidth = width * 0.8f
        val screenHeight = height * 0.55f
        val screenLeft = width * 0.1f
        val screenTop = height * 0.15f

        val screenPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = screenLeft,
                    top = screenTop,
                    right = screenLeft + screenWidth,
                    bottom = screenTop + screenHeight,
                    radiusX = 4.dp.toPx(),
                    radiusY = 4.dp.toPx()
                )
            )
        }
        drawPath(path = screenPath, color = color, style = Stroke(width = 2.dp.toPx()))

        val baseLeft = width * 0.03f
        val baseRight = width * 0.97f
        val baseTop = screenTop + screenHeight
        val baseBottom = height * 0.85f

        val basePath = Path().apply {
            moveTo(screenLeft + 8.dp.toPx(), baseTop)
            lineTo(screenLeft + screenWidth - 8.dp.toPx(), baseTop)
            lineTo(baseRight, baseBottom - 2.dp.toPx())
            lineTo(baseLeft, baseBottom - 2.dp.toPx())
            close()
        }
        drawPath(path = basePath, color = color, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun TvIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val tvWidth = width * 0.85f
        val tvHeight = height * 0.6f
        val tvLeft = width * 0.075f
        val tvTop = height * 0.25f

        val tvPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = tvLeft,
                    top = tvTop,
                    right = tvLeft + tvWidth,
                    bottom = tvTop + tvHeight,
                    radiusX = 6.dp.toPx(),
                    radiusY = 6.dp.toPx()
                )
            )
        }
        drawPath(path = tvPath, color = color, style = Stroke(width = 2.dp.toPx()))

        drawLine(
            color = color,
            start = Offset(tvLeft + tvWidth * 0.2f, tvTop + tvHeight),
            end = Offset(tvLeft + tvWidth * 0.1f, height * 0.95f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(tvLeft + tvWidth * 0.8f, tvTop + tvHeight),
            end = Offset(tvLeft + tvWidth * 0.9f, height * 0.95f),
            strokeWidth = 2.dp.toPx()
        )

        val center = Offset(width * 0.5f, tvTop)
        drawLine(
            color = color,
            start = center,
            end = Offset(width * 0.25f, height * 0.05f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = color,
            start = center,
            end = Offset(width * 0.75f, height * 0.05f),
            strokeWidth = 2.dp.toPx()
        )
    }
}
