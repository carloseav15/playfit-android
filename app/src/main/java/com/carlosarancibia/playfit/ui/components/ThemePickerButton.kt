package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.model.ThemeMode

@Composable
fun ThemePickerButton(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                when (currentTheme) {
                    ThemeMode.Light -> Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = "Light mode",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ThemeMode.Dark -> Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = "Dark mode",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ThemeMode.System -> Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System theme selection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = "Option: Light mode",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                text = {
                    Text(
                        "Light",
                        fontWeight = if (currentTheme == ThemeMode.Light) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                onClick = {
                    onThemeChange(ThemeMode.Light)
                    expanded = false
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = "Option: Dark mode",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                text = {
                    Text(
                        "Dark",
                        fontWeight = if (currentTheme == ThemeMode.Dark) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                onClick = {
                    onThemeChange(ThemeMode.Dark)
                    expanded = false
                },
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Option: System mode",
                        modifier = Modifier.size(18.dp),
                    )
                },
                text = {
                    Text(
                        "System",
                        fontWeight = if (currentTheme == ThemeMode.System) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                onClick = {
                    onThemeChange(ThemeMode.System)
                    expanded = false
                },
            )
        }
    }
}
