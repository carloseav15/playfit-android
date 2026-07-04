package com.carlosarancibia.playfit.ui.components.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(PlayfitExtendedTheme.colors.playfitInk)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
