package com.carlosarancibia.playfit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class ReasonTone {
    Accent, Warning, Muted;
}

@Composable
fun ReasonTone.colors(): Pair<Color, Color> {
    return when (this) {
        ReasonTone.Accent -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary
        ReasonTone.Warning -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.error
        ReasonTone.Muted -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun ReasonList(
    title: String,
    reasons: List<String>,
    modifier: Modifier = Modifier,
    tone: ReasonTone = ReasonTone.Accent,
    maxItems: Int = 4,
) {
    val visible = reasons.take(maxItems).ifEmpty {
        listOf("No specific reasons available.")
    }
    val (titleColor, markerColor) = tone.colors()
    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.70f)
                    else Color.White.copy(alpha = 0.72f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                )
                drawRoundRect(
                    color = primaryColor.copy(alpha = if (isDark) 0.18f else 0.10f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                )
            }
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            Spacer(Modifier.height(8.dp))
            visible.forEach { reason ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 2.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(markerColor),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}
