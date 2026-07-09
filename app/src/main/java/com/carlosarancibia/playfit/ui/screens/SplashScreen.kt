package com.carlosarancibia.playfit.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import kotlinx.coroutines.delay

private val LOGO_SIZE = 120.dp

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    reduceMotion: Boolean = false,
) {
    var startAnimation by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var startGlow by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.82f,
        animationSpec = if (reduceMotion) {
            tween(durationMillis = 0)
        } else {
            spring(dampingRatio = 0.68f, stiffness = 240f)
        },
        label = "logoScale",
    )
    val logoOpacity by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = if (reduceMotion) tween(0) else tween(durationMillis = 550),
        label = "logoOpacity",
    )
    val glowOpacity by animateFloatAsState(
        targetValue = if (startGlow) 1f else 0f,
        animationSpec = if (reduceMotion) tween(0) else tween(
            durationMillis = 800,
            easing = androidx.compose.animation.core.FastOutLinearInEasing,
        ),
        label = "glowOpacity",
    )
    val titleOpacity by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = if (reduceMotion) tween(0) else tween(
            durationMillis = 350,
            easing = androidx.compose.animation.core.FastOutLinearInEasing,
        ),
        label = "titleOpacity",
    )

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            startAnimation = true
            startGlow = true
            showTitle = true
            delay(500)
            onFinished()
        } else {
            startAnimation = true
            startGlow = true
            delay(350)
            showTitle = true
            delay(650)
            onFinished()
        }
    }

    val accent = PlayfitExtendedTheme.colors.playfitAccent
    val toneAccent = PlayfitExtendedTheme.colors.playfitToneAccent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(glowOpacity)
            .background(MaterialTheme.colorScheme.background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.35f),
                            accent.copy(alpha = 0.0f),
                        ),
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width, 0f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            toneAccent.copy(alpha = 0.20f),
                            toneAccent.copy(alpha = 0.0f),
                        ),
                    ),
                    radius = size.width * 0.75f,
                    center = Offset(0f, size.height),
                )
            }
            .semantics { contentDescription = "playfit.splash" },
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Image(
                painter = painterResource(com.carlosarancibia.playfit.R.drawable.playfit_logo),
                contentDescription = "Playfit",
                modifier = Modifier
                    .size(LOGO_SIZE)
                    .scale(logoScale)
                    .alpha(logoOpacity),
                contentScale = ContentScale.Fit,
            )

            Spacer(Modifier.height(PlayfitSpacing.md))

            Text(
                text = "Playfit",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp,
                ),
                modifier = Modifier.alpha(titleOpacity),
            )
        }
    }
}
