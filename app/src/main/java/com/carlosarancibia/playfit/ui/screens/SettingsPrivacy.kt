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
import com.carlosarancibia.playfit.ui.components.design.MoonIcon
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.SparklesIcon
import com.carlosarancibia.playfit.ui.components.design.SunIcon
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.viewmodel.PlayfitViewModel


@Composable
fun PrivacySettingsView(
    onBack: () -> Unit,
    onResetTaste: (() -> Unit)?,
    onDeleteAccount: () -> Unit = {},
    canDeleteAccount: Boolean = false,
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

        if (canDeleteAccount) {
            SettingsSection(title = "Delete Cloud Account") {
                Text(
                    text = "Permanently deletes your cloud Playfit profile, including synchronized taste, picks, and decision history. Your sign-in credentials are managed by your auth provider and are not deleted here.",
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
                    "This action is irreversible. Your cloud Playfit profile and associated app data will be permanently deleted. Your sign-in credentials are not deleted.",
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
