package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

@Composable
fun DossierActionBar(
    isPicked: Boolean,
    isSaving: Boolean,
    onTogglePick: () -> Unit,
    onAlreadyPlayed: () -> Unit,
    onNotForMe: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlayfitSpacing.md, vertical = PlayfitSpacing.sm),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(
            modifier = Modifier.padding(PlayfitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
        ) {
            Button(
                onClick = onTogglePick,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PlayfitExtendedTheme.colors.playfitAccent,
                ),
            ) {
                Text(
                    text = when {
                        isSaving -> "Saving..."
                        isPicked -> "Remove from Picks"
                        else -> "Save to Picks"
                    },
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
            ) {
                OutlinedButton(
                    onClick = onAlreadyPlayed,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Already played", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onNotForMe,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Not for me", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
