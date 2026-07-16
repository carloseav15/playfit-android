package com.carlosarancibia.playfit.ui.screens

import com.carlosarancibia.playfit.BuildConfig
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.fallbackPlatforms
import com.carlosarancibia.playfit.ui.components.design.MoonIcon
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.SparklesIcon
import com.carlosarancibia.playfit.ui.components.design.SunIcon
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.components.design.PlayfitOpacities


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlatformSelectionView(
    onBack: () -> Unit,
    persistedSelectedPlatformIds: Set<String> = emptySet(),
    onUpdatePlatforms: (Set<String>) -> Unit = {},
    platforms: List<Platform> = fallbackPlatforms,
    platformsLoading: Boolean = false,
    platformsError: String? = null,
    platformsStale: Boolean = false,
) {
    val availablePlatforms = platforms.ifEmpty { fallbackPlatforms }
    // Keep the state value immutable. Mutating a MutableSet in place does not reliably
    // invalidate Compose snapshots and can leave the selection UI stale.
    val selectedPlatformIds = remember { mutableStateOf(persistedSelectedPlatformIds) }
    LaunchedEffect(persistedSelectedPlatformIds, availablePlatforms) {
        val validPersistedIds = persistedSelectedPlatformIds.filterTo(mutableSetOf()) { persistedId ->
            availablePlatforms.any { it.platformId == persistedId }
        }
        if (selectedPlatformIds.value != validPersistedIds) {
            selectedPlatformIds.value = validPersistedIds
        }
    }

    fun persist(ids: Set<String>) {
        if (ids.isEmpty()) {
            onUpdatePlatforms(ids)
            return
        }
        selectedPlatformIds.value = ids.toSet()
        onUpdatePlatforms(ids)
    }

    var selectedFamily by remember { mutableStateOf("nintendo") }
    LaunchedEffect(availablePlatforms) {
        val families = com.carlosarancibia.playfit.model.sortedPlatformFamilies(availablePlatforms)
        if (families.isNotEmpty() && selectedFamily !in families) {
            selectedFamily = families.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Platforms",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
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
            )
        },
        containerColor = Color.Transparent,
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlayfitSpacing.md),
    ) {
        Spacer(Modifier.height(PlayfitSpacing.sm))
        Text(
            text = "Recommendations are only shown for games available on your active platforms. Changes save automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (platformsLoading || platformsError != null || platformsStale) {
            Spacer(Modifier.height(PlayfitSpacing.xs))
            Text(
                text = when {
                    platformsLoading -> "Loading current platform catalog..."
                    platformsError != null -> "Using saved platform catalog. ${platformsError}"
                    else -> "Using saved platform catalog."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(PlayfitSpacing.md))

        // Quick Groups / Presets
        SettingsSection(title = "Quick Groups") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                com.carlosarancibia.playfit.model.platformPresets.forEach { preset ->
                    val presetPlatforms = availablePlatforms.filter(preset.match)
                    val presetIds = presetPlatforms.map { it.platformId }.toSet()
                    val fullySelected = presetIds.isNotEmpty() && presetIds.all { it in selectedPlatformIds.value }
                    val partiallySelected = presetIds.any { it in selectedPlatformIds.value } && !fullySelected

                    FilterChip(
                        selected = fullySelected || partiallySelected,
                        onClick = {
                            val allSelected = presetIds.all { it in selectedPlatformIds.value }
                            val ids = if (allSelected)
                                selectedPlatformIds.value - presetIds
                            else selectedPlatformIds.value + presetIds
                            persist(ids)
                        },
                        label = {
                            Column {
                                Text(preset.label, fontWeight = FontWeight.SemiBold)
                                Text(preset.description, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = PlayfitOpacities.light),
                        ),
                    )
                }
            }
        }

        Spacer(Modifier.height(PlayfitSpacing.md))

        // Brand tabs
        Text(
            text = "Platforms by Brand",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(PlayfitSpacing.sm))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val families = com.carlosarancibia.playfit.model.sortedPlatformFamilies(availablePlatforms)
            families.forEach { family ->
                val familyPlatforms = availablePlatforms.filter { it.family == family }
                val selectedCount = familyPlatforms.count { it.platformId in selectedPlatformIds.value }
                val totalCount = familyPlatforms.size

                FilterChip(
                    selected = selectedFamily == family,
                    onClick = { selectedFamily = family },
                    label = {
                        Text(
                            "${com.carlosarancibia.playfit.model.familyDisplayName(family)} ($selectedCount/$totalCount)",
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PlayfitExtendedTheme.colors.playfitAccent,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }

        Spacer(Modifier.height(PlayfitSpacing.md))

        // Platform list for selected family
        SettingsSection(title = com.carlosarancibia.playfit.model.familyDisplayName(selectedFamily)) {
            val familyPlatforms = availablePlatforms
                .filter { it.family == selectedFamily }
                .sortedBy { it.sortOrder }
            val consoles = familyPlatforms.filter { it.kind != "handheld" }
            val handhelds = familyPlatforms.filter { it.kind == "handheld" }

            Column {
                if (consoles.isNotEmpty()) {
                    if (handhelds.isNotEmpty()) {
                        Text(
                            text = "Console / Hybrid",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = PlayfitSpacing.xs, vertical = PlayfitSpacing.xs),
                        )
                    }
                    consoles.forEach { platform ->
                        PlatformRow(
                            name = platform.displayName,
                            isSelected = platform.platformId in selectedPlatformIds.value,
                            onToggle = {
                                val ids = if (platform.platformId in selectedPlatformIds.value)
                                    selectedPlatformIds.value - platform.platformId
                                else selectedPlatformIds.value + platform.platformId
                                persist(ids)
                            },
                        )
                    }
                }
                if (handhelds.isNotEmpty()) {
                    Spacer(Modifier.height(PlayfitSpacing.sm))
                    Text(
                        text = "Handheld",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = PlayfitSpacing.xs, vertical = PlayfitSpacing.xs),
                    )
                    handhelds.forEach { platform ->
                        PlatformRow(
                            name = platform.displayName,
                            isSelected = platform.platformId in selectedPlatformIds.value,
                            onToggle = {
                                val ids = if (platform.platformId in selectedPlatformIds.value)
                                    selectedPlatformIds.value - platform.platformId
                                else selectedPlatformIds.value + platform.platformId
                                persist(ids)
                            },
                        )
                    }
                }
                if (familyPlatforms.isEmpty()) {
                    Text(
                        text = "No platforms available in this category.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(PlayfitSpacing.sm),
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun PlatformRow(
    name: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = PlayfitSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = PlayfitExtendedTheme.colors.playfitAccent,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}
