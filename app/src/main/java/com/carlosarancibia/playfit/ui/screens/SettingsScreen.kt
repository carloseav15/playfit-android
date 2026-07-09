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
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.fallbackPlatforms
import com.carlosarancibia.playfit.ui.components.design.MoonIcon
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.SparklesIcon
import com.carlosarancibia.playfit.ui.components.design.SunIcon
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.viewmodel.AuthState
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitUiState
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel

private enum class SettingsViewMode {
    Main, Appearance, Platforms, Privacy, Developer,
}

@Composable
fun SettingsScreen(
    viewModel: PlayfitViewModel? = null,
    platforms: List<Platform> = fallbackPlatforms,
    platformsLoading: Boolean = false,
    platformsError: String? = null,
    platformsStale: Boolean = false,
    hasProfile: Boolean = true,
    onResetTaste: (() -> Unit)? = null,
    onNavigateToPlayNext: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
) {
    var viewMode by remember { mutableStateOf(SettingsViewMode.Main) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val authState by viewModel?.authState?.collectAsState() ?: remember { mutableStateOf(AuthState()) }
    val uiState by viewModel?.ui?.collectAsState() ?: remember { mutableStateOf(PlayfitUiState()) }

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
                authState = authState,
                pendingSync = uiState.pendingSync,
            )
            SettingsViewMode.Appearance -> AppearanceView(
                onBack = { viewMode = SettingsViewMode.Main },
                viewModel = viewModel,
            )
            SettingsViewMode.Platforms -> PlatformSelectionView(
                onBack = { viewMode = SettingsViewMode.Main },
                viewModel = viewModel,
                platforms = platforms,
                platformsLoading = platformsLoading,
                platformsError = platformsError,
                platformsStale = platformsStale,
            )
            SettingsViewMode.Privacy -> PrivacySettingsView(
                onBack = { viewMode = SettingsViewMode.Main },
                onResetTaste = onResetTaste,
                onDeleteAccount = onDeleteAccount,
                canDeleteAccount = authState.canDeleteAccount,
            )
            SettingsViewMode.Developer -> {
                if (BuildConfig.DEBUG) {
                    DeveloperSettingsView(
                        onBack = { viewMode = SettingsViewMode.Main },
                        viewModel = viewModel,
                    )
                } else {
                    SettingsMainView(
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
                        authState = authState,
                        pendingSync = uiState.pendingSync,
                    )
                }
            }
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
    authState: AuthState,
    pendingSync: Boolean,
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
            style = MaterialTheme.typography.headlineMedium,
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

        AccountSection(
            authState = authState,
            pendingSync = pendingSync,
            onLinkGoogle = { viewModel?.linkGoogleAccount() },
            onSignOut = { viewModel?.signOutAsync() },
        )

        if (BuildConfig.DEBUG) {
            SettingsNavRow(title = "Developer Settings", subtitle = "API environment, debug info", onClick = onOpenDeveloper)
        }

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
private fun AccountSection(
    authState: AuthState,
    pendingSync: Boolean,
    onLinkGoogle: () -> Unit,
    onSignOut: () -> Unit,
) {
    val title = when {
        !authState.isAuthenticated -> "Not signed in"
        authState.isAnonymous -> "Guest profile"
        !authState.email.isNullOrBlank() -> authState.email
        else -> "Playfit account"
    }
    val description = when {
        !authState.isAuthenticated -> "Sign in to sync your library and preferences across devices."
        authState.isAnonymous -> "Your profile is saved on this device and can be linked to Google for cross-device sync."
        pendingSync -> "Signed in. Recent changes are saved on this device and waiting to sync."
        else -> "Signed in. Your library and preferences can sync across devices."
    }

    SettingsSection(title = "Account") {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PlayfitSpacing.sm))
        if (authState.canLinkGoogle) {
            OutlinedButton(
                onClick = onLinkGoogle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Link Google", fontWeight = FontWeight.Bold)
            }
        }
        if (authState.canSignOut) {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PlayfitExtendedTheme.colors.playfitNegative,
                ),
            ) {
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        }
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
        shape = MaterialTheme.shapes.large,
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
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
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
fun SubViewTopBar(title: String, onBack: () -> Unit) {
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
