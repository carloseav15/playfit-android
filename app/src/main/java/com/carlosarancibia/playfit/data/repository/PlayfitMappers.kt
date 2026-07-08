package com.carlosarancibia.playfit.data.repository

import androidx.room.withTransaction
import com.carlosarancibia.playfit.data.DataSource
import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.local.PlayfitDatabase
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.data.local.entity.CacheEntryEntity
import com.carlosarancibia.playfit.data.local.entity.GameStateEntity
import com.carlosarancibia.playfit.data.local.entity.PendingOperationEntity
import com.carlosarancibia.playfit.data.local.entity.PicksEntity
import com.carlosarancibia.playfit.data.remote.GameStateDto
import com.carlosarancibia.playfit.data.remote.GameStateRequest
import com.carlosarancibia.playfit.data.remote.BatchGamesRequest
import com.carlosarancibia.playfit.data.remote.BatchGamesResponse
import com.carlosarancibia.playfit.data.remote.SimilarGamesRequest
import com.carlosarancibia.playfit.data.remote.DeleteGameStateOperation
import com.carlosarancibia.playfit.data.remote.OnboardingDraftDto
import com.carlosarancibia.playfit.data.remote.PersistedOnboardingDto
import com.carlosarancibia.playfit.data.remote.PlatformSelectionDto
import com.carlosarancibia.playfit.data.remote.PlatformDto
import com.carlosarancibia.playfit.data.remote.PicksResponse
import com.carlosarancibia.playfit.data.remote.PlayfitApiService
import com.carlosarancibia.playfit.data.remote.ProfileBuildRequest
import com.carlosarancibia.playfit.data.remote.ProfileBuildResponse
import com.carlosarancibia.playfit.data.remote.ProfileSaveRequest
import com.carlosarancibia.playfit.data.remote.ProfilePersistedState
import com.carlosarancibia.playfit.data.remote.ProfileStateResponse
import com.carlosarancibia.playfit.data.remote.RankedSeedGameDto
import com.carlosarancibia.playfit.data.remote.SeedGameDto
import com.carlosarancibia.playfit.data.remote.TodayResponse
import com.carlosarancibia.playfit.data.sync.SyncManager
import com.carlosarancibia.playfit.data.toRepositoryError
import com.carlosarancibia.playfit.model.GameAccessStatus
import com.carlosarancibia.playfit.model.PlatformAvailability
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.ProductConfidence
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductGameStateTransitions
import com.carlosarancibia.playfit.model.ProductOnboardingDraft
import com.carlosarancibia.playfit.model.ProductOnboardingRules
import com.carlosarancibia.playfit.model.ProductOnboardingValidation
import com.carlosarancibia.playfit.model.ProductOnboardingStep
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductPlayStatus
import com.carlosarancibia.playfit.model.ProductProfile
import com.carlosarancibia.playfit.model.ProductProfileSignal
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.model.ProductTasteDerivation
import com.carlosarancibia.playfit.model.ProductUserState
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.SeedReleaseState
import com.carlosarancibia.playfit.model.SimilarGame
import com.carlosarancibia.playfit.model.fallbackPlatforms
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal fun TodayResponse.toDomain() = ProductPlayNextModel(
        primary = primary?.toDomain(),
        alternatives = alternatives.map { it.toDomain() },
        savedPickIds = savedPickIds,
        stateVersion = stateVersion.orEmpty(),
    )

internal fun ProfileStateResponse.toDomain(localStates: List<GameStateEntity>): ProductState {
        val persisted = state
        val remoteStates = persisted?.gameStates.orEmpty().mapValues { (id, dto) -> dto.toDomain(id) }
        val mergedStates = remoteStates + localStates.associate { it.gameId to it.toDomain() }
        return ProductState(
            user = ProductUserState(
                profile = persisted?.profile?.toDomain(),
                gameStates = mergedStates,
                onboarding = persisted?.onboarding?.let { onboarding ->
                    ProductOnboardingDraft(
                        step = onboarding.step.toOnboardingStep(),
                        platforms = onboarding.platforms.mapNotNull { platform ->
                            com.carlosarancibia.playfit.model.ProductAccessStatus
                                .fromApiValue(platform.status)
                                ?.let { status ->
                                    com.carlosarancibia.playfit.model.ProductPlatformSelection(
                                        platformId = platform.platformId,
                                        status = status,
                                    )
                                }
                        },
                        likedGameIds = onboarding.likedGameIds,
                        dislikedGameIds = onboarding.dislikedGameIds,
                    )
                } ?: ProductOnboardingDraft(),
                onboardingCompletedAt = persisted?.onboarding?.onboardingCompletedAt,
            ),
        )
    }

