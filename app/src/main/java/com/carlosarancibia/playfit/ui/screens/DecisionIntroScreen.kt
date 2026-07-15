package com.carlosarancibia.playfit.ui.screens

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.ThemeMode
import com.carlosarancibia.playfit.ui.components.design.GlowBackground
import com.carlosarancibia.playfit.ui.components.design.PlayfitCoverArt
import com.carlosarancibia.playfit.ui.components.design.PlayfitSpacing
import com.carlosarancibia.playfit.ui.theme.PlayfitExtendedTheme
import com.carlosarancibia.playfit.ui.components.ThemePickerButton
import com.carlosarancibia.playfit.ui.components.design.PlayfitOpacities

private val mockGameId = "mock-hades"
private val mockGameTitle = "Hades"
private val mockGenre = "Roguelike"
private val mockVibeFit = 96

@Composable
fun DecisionIntroScreen(
    onStartCalibration: () -> Unit,
    onSignIn: (() -> Unit)? = null,
    themeMode: ThemeMode = ThemeMode.System,
    onThemeChange: (ThemeMode) -> Unit = {},
    onSearchGames: suspend (String) -> List<SeedGame> = { emptyList() },
) {
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = PlayfitOpacities.low),
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
                                MaterialTheme.colorScheme.tertiary,
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
                            text = "Tell us where you play and a few games you loved or skipped. We'll find your next fit.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp,
                            ),
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
                            Button(
                                onClick = onSignIn,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .semantics { contentDescription = "playfit.intro.signin" },
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = PlayfitOpacities.soft),
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

                    // Compact example, intentionally secondary to the onboarding CTAs.
                    val previewAccent = PlayfitExtendedTheme.colors.playfitAccent
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PlayfitOpacities.light))
                            .drawBehind {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            previewAccent.copy(alpha = PlayfitOpacities.medium),
                                            previewAccent.copy(alpha = PlayfitOpacities.zero),
                                        ),
                                    ),
                                    radius = size.width * 0.30f,
                                    center = Offset(size.width, 0f),
                                )
                            }
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = PlayfitOpacities.soft),
                                shape = MaterialTheme.shapes.extraLarge,
                            )
                            .padding(PlayfitSpacing.md)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(PlayfitSpacing.sm),
                        ) {
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
                                        modifier = Modifier.size(12.dp),
                                        tint = PlayfitExtendedTheme.colors.playfitAccent,
                                    )
                                    Text(
                                        text = "PLAYFIT CURATION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = PlayfitExtendedTheme.colors.playfitAccent,
                                        ),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.extraLarge)
                                        .background(PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = PlayfitOpacities.faint))
                                        .border(
                                            width = 1.dp,
                                            color = PlayfitExtendedTheme.colors.playfitAccent.copy(alpha = PlayfitOpacities.medium),
                                            shape = MaterialTheme.shapes.extraLarge,
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "${mockVibeFit}% Vibe Fit",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PlayfitExtendedTheme.colors.playfitAccent,
                                        ),
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(PlayfitSpacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PlayfitCoverArt(
                                    gameId = mockGameId,
                                    title = mockGameTitle,
                                    coverUrl = hadesCoverUrl,
                                    modifier = Modifier.width(48.dp),
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = mockGenre.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PlayfitOpacities.prominent),
                                        ),
                                    )
                                    Text(
                                        text = mockGameTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        ),
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
