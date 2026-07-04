package com.carlosarancibia.playfit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing

@Composable
fun StatusSnackbar(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor = androidx.compose.ui.graphics.Color(0xFF34D399)

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically { -it },
        exit = slideOutVertically { -it },
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PlayfitSpacing.md)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = PlayfitSpacing.md, vertical = PlayfitSpacing.sm),
        ) {
            Text(
                text = "\u2713",
                color = iconColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