internal fun ProductOnboardingDraft.toPersistedDto(completedAt: String?) =
        PersistedOnboardingDto(
            step = "dislikes",
            platforms = platforms.map { PlatformSelectionDto(it.platformId, it.status.apiValue) },
            likedGameIds = likedGameIds,
            dislikedGameIds = dislikedGameIds,
            onboardingCompletedAt = completedAt,
        )

internal fun PersistedOnboardingDto.toDraftDto() = OnboardingDraftDto(
        step = step,
        platforms = platforms,
        likedGameIds = likedGameIds,
        dislikedGameIds = dislikedGameIds,
    )

internal fun String.toOnboardingStep(): ProductOnboardingStep = when (lowercase()) {
        "anchors" -> ProductOnboardingStep.Anchors
        "dislikes" -> ProductOnboardingStep.Dislikes
        else -> ProductOnboardingStep.Platforms
    }

internal fun RankedSeedGameDto.toDomain() = RankedSeedGame(
        game = game.toDomain(),
        affinityScore = affinityScore,
        riskScore = riskScore,
        confidence = ProductConfidence.entries.firstOrNull { it.name == confidence } ?: ProductConfidence.Medium,
        fitReasons = fitReasons,
        cautionReasons = cautionReasons,
        platformAvailability = PlatformAvailability.entries.firstOrNull { it.name == platformAvailability }
            ?: PlatformAvailability.Unknown,
        accessStatus = GameAccessStatus.entries.firstOrNull { it.name == accessStatus }
            ?: GameAccessStatus.Unreleased,
        inPlayfitPicks = inPlayfitPicks,
        similarGames = similarGames.map { SimilarGame(it.gameId, it.title, it.score) },
    )

internal fun SeedGameDto.toDomain() = SeedGame(
        gameId = gameId,
        title = title,
        aliases = aliases,
        series = series.orEmpty(),
        source = source.orEmpty(),
        primaryGenre = primaryGenre.orEmpty(),
        tags = tags,
        coverPath = coverPath.orEmpty(),
        externalCoverUrl = externalCoverUrl ?: coverPath?.takeIf { it.startsWith("http") },
        releaseYear = releaseYear,
        availablePlatformIds = availablePlatformIds,
        availablePlatformNames = availablePlatformNames,
        releaseState = SeedReleaseState.entries.firstOrNull { it.name == releaseState } ?: SeedReleaseState.Released,
    )

internal fun PlatformDto.toDomain(): Platform {
        val resolvedId = platformId ?: id ?: slug
        val fallback = fallbackPlatforms.firstOrNull { it.platformId == resolvedId || it.platformId == slug }
        return Platform(
            platformId = resolvedId,
            displayName = displayName ?: name ?: fallback?.displayName ?: resolvedId,
            family = family ?: fallback?.family ?: inferPlatformFamily(resolvedId),
            kind = kind ?: fallback?.kind ?: inferPlatformKind(resolvedId),
            activeStatus = activeStatus ?: fallback?.activeStatus ?: "active",
            sortOrder = sortOrder ?: fallback?.sortOrder ?: 0,
        )
    }

private fun inferPlatformFamily(platformId: String): String = when {
        platformId.contains("switch") ||
            platformId.contains("nintendo") ||
            platformId in setOf("nes", "snes", "n64", "gamecube", "wii", "wii_u", "gba", "gbc", "gb", "ds") -> "nintendo"
        platformId.startsWith("ps") || platformId.contains("playstation") || platformId.contains("vita") || platformId == "psp" -> "playstation"
        platformId.contains("xbox") -> "xbox"
        platformId in setOf("pc", "macos", "linux", "windows", "steam_deck") -> "pc"
        platformId.contains("sega") || platformId in setOf("genesis", "dreamcast", "saturn", "game_gear") -> "sega"
        else -> "other"
    }

