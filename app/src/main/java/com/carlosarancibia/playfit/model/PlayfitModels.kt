package com.carlosarancibia.playfit.model

import java.net.URL

// Domain models (UI layer) — kept for backward compatibility

data class Game(
    val id: String,
    val title: String,
    val platforms: List<String>,
    val genres: List<String>,
    val tags: List<String>,
    val criticScore: Double? = null,
    val playtimeHours: Int? = null,
)

enum class PlayStatus {
    Backlog,
    Playing,
    Completed,
    Dropped,
}

data class UserGameState(
    val status: PlayStatus? = null,
    val rating: Double? = null,
    val isPicked: Boolean = false,
    val isWishlist: Boolean = false,
)

data class RecommendationReason(
    val id: String,
    val label: String,
    val detail: String,
    val weight: Double,
)

data class PlayRecommendation(
    val game: Game,
    val fitScore: Double,
    val reasons: List<RecommendationReason>,
    val state: UserGameState = UserGameState(),
) {
    val id: String = game.id
}

data class TasteProfile(
    val favoriteGenres: List<String>,
    val favoriteTags: List<String>,
    val platformFocus: List<String>,
    val confidence: Double,
)
