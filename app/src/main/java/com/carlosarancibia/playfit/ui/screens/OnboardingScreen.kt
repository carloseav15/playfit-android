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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel
import com.carlosarancibia.playfit.ui.components.OnboardingHeader
import com.carlosarancibia.playfit.ui.components.ThemePickerButton
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    viewModel: PlayfitViewModel? = null,
    onComplete: (platforms: Set<String>, liked: List<String>, disliked: List<String>) -> Unit,
    onCancel: (() -> Unit)? = null,
    platforms: List<Platform> = fallbackPlatforms,
    onSearchGames: suspend (String) -> List<SeedGame> = { emptyList() },
    themeMode: String = "system",
    onThemeChange: (String) -> Unit = {},
) {
    val stepState = if (viewModel != null) viewModel.onboardingStep.collectAsMutableState() else remember { mutableStateOf(0) }
    var step by stepState

    val selectedPlatformIdsState = if (viewModel != null) viewModel.onboardingSelectedPlatforms.collectAsMutableState() else remember { mutableStateOf(setOf<String>()) }
    var selectedPlatformIds by selectedPlatformIdsState
    
    // Represent selection slots as size-bound lists containing null-capable entries
    val likedGamesState = if (viewModel != null) viewModel.onboardingLikedGames.collectAsMutableState() else remember { mutableStateOf(listOf<SeedGame?>(null, null, null)) }
    var likedGames by likedGamesState

    val dislikedGamesState = if (viewModel != null) viewModel.onboardingDislikedGames.collectAsMutableState() else remember { mutableStateOf(listOf<SeedGame?>(null)) }
    var dislikedGames by dislikedGamesState

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
                        TextButton(onClick = { step-- }) {
                            Text(
                                text = "← Back",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else if (onCancel != null) {
                        TextButton(onClick = onCancel) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    if (step == 2) {
                        val indigoGradient = Brush.linearGradient(
                            colors = listOf(
                                PlayfitExtendedTheme.colors.playfitAccent,
                                PlayfitExtendedTheme.colors.playfitIndigo
                            )
                        )
                        val buttonBackground = if (canContinue) {
                            Modifier
                                .clip(MaterialTheme.shapes.large)
                                .background(indigoGradient)
                        } else {
                            Modifier
                                .clip(MaterialTheme.shapes.large)
                                .background(PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.3f))
                        }
                        Button(
                            onClick = {
                                onComplete(
                                    selectedPlatformIds,
                                    likedGames.filterNotNull().map { it.gameId },
                                    dislikedGames.filterNotNull().map { it.gameId }
                                )
                            },
                            enabled = canContinue,
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 8.dp,
                                disabledElevation = 0.dp
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .then(buttonBackground)
                        ) {
                            Text(
                                text = "Find Play Next",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (canContinue) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                step++
                            },
                            enabled = canContinue,
                            shape = MaterialTheme.shapes.large,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "SET UP YOUR TASTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = PlayfitExtendedTheme.colors.playfitAccent,
                        letterSpacing = 0.15.sp
                    )

                    ThemePickerButton(
                        currentTheme = themeMode,
                        onThemeChange = onThemeChange,
                    )
                }

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
                likedGames.filterIndexed { idx, _ -> idx != slotIndex }.filterNotNull().map { it.gameId }.toSet()
            } else {
                likedGames.filterNotNull().map { it.gameId }.toSet()
            },
            likedGameIds = likedGames.filterNotNull().map { it.gameId }.toSet(),
            dislikedGameIds = dislikedGames.filterNotNull().map { it.gameId }.toSet(),
            replaceGameId = replaceGameId,
            isSearchPending = isSearchPending,
            searchError = searchError
        )
    }
}



@Composable
private fun <T> MutableStateFlow<T>.collectAsMutableState(): MutableState<T> {
    val state = collectAsState()
    return remember(this) {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(value) {
                    this@collectAsMutableState.value = value
                }
            override fun component1(): T = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}