private fun inferPlatformKind(platformId: String): String = when {
        platformId.contains("switch") || platformId == "steam_deck" -> "hybrid"
        platformId in setOf("gba", "gbc", "gb", "ds", "3ds", "psp", "ps_vita", "game_gear") -> "handheld"
        platformId in setOf("pc", "macos", "linux", "windows") -> "computer"
        else -> "console"
    }

internal fun com.carlosarancibia.playfit.data.remote.ProfileDto.toDomain() = ProductProfile(
        summary = summary,
        likedGenres = likedGenres,
        avoidedGenres = avoidedGenres,
        likedTags = likedTags,
        dislikedTags = dislikedTags,
        ratedCount = ratedCount,
        signals = signals.map { ProductProfileSignal(it.id, it.tone, it.label, it.description) },
    )

internal fun GameStateDto.toDomain(gameId: String) = ProductGameState(
        gameId = gameId,
        title = title,
        status = status?.let(ProductPlayStatus::fromApiValue),
        rating = rating,
        inBacklog = inBacklog ?: false,
        inWishlist = inWishlist ?: false,
        inPlayfitPicks = inPlayfitPicks ?: false,
        excluded = excluded ?: false,
        source = source,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun GameStateDto.toEntity(gameId: String) = GameStateEntity(
        gameId = gameId,
        status = status?.let(ProductPlayStatus::fromApiValue)?.apiValue,
        rating = rating,
        inPlayfitPicks = inPlayfitPicks ?: false,
        inBacklog = inBacklog ?: false,
        inWishlist = inWishlist ?: false,
        excluded = excluded ?: false,
        syncPending = false,
    )

internal fun RankedSeedGameDto.toEntity() = PicksEntity(
        gameId = game.gameId,
        title = game.title,
        affinityScore = affinityScore,
        fitReasons = fitReasons.joinToString("||"),
        genres = game.primaryGenre.orEmpty(),
        primaryGenre = game.primaryGenre.orEmpty(),
        coverPath = game.coverPath.orEmpty(),
    )

internal fun PicksEntity.toDomain() = RankedSeedGame(
        game = SeedGame(
            gameId = gameId,
            title = title,
            primaryGenre = primaryGenre,
            coverPath = coverPath,
            externalCoverUrl = coverPath.takeIf { it.startsWith("http") },
        ),
        affinityScore = affinityScore,
        riskScore = 0.0,
        confidence = ProductConfidence.Medium,
        fitReasons = fitReasons.split("||").filter { it.isNotBlank() },
        cautionReasons = emptyList(),
        platformAvailability = PlatformAvailability.Unknown,
        accessStatus = GameAccessStatus.Playable,
        inPlayfitPicks = true,
    )

internal fun GameStateEntity.toDomain() = ProductGameState(
        gameId = gameId,
        title = "",
        status = status?.let(ProductPlayStatus::fromApiValue),
        rating = rating,
        inBacklog = inBacklog,
        inWishlist = inWishlist,
        inPlayfitPicks = inPlayfitPicks,
        excluded = excluded,
        updatedAt = Instant.ofEpochMilli(updatedAt).toString(),
    )

internal fun GameStateEntity.toProfileDto(): GameStateDto {
        val timestamp = Instant.ofEpochMilli(updatedAt).toString()
        return GameStateDto(
            gameId = gameId,
            title = "",
            status = status,
            rating = rating,
            inPlayfitPicks = inPlayfitPicks,
            inBacklog = inBacklog,
            inWishlist = inWishlist,
            excluded = excluded,
            source = "manual",
            createdAt = timestamp,
            updatedAt = timestamp,
        )
    }

internal fun ProductGameState.toEntity(syncPending: Boolean) = GameStateEntity(
        gameId = gameId,
        status = status?.apiValue,
        rating = rating,
        inPlayfitPicks = inPlayfitPicks,
        inBacklog = inBacklog,
        inWishlist = inWishlist,
        excluded = excluded,
        updatedAt = updatedAt.toEpochMillisOrNow(),
        syncPending = syncPending,
    )

internal fun ProductGameState.toRequest() = GameStateRequest(
        status = status?.apiValue,
        rating = rating,
        inPlayfitPicks = inPlayfitPicks,
        inBacklog = inBacklog,
        inWishlist = inWishlist,
        excluded = excluded,
        source = source,
    )

internal fun String.toEpochMillisOrNow(): Long =
        runCatching { Instant.parse(this).toEpochMilli() }.getOrElse { System.currentTimeMillis() }
