package com.carlosarancibia.playfit.data

import com.carlosarancibia.playfit.model.Game
import com.carlosarancibia.playfit.model.PlayRecommendation
import com.carlosarancibia.playfit.model.RecommendationReason
import com.carlosarancibia.playfit.model.TasteProfile
import com.carlosarancibia.playfit.model.UserGameState

object MockPlayfitData {
    val recommendations = listOf(
        PlayRecommendation(
            game = Game(
                id = "metroid_prime_remastered",
                title = "Metroid Prime Remastered",
                platforms = listOf("Nintendo Switch"),
                genres = listOf("Adventure", "Shooter"),
                tags = listOf("Atmospheric", "Exploration", "Tight pacing"),
                criticScore = 94.0,
                playtimeHours = 14,
            ),
            fitScore = 0.94,
            reasons = listOf(
                RecommendationReason(
                    id = "pace",
                    label = "Strong pacing fit",
                    detail = "Compact campaign length and high exploration density.",
                    weight = 0.35,
                ),
                RecommendationReason(
                    id = "taste",
                    label = "Matches your taste map",
                    detail = "You respond well to atmospheric action-adventure games.",
                    weight = 0.42,
                ),
            ),
            state = UserGameState(isPicked = true),
        ),
        PlayRecommendation(
            game = Game(
                id = "hades",
                title = "Hades",
                platforms = listOf("Nintendo Switch", "PC", "PlayStation 5"),
                genres = listOf("Action", "Roguelite"),
                tags = listOf("Fast sessions", "Build crafting", "Narrative"),
                criticScore = 93.0,
                playtimeHours = 24,
            ),
            fitScore = 0.89,
            reasons = listOf(
                RecommendationReason(
                    id = "sessions",
                    label = "Great session length",
                    detail = "Short runs make it easy to play in small windows.",
                    weight = 0.31,
                ),
                RecommendationReason(
                    id = "quality",
                    label = "High confidence pick",
                    detail = "Strong critic signal and broad player sentiment.",
                    weight = 0.29,
                ),
            ),
        ),
        PlayRecommendation(
            game = Game(
                id = "outer_wilds",
                title = "Outer Wilds",
                platforms = listOf("Nintendo Switch", "PC", "PlayStation 5"),
                genres = listOf("Adventure", "Puzzle"),
                tags = listOf("Mystery", "Discovery", "Systems"),
                criticScore = 85.0,
                playtimeHours = 18,
            ),
            fitScore = 0.86,
            reasons = listOf(
                RecommendationReason(
                    id = "discovery",
                    label = "Discovery-heavy",
                    detail = "Rewards curiosity without grinding progression.",
                    weight = 0.40,
                ),
            ),
        ),
    )

    val tasteProfile = TasteProfile(
        favoriteGenres = listOf("Adventure", "RPG", "Action"),
        favoriteTags = listOf("Exploration", "Atmospheric", "Strong pacing", "Build crafting"),
        platformFocus = listOf("Nintendo Switch", "PlayStation 5", "PC"),
        confidence = 0.78,
    )
}
