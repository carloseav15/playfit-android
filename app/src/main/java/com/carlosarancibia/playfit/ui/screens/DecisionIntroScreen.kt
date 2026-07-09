package com.carlosarancibia.playfit.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.components.ThemePickerButton

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
    val isDark = isSystemInDarkTheme()

    // Fetch coverUrl for Hades dynamically
    var hadesCoverUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        try {
            val results = onSearchGames(mockGameTitle)
            val hades = results.find { it.title.equals(mockGameTitle, ignoreCase = true) } ?: results.firstOrNull()
            hadesCoverUrl = hades?.externalCoverUrl ?: hades?.coverPath
        } catch (_: Exception) {}
        if (hadesCoverUrl.isNullOrEmpty()) {
            hadesCoverUrl = "https://vhhnwjuwqbspvllvppnn.supabase.co/storage/v1/object/public/game-covers/hades.jpg"
        }
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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PlayfitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
        ) {

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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.lg)
            ) {
                    
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
                        Button(
                            onClick = onStartCalibration,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .semantics { contentDescription = "playfit.intro.start" },
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Find What to Play",
                                style = MaterialTheme.typography.titleSmall,
                            )
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
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = signInContainerColor,
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                            shape = MaterialTheme.shapes.large,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Login,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onBackground,
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
                    val previewAccent = PlayfitExtendedTheme.colors.playfitAccent
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            previewAccent.copy(alpha = 0.30f),
                                            previewAccent.copy(alpha = 0.0f),
                                        ),
                                    ),
                                    radius = size.width * 0.30f,
                                    center = Offset(size.width, 0f),
                                )
                            }
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                shape = MaterialTheme.shapes.extraLarge,
                            )
                            .padding(16.dp)
                    ) {
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
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(12.dp)
                                            .scale(sparklesScale),
                                        tint = PlayfitExtendedTheme.colors.playfitAccent,
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

            Spacer(Modifier.height(PlayfitSpacing.xxl))
        }
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


