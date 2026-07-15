package com.carlosarancibia.playfit.ui.components.design

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

@Composable
fun GlowBackground(
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val accentColor = PlayfitExtendedTheme.colors.playfitAccent
    
    // Bottom-left sky blue glow, matching iOS implementation!
    val toneColor = PlayfitExtendedTheme.colors.playfitToneAccent

    val baseBgColor = MaterialTheme.colorScheme.background

    // Smooth ambient background animations matching Web's pulse-slow
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw solid background base first (ensures opacity & color base)
        drawRect(color = baseBgColor)

        // Draw top-right glow (orange-red accent)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accentColor.copy(alpha = if (isDark) PlayfitOpacities.soft else PlayfitOpacities.faint),
                    accentColor.copy(alpha = PlayfitOpacities.zero)
                ),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.05f),
                radius = size.width * 0.8f * pulseScale
            ),
            radius = size.width * 0.8f * pulseScale,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.05f)
        )

        // Draw bottom-left glow (green positive)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    toneColor.copy(alpha = if (isDark) PlayfitOpacities.subtle else PlayfitOpacities.faint),
                    toneColor.copy(alpha = PlayfitOpacities.zero)
                ),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.05f, size.height * 0.95f),
                radius = size.width * 0.7f * (2.0f - pulseScale)
            ),
            radius = size.width * 0.7f * (2.0f - pulseScale),
            center = androidx.compose.ui.geometry.Offset(size.width * 0.05f, size.height * 0.95f)
        )
    }
}
