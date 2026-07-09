package com.carlosarancibia.playfit.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

@Composable
fun OnboardingHeader(
    currentStep: Int,
    selectedPlatformsCount: Int,
    likedCount: Int,
    dislikedCount: Int
) {
    val stepData = listOf(
        Triple("Platforms", "$selectedPlatformsCount selected", 0),
        Triple("Loved Games", "$likedCount/3", 1),
        Triple("Missed Game", "$dislikedCount/1", 2)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PlayfitSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stepData.forEach { (label, count, stepIndex) ->
            val isCompleted = currentStep > stepIndex
            val isActive = currentStep == stepIndex

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                // Horizontal track bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(PlayfitExtendedTheme.colors.playfitPositive)
                        )
                    } else if (isActive) {
                        val infiniteTransition = rememberInfiniteTransition(label = "progressBarPulse")
                        val pulseAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseAlpha"
                        )
                        val activeBrush = Brush.linearGradient(
                            colors = listOf(
                                PlayfitExtendedTheme.colors.playfitAccent,
                                Color(0xFFEC4899)
                            )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(brush = activeBrush, alpha = pulseAlpha)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                val labelColor = when {
                    isActive -> PlayfitExtendedTheme.colors.playfitAccent
                    isCompleted -> PlayfitExtendedTheme.colors.playfitPositive
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                }

                Text(
                    text = label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = labelColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = count,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
