package com.carlosarancibia.playfit.model

// ── Enums ──────────────────────────────────────────────────────────────────

enum class ProductAccessStatus {
    Available, Limited, Planned;

    val apiValue: String get() = name.lowercase()

    companion object {
        fun fromApiValue(value: String): ProductAccessStatus? =
            entries.firstOrNull { it.apiValue == value.lowercase() }
    }
}
enum class ThemeMode {
    System, Light, Dark;

    val apiValue: String get() = name.lowercase()

    companion object {
        fun fromApiValue(value: String?): ThemeMode =
            entries.firstOrNull { it.apiValue == value?.lowercase() } ?: System
    }
}
enum class ProductConfidence { Low, Medium, High }
enum class SeedReleaseState { Released, Unreleased }
enum class PlatformAvailability { Available, Unavailable, Unknown }
enum class GameAccessStatus { Playable, NotOnPlatforms, UnknownPlatform, Unreleased }

enum class ProductPlayStatus {
    Playing, OnHold, Shelved, Beaten, Completed, Abandoned, WantToPlay;

    val apiValue: String
        get() = when (this) {
            Playing -> "playing"
            OnHold -> "on_hold"
            Shelved -> "shelved"
            Beaten -> "beaten"
            Completed -> "completed"
            Abandoned -> "abandoned"
            WantToPlay -> "want_to_play"
        }

    companion object {
        fun fromApiValue(value: String): ProductPlayStatus? {
            return entries.firstOrNull {
                it.apiValue == value.lowercase() || it.name.equals(value, ignoreCase = true)
            }
        }
    }
}

enum class ProductDecisionFeedback {
    Play, Later, Loved, Liked, Mixed, NotForMe,
    PlayedLoved, PlayedLiked, PlayedMixed, PlayedDropped;
}

enum class ProductOnboardingStep { Platforms, Anchors, Dislikes }

enum class ProductTasteConfidence { Early, Emerging, Strong }

// ── Core data types ────────────────────────────────────────────────────────

data class SeedGame(
    val gameId: String,
    val title: String,
    val aliases: List<String> = emptyList(),
    val series: String = "",
    val source: String = "catalog",
    val primaryGenre: String = "",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val coverPath: String = "",
    val externalCoverUrl: String? = null,
    val releaseYear: String? = null,
    val availablePlatformIds: List<String> = emptyList(),
    val availablePlatformNames: List<String> = emptyList(),
    val releaseState: SeedReleaseState = SeedReleaseState.Released,
)

data class SimilarGame(
    val gameId: String,
    val title: String,
    val similarity: Double,
)

data class RankedSeedGame(
    val game: SeedGame,
    val affinityScore: Double,
    val riskScore: Double,
    val confidence: ProductConfidence,
    val fitReasons: List<String>,
    val cautionReasons: List<String>,
    val platformAvailability: PlatformAvailability,
    val accessStatus: GameAccessStatus,
    val inBacklog: Boolean = false,
    val inWishlist: Boolean = false,
    val inPlayfitPicks: Boolean = false,
    val similarGames: List<SimilarGame> = emptyList(),
)

// ── API response models ────────────────────────────────────────────────────

data class ProductPlayNextModel(
    val primary: RankedSeedGame?,
    val alternatives: List<RankedSeedGame>,
    val savedPickIds: List<String>,
    val stateVersion: String,
)

// ── Game state ─────────────────────────────────────────────────────────────

data class ProductGameState(
    val gameId: String,
    val title: String,
    val status: ProductPlayStatus? = null,
    val rating: Double? = null,
    val inBacklog: Boolean = false,
    val inWishlist: Boolean = false,
    val inPlayfitPicks: Boolean = false,
    val excluded: Boolean = false,
    val source: String = "manual",
    val createdAt: String = "",
    val updatedAt: String = "",
)

// ── Onboarding ─────────────────────────────────────────────────────────────

data class ProductPlatformSelection(
    val platformId: String,
    val status: ProductAccessStatus,
)

data class ProductOnboardingDraft(
    val step: ProductOnboardingStep = ProductOnboardingStep.Platforms,
    val platforms: List<ProductPlatformSelection> = emptyList(),
    val likedGameIds: List<String> = emptyList(),
    val dislikedGameIds: List<String> = emptyList(),
)

// ── Profile ────────────────────────────────────────────────────────────────

data class ProductProfileSignal(
    val id: String,
    val tone: String,
    val label: String,
    val reason: String,
)

data class ProductProfile(
    val summary: String = "",
    val likedGenres: List<String> = emptyList(),
    val avoidedGenres: List<String> = emptyList(),
    val likedTags: Map<String, Int> = emptyMap(),
    val dislikedTags: Map<String, Int> = emptyMap(),
    val ratedCount: Int = 0,
    val signals: List<ProductProfileSignal> = emptyList(),
)

// ── Global state ───────────────────────────────────────────────────────────

data class ProductUserState(
    val onboarding: ProductOnboardingDraft = ProductOnboardingDraft(),
    val onboardingCompletedAt: String? = null,
    val profile: ProductProfile? = null,
    val gameStates: Map<String, ProductGameState> = emptyMap(),
    val lastUpdatedAt: String? = null,
)

data class ProductState(
    val version: Int = 1,
    val user: ProductUserState = ProductUserState(),
)

// ── Taste ──────────────────────────────────────────────────────────────────

