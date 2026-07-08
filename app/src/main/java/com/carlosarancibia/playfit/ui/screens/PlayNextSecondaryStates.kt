package com.carlosarancibia.playfit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.components.design.ShimmerBox
import com.carlosarancibia.playfit.ui.components.design.ShimmerCard
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

@Composable
fun PlayNextLoading(showSlowLoading: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PlayfitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
        ) {
            item {
                Spacer(Modifier.height(PlayfitSpacing.lg))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(28.dp),
                )
                Spacer(Modifier.height(8.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                )
                if (showSlowLoading) {
                    Spacer(Modifier.height(PlayfitSpacing.sm))
                    Text(
                        text = "This is taking a little longer. Playfit is still checking your profile and platform matches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(PlayfitSpacing.md))
            }
            items(3) {
                ShimmerCard()
            }
        }
    }
}

@Composable
fun PlayNextError(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PlayfitSpacing.lg),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Play Next could not load",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            Text(
                text = "The catalog connection failed. Please try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(PlayfitSpacing.lg))
            Button(onClick = onRetry) {
                Text("Try again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PlayNextRefreshingCard() {
    PlayfitGlassCard {
        Column(modifier = Modifier.padding(PlayfitSpacing.md)) {
            Text(
                text = "Refreshing recommendations...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(PlayfitSpacing.xs))
            Text(
                text = "Your action is being saved. Playfit is finding the next candidate in the background.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun PlayNextEmpty(
    excludedCount: Int = 0,
    onShowSkipped: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    hasPlatforms: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PlayfitSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No games to recommend yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(PlayfitSpacing.xs))
        Text(
            text = if (hasPlatforms)
                "Try adding more platforms or rating more games so we can find a recommendation."
            else
                "Select your gaming platforms so we can find recommendations for you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(PlayfitSpacing.md))
        Button(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PlayfitExtendedTheme.colors.playfitAccent,
            ),
        ) {
            Text(
                text = if (hasPlatforms) "Manage Platforms" else "Add Platforms",
                fontWeight = FontWeight.Bold,
            )
        }

        if (excludedCount > 0) {
            Spacer(Modifier.height(PlayfitSpacing.md))
            Text(
                text = "All current candidates were skipped in this session.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        RoundedCornerShape(10.dp),
                    )
                    .padding(10.dp),
            )
            Spacer(Modifier.height(PlayfitSpacing.sm))
            OutlinedButton(onClick = onShowSkipped) {
                Text("Show skipped again", fontWeight = FontWeight.Bold)
            }
        }
    }
}
