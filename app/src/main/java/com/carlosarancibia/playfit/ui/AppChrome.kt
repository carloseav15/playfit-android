package com.carlosarancibia.playfit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing

internal data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

internal val bottomNavItems = listOf(
    BottomNavItem("play-next", com.carlosarancibia.playfit.R.string.nav_play_next, Icons.Default.PlayArrow),
    BottomNavItem("picks", com.carlosarancibia.playfit.R.string.nav_picks, Icons.Default.FavoriteBorder),
    BottomNavItem("taste", com.carlosarancibia.playfit.R.string.nav_taste, Icons.AutoMirrored.Filled.List),
    BottomNavItem("search", com.carlosarancibia.playfit.R.string.nav_search, Icons.Default.Search),
    BottomNavItem("settings", com.carlosarancibia.playfit.R.string.nav_settings, Icons.Default.Settings),
)

@Composable
internal fun PlayfitBottomBar(
    currentRoute: String?,
    pickCount: Int,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    if (item.route == "picks" && pickCount > 0) {
                        BadgedBox(badge = { Badge { Text("$pickCount", style = MaterialTheme.typography.labelSmall) } }) {
                            Icon(item.icon, contentDescription = label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = label)
                    }
                },
                label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

@Composable
internal fun SyncStatusBar(
    refreshing: Boolean,
    saving: Boolean,
    pendingSync: Boolean,
    showingStaleData: Boolean,
) {
    if (refreshing || saving || pendingSync || showingStaleData) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = PlayfitSpacing.md, vertical = 7.dp),
        ) {
            Text(
                text = when {
                    refreshing -> stringResource(com.carlosarancibia.playfit.R.string.syncing_changes)
                    saving -> stringResource(com.carlosarancibia.playfit.R.string.saving_changes)
                    pendingSync -> stringResource(com.carlosarancibia.playfit.R.string.pending_sync_status)
                    else -> stringResource(com.carlosarancibia.playfit.R.string.stale_data_status)
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
