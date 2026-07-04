package com.carlosarancibia.playfit.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitGlassCard
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme

private val mockGameId = "mock-hades"
private val mockGameTitle = "Hades"
private val mockGenre = "Roguelike"
private val mockDescription = "Defy the god of the dead in a hack-and-slash underworld escape."
private val mockVibeFit = 96
private val mockWhyMatches = "High action affinity"
private val mockWatchOut = "Repetitive run loops"
private val mockConfidence = "High"

@Composable
fun DecisionIntroScreen(
    onStartCalibration: () -> Unit,
    onSignIn: (() -> Unit)? = null,
    themeMode: String = "system",
    onThemeChange: (String) -> Unit = {},
    onSearchGames: suspend (String) -> List<SeedGame> = { emptyList() },
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f

    // Fetch coverUrl for Hades dynamically
    var hadesCoverUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try {
            val results = onSearchGames(mockGameTitle)
            val hades = results.find { it.title.equals(mockGameTitle, ignoreCase = true) } ?: results.firstOrNull()
            hadesCoverUrl = hades?.externalCoverUrl ?: hades?.coverPath
        } catch (_: Exception) {}
    }

    // Sparkles pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "sparklesPulse")
    val sparklesScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparklesScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PlayfitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
        ) {
            Spacer(Modifier.height(PlayfitSpacing.sm))

            // ── Top Header Bar ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(com.carlosarancibia.playfit.R.drawable.playfit_logo),
                            contentDescription = "Playfit Brand Logo",
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit,
                        )
                    }
                    Text(
                        text = "PLAYFIT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = PlayfitExtendedTheme.colors.playfitAccent,
                    )
                }

                ThemePickerButton(
                    currentTheme = themeMode,
                    onThemeChange = onThemeChange,
                )
            }

            // ── Unified Parent Glass Card ──
            PlayfitGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.lg)) {
                    
                    // Hero section content
                    Column {
                        // Title: "Your next game, curated."
                        Text(
                            text = "Your next game,",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-0.5).sp,
                                lineHeight = MaterialTheme.typography.headlineLarge.lineHeight,
                            ),
                        )

                        val curatedGradient = Brush.linearGradient(
                            colors = listOf(
                                PlayfitExtendedTheme.colors.playfitAccent,
                                Color(0xFFEC4899), // pink-500 equivalent color
                            ),
                        )
                        Text(
                            text = "curated.",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                brush = curatedGradient,
                                letterSpacing = (-0.5).sp,
                                lineHeight = MaterialTheme.typography.headlineLarge.lineHeight,
                            ),
                        )

                        Spacer(Modifier.height(PlayfitSpacing.sm))

                        Text(
                            text = "Select your platforms, three favorites, and one notable miss. Get one clear recommendation with its complete decision analysis.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp,
                            ),
                        )

                        Text(
                            text = "Zero noise. Zero decision fatigue.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            modifier = Modifier.padding(top = PlayfitSpacing.xs),
                        )

                        Spacer(Modifier.height(PlayfitSpacing.md))

                        // ── "Find What to Play" button ──
                        val findButtonGradient = if (isDark) {
                            Brush.linearGradient(
                                colors = listOf(
                                    PlayfitExtendedTheme.colors.playfitAccent,
                                    PlayfitExtendedTheme.colors.playfitIndigo,
                                ),
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    PlayfitExtendedTheme.colors.playfitAccent,
                                    PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.85f),
                                ),
                            )
                        }

                        Button(
                            onClick = onStartCalibration,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .semantics { contentDescription = "playfit.intro.start" },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (isDark) 12.dp else 6.dp,
                            ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(findButtonGradient, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    CompassIcon(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Find What to Play",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                        ),
                                    )
                                }
                            }
                        }

                        // ── "Sign In" button ──
                        if (onSignIn != null) {
                            Spacer(Modifier.height(PlayfitSpacing.sm))
                            val signInContainerColor = if (isDark)
                                Color(0xFF0F172A).copy(alpha = 0.70f)
                            else
                                Color.White.copy(alpha = 0.72f)

                            Button(
                                onClick = onSignIn,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .semantics { contentDescription = "playfit.intro.signin" },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = signInContainerColor,
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(16.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        LoginIcon(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Sign In",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Separation spacer
                    Spacer(Modifier.height(PlayfitSpacing.xs))

                    // ── Preview section ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(24.dp),
                            )
                            .padding(16.dp)
                    ) {
                        // Inner glow
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.TopEnd)
                                .blur(25.dp)
                                .background(
                                    PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.15f),
                                    CircleShape,
                                ),
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
                        ) {
                            // Header row: "Playfit Curation" + "% Vibe Fit"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SparklesIcon(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .scale(sparklesScale),
                                        color = PlayfitExtendedTheme.colors.playfitAccent
                                    )
                                    Text(
                                        text = "PLAYFIT CURATION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PlayfitExtendedTheme.colors.playfitAccent,
                                            letterSpacing = 1.5.sp,
                                        ),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.05f))
                                        .border(
                                            width = 1.dp,
                                            color = PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(20.dp),
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "${mockVibeFit}% Vibe Fit",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PlayfitExtendedTheme.colors.playfitAccent,
                                        ),
                                    )
                                }
                            }

                            // Game info row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PlayfitCoverArt(
                                    gameId = mockGameId,
                                    title = mockGameTitle,
                                    coverUrl = hadesCoverUrl,
                                    modifier = Modifier.width(72.dp),
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = mockGenre.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            letterSpacing = 1.sp,
                                        ),
                                    )
                                    Text(
                                        text = mockGameTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        ),
                                    )
                                    Text(
                                        text = mockDescription,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }

                            // Divider
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )

                            // Reason rows
                            Column(
                                verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.xs),
                            ) {
                                PreviewSignalRow(
                                    label = "Why it matches",
                                    value = mockWhyMatches,
                                    color = PlayfitExtendedTheme.colors.playfitPositive,
                                )
                                PreviewSignalRow(
                                    label = "Watch-outs",
                                    value = mockWatchOut,
                                    color = PlayfitExtendedTheme.colors.playfitWarning, // aligned warning color
                                )
                                PreviewSignalRow(
                                    label = "Confidence",
                                    value = null,
                                    color = PlayfitExtendedTheme.colors.playfitAccent,
                                    badge = mockConfidence,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(PlayfitSpacing.xxl))
        }
    }
}