data class ProductTasteHistoryEntry(
    val gameId: String,
    val title: String,
    val decision: String,
    val source: String,
    val tone: String,
    val rating: Double? = null,
    val status: String? = null,
    val updatedAt: String? = null,
    val traits: List<String> = emptyList(),
    val coverUrl: String? = null,
)

data class ProductTasteMapTrait(
    val id: String,
    val label: String,
    val kind: String,
    val positiveCount: Int = 0,
    val negativeCount: Int = 0,
    val netScore: Double = 0.0,
    val strength: Double = 0.0,
    val confidence: String = "Early",
    val direction: String = "neutral",
)

data class ProductTasteModel(
    val evidenceCount: Int = 0,
    val historyEntries: List<ProductTasteHistoryEntry> = emptyList(),
    val mapTraits: List<ProductTasteMapTrait> = emptyList(),
    val positiveCount: Int = 0,
    val negativeCount: Int = 0,
    val confidenceLabel: String = "Still learning",
)

// ── Platform ────────────────────────────────────────────────────────────────

data class Platform(
    val platformId: String,
    val displayName: String,
    val family: String,
    val kind: String = "console",
    val activeStatus: String = "active",
    val sortOrder: Int = 0,
)

val fallbackPlatforms: List<Platform> = listOf(
    Platform("switch_1", "Nintendo Switch", "nintendo", "hybrid", sortOrder = 9),
    Platform("switch_2", "Nintendo Switch 2", "nintendo", "hybrid", sortOrder = 10),
    Platform("ps5", "PlayStation 5", "playstation", "console", sortOrder = 9),
    Platform("ps4", "PlayStation 4", "playstation", "console", sortOrder = 8),
    Platform("xbox_series_xs", "Xbox Series X|S", "xbox", "console", sortOrder = 9),
    Platform("xbox_one", "Xbox One", "xbox", "console", sortOrder = 8),
    Platform("pc", "PC", "pc", "computer", sortOrder = 10),
    Platform("macos", "Mac", "pc", "computer", sortOrder = 9),
    Platform("snes", "Super Nintendo", "nintendo", "console", sortOrder = 4),
    Platform("n64", "Nintendo 64", "nintendo", "console", sortOrder = 5),
    Platform("wii", "Nintendo Wii", "nintendo", "console", sortOrder = 7),
    Platform("ps2", "PlayStation 2", "playstation", "console", sortOrder = 6),
    Platform("ps3", "PlayStation 3", "playstation", "console", sortOrder = 7),
    Platform("xbox_360", "Xbox 360", "xbox", "console", sortOrder = 7),
    Platform("sega_genesis", "SEGA Genesis", "sega", "console", sortOrder = 5),
    Platform("game_boy_advance", "Game Boy Advance", "nintendo", "handheld", sortOrder = 3),
    Platform("ds", "Nintendo DS", "nintendo", "handheld", sortOrder = 6),
    Platform("psp", "PlayStation Portable", "playstation", "handheld", sortOrder = 5),
    Platform("ps_vita", "PlayStation Vita", "playstation", "handheld", sortOrder = 4),
)

data class PlatformPreset(
    val id: String,
    val label: String,
    val description: String,
    val match: (Platform) -> Boolean,
)

val platformPresets: List<PlatformPreset> = listOf(
    PlatformPreset("current", "Current systems", "Modern consoles and computers.") { p ->
        listOf("switch_1", "switch_2", "ps5", "xbox_series_xs", "pc", "macos", "linux", "cups").contains(p.platformId)
    },
    PlatformPreset("nintendo", "Nintendo", "Switch, handhelds, and classic Nintendo.") { p ->
        p.family == "nintendo"
    },
    PlatformPreset("playstation", "PlayStation", "Sony home and handheld systems.") { p ->
        p.family == "playstation"
    },
    PlatformPreset("xbox", "Xbox", "Xbox generations and current consoles.") { p ->
        p.family == "xbox"
    },
    PlatformPreset("pc", "PC", "Desktop and computer platforms.") { p ->
        p.family == "pc" || p.kind == "computer"
    },
    PlatformPreset("retro", "Retro", "Older consoles and handhelds.") { p ->
        listOf(
            "snes", "n64", "wii", "ps2", "ps3", "xbox_360", "xbox_original", "ps1",
            "game_boy_advance", "ds", "psp", "ps_vita", "gamecube", "gba", "gbc", "gb",
            "genesis", "sega_genesis", "wii_u", "dreamcast", "game_gear", "saturn",
            "sega_master_system", "nes", "atari_2600"
        ).contains(p.platformId) ||
        listOf("sega", "atari", "snk").contains(p.family)
    },
)

val platformStandardFamilies = listOf("nintendo", "playstation", "xbox", "sega", "pc")

fun sortedPlatformFamilies(platforms: List<Platform>): List<String> {
    val families = platforms.map { it.family }.distinct()
    val ordered = platformStandardFamilies.filter { it in families }
    val hasOther = families.any { it !in platformStandardFamilies }
    return ordered + (if (hasOther) listOf("other") else emptyList())
}

fun familyDisplayName(family: String): String = when (family) {
    "nintendo" -> "Nintendo"
    "playstation" -> "PlayStation"
    "xbox" -> "Xbox"
    "sega" -> "SEGA"
    "pc" -> "PC"
    "other" -> "Other"
    else -> family.replaceFirstChar { it.uppercase() }
}
