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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel

private enum class SettingsViewMode {
    Main, Appearance, Platforms, Privacy, Developer,
}

@Composable
fun SettingsScreen(
    viewModel: PlayfitViewModel? = null,
    hasProfile: Boolean = true,
    onResetTaste: (() -> Unit)? = null,
    onNavigateToPlayNext: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
    var viewMode by remember { mutableStateOf(SettingsViewMode.Main) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (viewMode) {
            SettingsViewMode.Main -> SettingsMainView(
                hasProfile = hasProfile,
                onOpenAppearance = { viewMode = SettingsViewMode.Appearance },
                onOpenPlatforms = { viewMode = SettingsViewMode.Platforms },
                onOpenPrivacy = { viewMode = SettingsViewMode.Privacy },
                onOpenDeveloper = { viewMode = SettingsViewMode.Developer },
                showResetDialog = showResetDialog,
                showDeleteDialog = showDeleteDialog,
                onShowResetDialog = { showResetDialog = true },
                onShowDeleteDialog = { showDeleteDialog = true },
                onDismissResetDialog = { showResetDialog = false },
                onDismissDeleteDialog = { showDeleteDialog = false },
                onResetTaste = onResetTaste,
                viewModel = viewModel,
                onNavigateToPlayNext = onNavigateToPlayNext,
                onDeleteAccount = onDeleteAccount,
            )
            SettingsViewMode.Appearance -> AppearanceView(
                onBack = { viewMode = SettingsViewMode.Main },
                viewModel = viewModel,
            )
            SettingsViewMode.Platforms -> PlatformSelectionView(
                onBack = { viewMode = SettingsViewMode.Main },
                viewModel = viewModel,
            )
            SettingsViewMode.Privacy -> PrivacySettingsView(
                onBack = { viewMode = SettingsViewMode.Main },
                onResetTaste = onResetTaste,
                onDeleteAccount = onDeleteAccount,
            )
            SettingsViewMode.Developer -> DeveloperSettingsView(
                onBack = { viewMode = SettingsViewMode.Main },
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun SettingsMainView(
    hasProfile: Boolean,
    onOpenAppearance: () -> Unit,
    onOpenPlatforms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenDeveloper: () -> Unit,
    showResetDialog: Boolean,
    showDeleteDialog: Boolean,
    onShowResetDialog: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onDismissResetDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onResetTaste: (() -> Unit)?,
    viewModel: PlayfitViewModel?,
    onNavigateToPlayNext: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlayfitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
    ) {
        Spacer(Modifier.height(PlayfitSpacing.lg))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (!hasProfile) {
            SettingsSection(title = "Set up your taste first") {
                Text(
                    text = "Select your platforms and a few favorite games so we can build your recommendations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(PlayfitSpacing.md))
                Button(onClick = onNavigateToPlayNext) {
                    Text("Start Play Next", fontWeight = FontWeight.Bold)
                }
            }
            return
        }

        SettingsNavRow(title = "App Appearance", subtitle = "Light, Dark, or System", onClick = onOpenAppearance)
        SettingsNavRow(title = "Your Platforms", subtitle = "Manage your active gaming platforms", onClick = onOpenPlatforms)
        SettingsNavRow(title = "Data & Privacy", subtitle = "Reset profile or delete account", onClick = onOpenPrivacy)

        SettingsSection(title = "Account") {
            Text(
                text = "Guest session",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Save your library and preferences to access them from any device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            OutlinedButton(
                onClick = { viewModel?.linkGoogleAccount() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Link Google", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = {
                    viewModel?.signOutAsync()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PlayfitExtendedTheme.colors.playfitNegative,
                ),
            ) {
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        }

        SettingsNavRow(title = "Developer Settings", subtitle = "API environment, debug info", onClick = onOpenDeveloper)

        SettingsSection(title = "About") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SparklesIcon(
                    modifier = Modifier.size(16.dp),
                    color = PlayfitExtendedTheme.colors.playfitAccent
                )
                Spacer(Modifier.width(PlayfitSpacing.sm))
                Text(
                    text = "Playfit for Android",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(PlayfitSpacing.sm))
                Text(
                    text = "Built with Jetpack Compose",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        Spacer(Modifier.height(PlayfitSpacing.xxl))
    }

    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissResetDialog,
            title = { Text("Confirm Reset", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will clear all your taste preferences, ratings, and library. You'll need to set up again from the intro screen.",
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissResetDialog()
                        onResetTaste?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlayfitExtendedTheme.colors.playfitNegative,
                    ),
                ) {
                    Text("Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissResetDialog) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("Confirm Delete", fontWeight = FontWeight.Bold) },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissDeleteDialog()
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlayfitExtendedTheme.colors.playfitNegative,
                    ),
                ) {
                    Text("Confirm Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PlayfitSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(PlayfitSpacing.sm))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(PlayfitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            content()
        }
    }
}

@Composable
private fun SubViewTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PlayfitSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.width(PlayfitSpacing.sm))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun AppearanceView(
    onBack: () -> Unit,
    viewModel: PlayfitViewModel? = null,
) {
    val savedTheme by viewModel?.preferencesDataStore?.themeMode
        ?.collectAsState(initial = "system") ?: remember { mutableStateOf("system") }
    var currentTheme by remember { mutableStateOf(savedTheme) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(savedTheme) {
        currentTheme = savedTheme
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlayfitSpacing.md),
    ) {
        SubViewTopBar(title = "App Appearance", onBack = onBack)
        Spacer(Modifier.height(PlayfitSpacing.sm))
        Text(
            text = "Choose your preferred theme for the interface.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.md))
        SettingsSection(title = "Theme") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Light" to "light",
                    "Dark" to "dark",
                    "System" to "system",
                ).forEach { (label, value) ->
                    val isSelected = currentTheme == value
                    Button(
                        onClick = {
                            currentTheme = value
                            viewModel?.let { vm ->
                                scope.launch {
                                    vm.preferencesDataStore.setThemeMode(value)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) PlayfitExtendedTheme.colors.playfitAccent
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            when (value) {
                                "light" -> SunIcon(modifier = Modifier.size(16.dp), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                "dark" -> MoonIcon(modifier = Modifier.size(14.dp), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                else -> Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(PlayfitSpacing.md))
        Text(
            text = "System theme follows your device settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = PlayfitSpacing.sm),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlatformSelectionView(
    onBack: () -> Unit,
    viewModel: PlayfitViewModel? = null,
) {
    val platforms = com.carlosarancibia.playfit.model.fallbackPlatforms
    val persistedIds by viewModel?.preferencesDataStore?.selectedPlatformIds
        ?.collectAsState(initial = emptySet()) ?: remember { mutableStateOf(emptySet()) }
    val selectedPlatformIds = remember { mutableStateOf(persistedIds.toMutableSet()) }
    LaunchedEffect(persistedIds) {
        if (selectedPlatformIds.value != persistedIds) {
            selectedPlatformIds.value = persistedIds.toMutableSet()
        }
    }

    fun persist(ids: Set<String>) {
        if (ids.isEmpty()) {
            viewModel?.updatePlatforms(ids)
            return
        }
        selectedPlatformIds.value = ids.toMutableSet()
        viewModel?.updatePlatforms(ids)
    }

    var selectedFamily by remember { mutableStateOf("nintendo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlayfitSpacing.md),
    ) {
        SubViewTopBar(title = "Your Platforms", onBack = onBack)
        Spacer(Modifier.height(PlayfitSpacing.sm))
        Text(
            text = "Recommendations are only shown for games available on your active platforms. Changes save automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.md))

        // Quick Groups / Presets
        SettingsSection(title = "Quick Groups") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                com.carlosarancibia.playfit.model.platformPresets.forEach { preset ->
                    val presetPlatforms = platforms.filter(preset.match)
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
                            selectedContainerColor = PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.15f),
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
            val families = com.carlosarancibia.playfit.model.sortedPlatformFamilies(platforms)
            families.forEach { family ->
                val familyPlatforms = platforms.filter { it.family == family }
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
            val familyPlatforms = platforms
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

@Composable
private fun PrivacySettingsView(
    onBack: () -> Unit,
    onResetTaste: (() -> Unit)?,
    onDeleteAccount: () -> Unit = {},
) {
    var showReset by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlayfitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
    ) {
        SubViewTopBar(title = "Data & Privacy", onBack = onBack)

        SettingsSection(title = "Reset Taste Profile") {
            Text(
                text = "Deletes all taste preferences, ratings, and library history. Your active account session stays, and you will restart calibration.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            OutlinedButton(
                onClick = { showReset = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PlayfitExtendedTheme.colors.playfitNegative,
                ),
            ) {
                Text("Reset Profile", fontWeight = FontWeight.Bold)
            }
        }

        SettingsSection(title = "Delete Cloud Account") {
            Text(
                text = "Permanently deletes your account metadata, cloud-synchronized taste, and sign-in credentials from our servers. This action is irreversible.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PlayfitExtendedTheme.colors.playfitNegative,
                ),
            ) {
                Text("Delete Account", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showReset) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Confirm Reset", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will clear all your taste preferences, ratings, and library. You'll need to set up again from the intro screen.",
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReset = false
                        onResetTaste?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlayfitExtendedTheme.colors.playfitNegative,
                    ),
                ) {
                    Text("Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Cancel") }
            },
        )
    }

    if (showDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Confirm Delete", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action is irreversible. Your account and all associated data will be permanently deleted.",
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDelete = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PlayfitExtendedTheme.colors.playfitNegative,
                    ),
                ) {
                    Text("Confirm Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun DeveloperSettingsView(
    onBack: () -> Unit,
    viewModel: PlayfitViewModel? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PlayfitSpacing.md),
    ) {
        SubViewTopBar(title = "Developer Settings", onBack = onBack)
        Spacer(Modifier.height(PlayfitSpacing.sm))

        SettingsSection(title = "Backend Environment") {
            Text(
                text = BuildConfig.BUILD_ENVIRONMENT.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "API: ${BuildConfig.API_BASE_URL}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Environment is selected by the Android build type.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            OutlinedButton(
                onClick = { viewModel?.refreshRecommendations() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Refresh Current Environment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

