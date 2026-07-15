package com.carlosarancibia.playfit.data.repository

import androidx.room.withTransaction
import com.carlosarancibia.playfit.data.DataSource
import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.SearchGamesPage
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
import com.carlosarancibia.playfit.data.remote.SimilarGamesResponse
import com.carlosarancibia.playfit.data.remote.DeleteGameStateOperation
import com.carlosarancibia.playfit.data.remote.OnboardingDraftDto
import com.carlosarancibia.playfit.data.remote.PersistedOnboardingDto
import com.carlosarancibia.playfit.data.remote.PlatformSelectionDto
import com.carlosarancibia.playfit.data.remote.PlatformsResponse
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
import kotlinx.serialization.decodeFromString

@Singleton
class PlayfitRepositoryImpl @Inject constructor(
    private val apiService: PlayfitApiService,
    private val database: PlayfitDatabase,
    private val preferencesDataStore: PreferencesDataStore,
    private val authManager: AuthManager,
    private val syncManager: SyncManager,
) : PlayfitRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun observePendingSync(): Flow<Boolean> = combine(
        database.gameStateDao().observePendingSyncCount(),
        database.pendingOperationDao().observeCount(),
    ) { gameStates, operations -> gameStates + operations > 0 }

    override suspend fun getTodayRecommendations(): RepositoryResult<ProductPlayNextModel> {
        return try {
            val response = apiService.getTodayRecommendations()
            cache(CACHE_TODAY, response)
            RepositoryResult.Success(response.toDomain(), DataSource.Network)
        } catch (error: Exception) {
            val cached = readCache<TodayResponse>(CACHE_TODAY)
            if (cached != null) {
                RepositoryResult.Success(cached.toDomain(), DataSource.Cache, isStale = true)
            } else {
                RepositoryResult.Failure(error.toRepositoryError())
            }
        }
    }

    override suspend fun getPicks(): RepositoryResult<List<RankedSeedGame>> {
        return try {
            val picks = apiService.getPicks()
            cache(CACHE_PICKS, picks)
            database.picksDao().deleteAll()
            database.picksDao().insertAll(picks.map { it.toEntity() })
            RepositoryResult.Success(
                data = overlayLocalPicks(picks.map { it.toDomain() }, pendingOnly = true),
                source = DataSource.Network,
            )
        } catch (error: Exception) {
            val cached = readCache<List<RankedSeedGameDto>>(CACHE_PICKS)?.map { it.toDomain() }
                ?: database.picksDao().getAllPicks().first().map { it.toDomain() }
            val merged = overlayLocalPicks(cached, pendingOnly = false)
            if (merged.isNotEmpty() || cached.isNotEmpty()) {
                RepositoryResult.Success(merged, DataSource.Cache, isStale = true)
            } else {
                RepositoryResult.Failure(error.toRepositoryError())
            }
        }
    }

    override suspend fun getGameRecommendation(gameId: String): RepositoryResult<RankedSeedGame?> {
        return try {
            val response = apiService.getGameRecommendation(gameId)
            RepositoryResult.Success(response.entry?.toDomain(), DataSource.Network)
        } catch (error: Exception) {
            val cached = cachedRecommendations().firstOrNull { it.game.gameId == gameId }
            if (cached != null) {
                RepositoryResult.Success(cached, DataSource.Cache, isStale = true)
            } else {
                RepositoryResult.Failure(error.toRepositoryError())
            }
        }
    }

    override suspend fun getTasteProfile(): RepositoryResult<ProductProfile> {
        val buildRequest = currentProfileBuildRequest()
        if (buildRequest == null) {
            val cached = readCache<ProfileBuildResponse>(CACHE_PROFILE_BUILD)
            return if (cached != null) {
                RepositoryResult.Success(cached.profile.toDomain(), DataSource.Cache, isStale = true)
            } else {
                RepositoryResult.Failure(
                    RepositoryError.InvalidData("Complete onboarding before building your taste profile."),
                )
            }
        }
        return try {
            val response = apiService.buildProfile(buildRequest)
            cache(CACHE_PROFILE_BUILD, response)
            RepositoryResult.Success(response.profile.toDomain(), DataSource.Network)
        } catch (error: Exception) {
            val cached = readCache<ProfileBuildResponse>(CACHE_PROFILE_BUILD)
            if (cached != null) {
                RepositoryResult.Success(cached.profile.toDomain(), DataSource.Cache, isStale = true)
            } else {
                RepositoryResult.Failure(error.toRepositoryError())
            }
        }
    }

    override suspend fun getTasteModel(): RepositoryResult<ProductTasteModel> {
        val localStates = database.gameStateDao().getAllStates()
        val cachedState = readCache<ProfileStateResponse>(CACHE_PROFILE_STATE)
            ?.toDomain(localStates)
            ?: ProductState(
                user = ProductUserState(
                    gameStates = localStates.associate { it.gameId to it.toDomain() },
                ),
            )
        val profileResult = getTasteProfile()
        val profile = when (profileResult) {
            is RepositoryResult.Success -> profileResult.data
            is RepositoryResult.Failure -> cachedState.user.profile
                ?: return profileResult
        }
        val state = cachedState.copy(user = cachedState.user.copy(profile = profile))
        val gameIds = buildSet {
            addAll(state.user.gameStates.keys)
            addAll(state.user.onboarding.likedGameIds)
            addAll(state.user.onboarding.dislikedGameIds)
        }
        val games = hydrateTasteGames(gameIds)
        return RepositoryResult.Success(
            data = ProductTasteDerivation.build(state, games),
            source = (profileResult as? RepositoryResult.Success)?.source ?: DataSource.Cache,
            isStale = (profileResult as? RepositoryResult.Success)?.isStale ?: true,
        )
    }

    override suspend fun getPlatforms(): RepositoryResult<List<Platform>> {
        return try {
            val response = apiService.getPlatforms()
            cache(CACHE_PLATFORMS, response)
            RepositoryResult.Success(
                data = response.platforms.map { it.toDomain() },
                source = DataSource.Network,
            )
        } catch (error: Exception) {
            val cached = readCache<PlatformsResponse>(CACHE_PLATFORMS)
                ?.platforms
                ?.map { it.toDomain() }
                .orEmpty()
            if (cached.isNotEmpty()) {
                RepositoryResult.Success(cached, DataSource.Cache, isStale = true)
            } else {
                RepositoryResult.Success(fallbackPlatforms, DataSource.Local, isStale = true)
            }
        }
    }

    override suspend fun getState(): RepositoryResult<ProductState> {
        return try {
            val response = apiService.getProfile()
            cache(CACHE_PROFILE_STATE, response)
            mergeRemoteGameStates(response)
            RepositoryResult.Success(
                data = response.toDomain(database.gameStateDao().getAllStates()),
                source = DataSource.Network,
            )
        } catch (error: Exception) {
            val cached = readCache<ProfileStateResponse>(CACHE_PROFILE_STATE)
            val localStates = database.gameStateDao().getAllStates()
            when {
                cached != null -> RepositoryResult.Success(
                    cached.toDomain(localStates),
                    DataSource.Cache,
                    isStale = true,
                )
                localStates.isNotEmpty() -> RepositoryResult.Success(
                    ProductState(user = ProductUserState(gameStates = localStates.associate { it.gameId to it.toDomain() })),
                    DataSource.Local,
                    isStale = true,
                )
                else -> RepositoryResult.Failure(error.toRepositoryError())
            }
        }
    }

    override suspend fun saveOnboarding(
        draft: ProductOnboardingDraft,
        completedAt: String?,
    ): RepositoryResult<Unit> {
        when (val validation = ProductOnboardingRules.validate(draft)) {
            is ProductOnboardingValidation.Invalid -> return RepositoryResult.Failure(
                RepositoryError.InvalidData(validation.message),
            )
            ProductOnboardingValidation.Valid -> Unit
        }
        return when (val result = persistTasteProfile(
            draft = draft,
            completedAt = completedAt ?: Instant.now().toString(),
            ensureOnboardingStates = true,
        )) {
            is RepositoryResult.Failure -> result
            is RepositoryResult.Success -> RepositoryResult.Success(
                Unit,
                result.source,
                result.isStale,
                result.pendingSync,
            )
        }
    }

    override suspend fun rebuildTasteProfile(
        draft: ProductOnboardingDraft,
        completedAt: String?,
    ): RepositoryResult<ProductProfile> = persistTasteProfile(
        draft = draft,
        completedAt = completedAt,
        ensureOnboardingStates = false,
    )

    override suspend fun deleteGameState(gameId: String): RepositoryResult<Unit> {
        database.gameStateDao().delete(gameId)
        database.picksDao().delete(gameId)
        val operation = PendingOperationEntity(
            operationId = "delete_game_state:$gameId",
            operationType = OPERATION_DELETE_GAME_STATE,
            payload = json.encodeToString(DeleteGameStateOperation(gameId)),
        )
        database.pendingOperationDao().put(operation)
        return try {
            apiService.deleteGameState(gameId)
            database.pendingOperationDao().delete(operation.operationId)
            RepositoryResult.Success(Unit, DataSource.Network)
        } catch (_: Exception) {
            syncManager.enqueueSync()
            RepositoryResult.Success(Unit, DataSource.Local, pendingSync = true)
        }
    }

    override suspend fun resetTaste(): RepositoryResult<Unit> {
        syncManager.cancelSync()
        val operation = PendingOperationEntity(
            operationId = RESET_TASTE_OPERATION_ID,
            operationType = OPERATION_DELETE_PROFILE,
            payload = "{}",
        )
        database.withTransaction {
            database.gameStateDao().deleteAll()
            database.picksDao().deleteAll()
            database.cacheEntryDao().deleteAll()
            database.pendingOperationDao().deleteAll()
            database.pendingOperationDao().put(operation)
        }
        preferencesDataStore.resetTaste()
        return try {
            apiService.deleteProfile()
            database.pendingOperationDao().delete(operation.operationId)
            RepositoryResult.Success(Unit, DataSource.Network)
        } catch (_: Exception) {
            syncManager.enqueueSync()
            RepositoryResult.Success(Unit, DataSource.Local, pendingSync = true)
        }
    }

    override suspend fun deleteCloudProfile(): RepositoryResult<Unit> {
        // Profile deletion is irreversible, so unlike a taste reset it must not become an
        // offline operation that could outlive the authenticated session.
        syncManager.cancelSync()
        return try {
            apiService.deleteProfile()
            database.withTransaction {
                database.gameStateDao().deleteAll()
                database.picksDao().deleteAll()
                database.cacheEntryDao().deleteAll()
                database.pendingOperationDao().deleteAll()
            }
            preferencesDataStore.resetTaste()
            RepositoryResult.Success(Unit, DataSource.Network)
        } catch (error: Exception) {
            syncManager.enqueueSync()
            RepositoryResult.Failure(error.toRepositoryError())
        }
    }

    override suspend fun saveGameState(
        gameId: String,
        state: ProductGameState,
    ): RepositoryResult<Unit> {
        database.gameStateDao().upsert(state.toEntity(syncPending = true))
        return try {
            apiService.upsertGameState(gameId, state.toRequest())
            database.gameStateDao().markSynced(gameId)
            RepositoryResult.Success(Unit, DataSource.Network)
        } catch (_: Exception) {
            syncManager.enqueueSync()
            RepositoryResult.Success(Unit, DataSource.Local, pendingSync = true)
        }
    }

    override suspend fun applyFeedback(
        gameId: String,
        feedback: ProductDecisionFeedback,
    ): RepositoryResult<Unit> {
        val existing = database.gameStateDao().getState(gameId)?.toDomain()
        val next = ProductGameStateTransitions.applyFeedback(existing, gameId, feedback)
        database.gameStateDao().upsert(next.toEntity(syncPending = true))
        if (!next.inPlayfitPicks) database.picksDao().delete(gameId)
        syncManager.enqueueSync()
        return RepositoryResult.Success(Unit, DataSource.Local, pendingSync = true)
    }

    override suspend fun togglePick(gameId: String, picked: Boolean): RepositoryResult<Unit> {
        val existing = database.gameStateDao().getState(gameId)?.toDomain()
        val next = ProductGameStateTransitions.setPick(existing, gameId, picked)
        database.gameStateDao().upsert(next.toEntity(syncPending = true))
        if (!picked) database.picksDao().delete(gameId)
        syncManager.enqueueSync()
        return RepositoryResult.Success(Unit, DataSource.Local, pendingSync = true)
    }

    override suspend fun refreshRecommendations(): RepositoryResult<ProductPlayNextModel> =
        getTodayRecommendations()

    override suspend fun getSimilarGames(gameId: String): RepositoryResult<List<SimilarGame>> {
        return try {
            val response = apiService.getSimilarRecommendations(SimilarGamesRequest(gameId))
            cache(similarGamesCacheKey(gameId), response)
            RepositoryResult.Success(
                response.similar.map { SimilarGame(it.gameId, it.title, it.score) },
                DataSource.Network,
            )
        } catch (error: Exception) {
            val cached = readCache<SimilarGamesResponse>(similarGamesCacheKey(gameId))
            if (cached != null) {
                RepositoryResult.Success(
                    cached.similar.map { SimilarGame(it.gameId, it.title, it.score) },
                    DataSource.Cache,
                    isStale = true,
                )
            } else {
                RepositoryResult.Failure(error.toRepositoryError())
            }
        }
    }

    override suspend fun searchGames(
        query: String,
        limit: Int,
        platformIds: List<String>,
        page: Int,
        pageSize: Int,
    ): RepositoryResult<SearchGamesPage> {
        return try {
            val response = apiService.searchGames(
                query = query,
                platform = platformIds.takeIf { it.isNotEmpty() }?.joinToString(","),
                page = page,
                pageSize = pageSize,
            )
            RepositoryResult.Success(
                SearchGamesPage(response.games.take(limit).map { it.toDomain() }, response.total),
                DataSource.Network,
            )
        } catch (error: Exception) {
            RepositoryResult.Failure(error.toRepositoryError())
        }
    }

    private suspend fun mergeRemoteGameStates(response: ProfileStateResponse) {
        val persisted = response.state ?: return
        val pendingIds = database.gameStateDao().getPendingSync().mapTo(mutableSetOf()) { it.gameId }
        val remoteStates = persisted.gameStates.orEmpty()
            .filterKeys { it !in pendingIds }
            .map { (gameId, dto) -> dto.toEntity(gameId) }
        database.gameStateDao().replaceSyncedStates(remoteStates)
    }

    private suspend fun currentProfileBuildRequest(): ProfileBuildRequest? {
        val onboarding = readCache<ProfileStateResponse>(CACHE_PROFILE_STATE)
            ?.state
            ?.onboarding
            ?: return null
        val gameStates = database.gameStateDao().getAllStates().associate { entity ->
            entity.gameId to entity.toProfileDto()
        }
        return ProfileBuildRequest(onboarding.toDraftDto(), gameStates)
    }

    private suspend fun ensureOnboardingGameStates(
        draft: ProductOnboardingDraft,
        timestamp: String,
    ) {
        (draft.likedGameIds + draft.dislikedGameIds).distinct().forEach { gameId ->
            if (database.gameStateDao().getState(gameId) == null) {
                database.gameStateDao().upsert(
                    ProductGameState(
                        gameId = gameId,
                        title = "",
                        source = "onboarding",
                        createdAt = timestamp,
                        updatedAt = timestamp,
                    ).toEntity(syncPending = false),
                )
            }
        }
    }

    private suspend fun persistTasteProfile(
        draft: ProductOnboardingDraft,
        completedAt: String?,
        ensureOnboardingStates: Boolean,
    ): RepositoryResult<ProductProfile> {
        if (ensureOnboardingStates) {
            ensureOnboardingGameStates(draft, completedAt ?: Instant.now().toString())
        }
        val gameStates = database.gameStateDao().getAllStates().associate { entity ->
            entity.gameId to entity.toProfileDto()
        }
        val onboarding = draft.toPersistedDto(completedAt)
        val baseRequest = ProfileSaveRequest(
            deviceId = authManager.deviceId,
            gameStates = gameStates,
            profile = null,
            onboarding = onboarding,
        )
        val operation = PendingOperationEntity(
            operationId = ONBOARDING_OPERATION_ID,
            operationType = OPERATION_SAVE_PROFILE,
            payload = json.encodeToString(baseRequest),
        )
        database.pendingOperationDao().put(operation)
        return try {
            val profileResponse = apiService.buildProfile(
                ProfileBuildRequest(onboarding.toDraftDto(), gameStates),
            )
            cache(CACHE_PROFILE_BUILD, profileResponse)
            val request = baseRequest.copy(profile = profileResponse.profile)
            database.pendingOperationDao().put(operation.copy(payload = json.encodeToString(request)))
            apiService.saveProfile(request)
            cache(
                CACHE_PROFILE_STATE,
                ProfileStateResponse(
                    ProfilePersistedState(gameStates, profileResponse.profile, onboarding),
                ),
            )
            database.pendingOperationDao().delete(operation.operationId)
            RepositoryResult.Success(profileResponse.profile.toDomain(), DataSource.Network)
        } catch (_: Exception) {
            syncManager.enqueueSync()
            val cachedProfileDto = readCache<ProfileBuildResponse>(CACHE_PROFILE_BUILD)?.profile
                ?: readCache<ProfileStateResponse>(CACHE_PROFILE_STATE)?.state?.profile
            cache(
                CACHE_PROFILE_STATE,
                ProfileStateResponse(
                    ProfilePersistedState(gameStates, cachedProfileDto, onboarding),
                ),
            )
            val cachedProfile = cachedProfileDto?.toDomain() ?: ProductProfile()
            RepositoryResult.Success(
                cachedProfile,
                DataSource.Local,
                isStale = true,
                pendingSync = true,
            )
        }
    }

    private suspend fun overlayLocalPicks(
        remoteOrCached: List<RankedSeedGame>,
        pendingOnly: Boolean,
    ): List<RankedSeedGame> {
        val states = database.gameStateDao().getAllStates()
            .filter { !pendingOnly || it.syncPending }
            .associateBy { it.gameId }
        val known = (remoteOrCached + cachedRecommendations()).associateBy { it.game.gameId }
        val result = remoteOrCached.associateBy { it.game.gameId }.toMutableMap()
        states.values.forEach { state ->
            if (state.inPlayfitPicks && !state.excluded) {
                known[state.gameId]?.let { result[state.gameId] = it.copy(inPlayfitPicks = true) }
            } else {
                result.remove(state.gameId)
            }
        }
        return result.values.sortedByDescending { it.affinityScore }
    }

    private suspend fun cachedRecommendations(): List<RankedSeedGame> {
        val today = readCache<TodayResponse>(CACHE_TODAY)
        return buildList {
            today?.primary?.toDomain()?.let(::add)
            addAll(today?.alternatives.orEmpty().map { it.toDomain() })
            addAll(readCache<List<RankedSeedGameDto>>(CACHE_PICKS).orEmpty().map { it.toDomain() })
        }.distinctBy { it.game.gameId }
    }

    private suspend fun hydrateTasteGames(gameIds: Set<String>): List<SeedGame> {
        val known = linkedMapOf<String, SeedGame>()
        cachedRecommendations().forEach { known[it.game.gameId] = it.game }
        readCache<BatchGamesResponse>(CACHE_TASTE_GAMES)?.games.orEmpty()
            .map { it.toDomain() }
            .forEach { known[it.gameId] = it }

        val missing = gameIds.filterNot { it in known }
        if (missing.isNotEmpty()) {
            try {
                val response = apiService.batchGames(BatchGamesRequest(missing))
                val mergedDtos = (
                    readCache<BatchGamesResponse>(CACHE_TASTE_GAMES)?.games.orEmpty() + response.games
                ).distinctBy { it.gameId }
                cache(CACHE_TASTE_GAMES, BatchGamesResponse(mergedDtos))
                response.games.map { it.toDomain() }.forEach { known[it.gameId] = it }
            } catch (_: Exception) {
                // Existing local/cache data remains usable offline.
            }
        }
        return gameIds.map { id -> known[id] ?: SeedGame(gameId = id, title = id) }
    }

    private suspend inline fun <reified T> cache(key: String, value: T) {
        database.cacheEntryDao().put(CacheEntryEntity(key, json.encodeToString(value)))
    }

    private suspend inline fun <reified T> readCache(key: String): T? {
        val payload = database.cacheEntryDao().get(key)?.payload ?: return null
        return runCatching { json.decodeFromString<T>(payload) }.getOrNull()
    }

    private fun similarGamesCacheKey(gameId: String) = "$CACHE_SIMILAR_GAMES_PREFIX$gameId"

    companion object {
        const val OPERATION_SAVE_PROFILE = "save_profile"
        const val OPERATION_DELETE_GAME_STATE = "delete_game_state"
        const val OPERATION_DELETE_PROFILE = "delete_profile"
        private const val ONBOARDING_OPERATION_ID = "onboarding"
        private const val RESET_TASTE_OPERATION_ID = "reset_taste"
        private const val CACHE_TODAY = "recommendations_today"
        private const val CACHE_PICKS = "recommendations_picks"
        private const val CACHE_PLATFORMS = "platforms"
        private const val CACHE_PROFILE_BUILD = "profile_build"
        const val CACHE_PROFILE_STATE = "profile_state"
        private const val CACHE_TASTE_GAMES = "taste_games"
        private const val CACHE_SIMILAR_GAMES_PREFIX = "similar_games:"
    }
}
