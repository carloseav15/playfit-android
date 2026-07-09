package com.carlosarancibia.playfit.ui.components.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

enum class DecisionTone {
    Positive, Warning, Negative, Info,
}

@Composable
fun DecisionLabelBadge(
    label: String,
    tone: DecisionTone,
    modifier: Modifier = Modifier,
) {
    val foreground = when (tone) {
        DecisionTone.Positive -> PlayfitExtendedTheme.colors.playfitPositive
        DecisionTone.Warning -> PlayfitExtendedTheme.colors.playfitWarning
        DecisionTone.Negative -> PlayfitExtendedTheme.colors.playfitNegative
        DecisionTone.Info -> PlayfitExtendedTheme.colors.playfitToneAccent
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = foreground,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(foreground.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
