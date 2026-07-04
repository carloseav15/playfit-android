package com.carlosarancibia.playfit.ui.components.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SignalSummaryBar(
    preferencesCount: Int,
    likedCount: Int,
    avoidedCount: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$preferencesCount preferences / $likedCount liked / $avoidedCount avoided",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
