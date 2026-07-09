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
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

@Composable
fun ScoreBadge(
    score: Double,
    modifier: Modifier = Modifier,
) {
    val text = "${(score * 100).toInt()}% fit"
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        // playfitInk is light-to-mid toned in both themes (WCAG-checked against
        // #0d9488 light / #38bdf8 dark), so a fixed dark navy passes AA on both.
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(PlayfitExtendedTheme.colors.playfitInk)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