// ── Programmatic Sparkles Icon Composable ──
@Composable
fun SparklesIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            quadraticTo(w / 2f, h / 2f, w, h / 2f)
            quadraticTo(w / 2f, h / 2f, w / 2f, h)
            quadraticTo(w / 2f, h / 2f, 0f, h / 2f)
            quadraticTo(w / 2f, h / 2f, w / 2f, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

// ── Programmatic Sun Icon Composable ──
@Composable
fun SunIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f)
        val radius = w * 0.25f
        drawCircle(color = color, radius = radius, center = center)
        val rayLength = w * 0.12f
        val rayThickness = w * 0.06f
        for (i in 0 until 8) {
            val angle = i * Math.PI / 4
            val startX = (w / 2f + Math.cos(angle) * (radius + 2.dp.toPx())).toFloat()
            val startY = (h / 2f + Math.sin(angle) * (radius + 2.dp.toPx())).toFloat()
            val endX = (w / 2f + Math.cos(angle) * (radius + 2.dp.toPx() + rayLength)).toFloat()
            val endY = (h / 2f + Math.sin(angle) * (radius + 2.dp.toPx() + rayLength)).toFloat()
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(startX, startY),
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = rayThickness,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

// ── Programmatic Moon Icon Composable ──
@Composable
fun MoonIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.15f)
            cubicTo(w * 0.85f, h * 0.15f, w * 0.85f, h * 0.85f, w * 0.35f, h * 0.85f)
            cubicTo(w * 0.65f, h * 0.70f, w * 0.65f, h * 0.30f, w * 0.35f, h * 0.15f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

// ── Programmatic Compass Icon Composable ──
@Composable
fun CompassIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = androidx.compose.ui.geometry.Offset(w / 2f, h / 2f)
        val radius = w * 0.42f
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.08f)
        )
        val needlePath = Path().apply {
            moveTo(w / 2f, h * 0.23f)
            lineTo(w * 0.64f, h / 2f)
            lineTo(w / 2f, h * 0.77f)
            lineTo(w * 0.36f, h / 2f)
            close()
        }
        drawPath(path = needlePath, color = color)
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(w / 2f, h * 0.23f),
            end = androidx.compose.ui.geometry.Offset(w / 2f, h * 0.77f),
            strokeWidth = w * 0.04f
        )
    }
}

// ── Programmatic Login Icon Composable ──
@Composable
fun LoginIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val bracketPath = Path().apply {
            moveTo(w * 0.70f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.15f)
            lineTo(w * 0.40f, h * 0.35f)
            moveTo(w * 0.40f, h * 0.65f)
            lineTo(w * 0.40f, h * 0.85f)
            lineTo(w * 0.70f, h * 0.85f)
        }
        drawPath(
            path = bracketPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = w * 0.08f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
        val arrowPath = Path().apply {
            moveTo(w * 0.15f, h / 2f)
            lineTo(w * 0.60f, h / 2f)
            moveTo(w * 0.43f, h * 0.33f)
            lineTo(w * 0.60f, h / 2f)
            lineTo(w * 0.43f, h * 0.67f)
        }
        drawPath(
            path = arrowPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = w * 0.08f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

// ── Preview Signal Row ──
@Composable
private fun PreviewSignalRow(
    label: String,
    value: String?,
    color: Color,
    badge: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape),
        )
        Spacer(Modifier.width(PlayfitSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(Modifier.weight(1f))
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
    }
}

// ── Theme Picker Button ──
@Composable
private fun ThemePickerButton(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center,
            ) {
                when (currentTheme) {
                    "light" -> SunIcon(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    "dark" -> MoonIcon(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System theme selection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                leadingIcon = { SunIcon(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                text = {
                    Text(
                        "Light",
                        fontWeight = if (currentTheme == "light") FontWeight.Bold else FontWeight.Normal,
                    )
                },
                onClick = {
                    onThemeChange("light")
                    expanded = false
                },
            )
            DropdownMenuItem(
                leadingIcon = { MoonIcon(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) },
                text = {
                    Text(
                        "Dark",
                        fontWeight = if (currentTheme == "dark") FontWeight.Bold else FontWeight.Normal,
                    )
                },
                onClick = {
                    onThemeChange("dark")
                    expanded = false
                },
            )
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp)) },
                text = {
                    Text(
                        "System",
                        fontWeight = if (currentTheme == "system") FontWeight.Bold else FontWeight.Normal,
                    )
                },
                onClick = {
                    onThemeChange("system")
                    expanded = false
                },
            )
        }
    }
}
