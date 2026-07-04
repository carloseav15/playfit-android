package com.carlosarancibia.playfit.ui.components.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

@Composable
fun PlayfitGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    
    // Sleek translucent borders
    val strokeColor = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, strokeColor),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) {
                // Sleek translucent dark slate
                Color(0xFF0F172A).copy(alpha = 0.70f)
            } else {
                // Soft translucent light white
                Color.White.copy(alpha = 0.72f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.padding(PlayfitSpacing.lg)) {
            content()
        }
    }
}
