package com.carlosarancibia.playfit.ui.viewmodel

import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.fold
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.model.Platform
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.fallbackPlatforms
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal data class InitialDataSnapshot(
    val state: ProductState?,
    val playNext: ProductPlayNextModel?,
    val picks: List<RankedSeedGame>?,
    val tasteModel: ProductTasteModel?,
    val platforms: List<Platform>,
    val platformsUi: PlatformsUiState,
    val error: String?,
    val showingStaleData: Boolean,
)

internal class InitialDataCoordinator(
    private val repository: PlayfitRepository,
    private val preferencesDataStore: PreferencesDataStore,
) {
    suspend fun load(): InitialDataSnapshot = coroutineScope {
        val failures = mutableListOf<String>()
        val platformsDeferred = async { loadPlatforms() }
        val stateDeferred = async { safeRepositoryCall { repository.getState() } }
        val playNextDeferred = async { safeRepositoryCall { repository.getTodayRecommendations() } }
        val picksDeferred = async { safeRepositoryCall { repository.getPicks() } }

        var stale = false
        var state: ProductState? = null
        var playNext: ProductPlayNextModel? = null
        var picks: List<RankedSeedGame>? = null
        var tasteModel: ProductTasteModel? = null

        stateDeferred.await().fold(
            onSuccess = { result ->
                state = result.data
                val isCompleted = result.data.user.onboardingCompletedAt != null
                preferencesDataStore.setOnboardingCompleted(isCompleted)
                val platformIds = result.data.user.onboarding.platforms.map { it.platformId }.toSet()
                if (platformIds.isNotEmpty()) {
                    preferencesDataStore.setSelectedPlatformIds(platformIds)
                }
                stale = stale || result.isStale
            },
            onFailure = { error -> failures += error.message },
        )
        // Taste derives from Room/profile cache, which getState refreshes. Keep it after State
        // while the independent network reads above run in parallel.
        safeRepositoryCall { repository.getTasteModel() }.fold(
            onSuccess = { result ->
                tasteModel = result.data
                stale = stale || result.isStale
            },
            onFailure = { error -> failures += error.message },
        )
        playNextDeferred.await().fold(
            onSuccess = { result ->
                playNext = result.data
                stale = stale || result.isStale
            },
            onFailure = { error -> failures += error.message },
        )
        picksDeferred.await().fold(
            onSuccess = { result ->
                picks = result.data
                stale = stale || result.isStale
            },
            onFailure = { error -> failures += error.message },
        )
        val platformsResult = platformsDeferred.await()
        stale = stale || platformsResult.second.showingStaleData

        InitialDataSnapshot(
            state = state,
            playNext = playNext,
            picks = picks,
            tasteModel = tasteModel,
            platforms = platformsResult.first,
            platformsUi = platformsResult.second,
            error = failures.firstOrNull(),
            showingStaleData = stale,
        )
    }

    private suspend fun loadPlatforms(): Pair<List<Platform>, PlatformsUiState> =
        safeRepositoryCall { repository.getPlatforms() }.fold(
            onSuccess = { result ->
                val platforms = result.data.ifEmpty { fallbackPlatforms }
                platforms to PlatformsUiState(
                    loading = false,
                    error = null,
                    showingStaleData = result.isStale,
                )
            },
            onFailure = { error ->
                fallbackPlatforms to PlatformsUiState(
                    loading = false,
                    error = error.message,
                    showingStaleData = true,
                )
            },
        )

    private suspend fun <T> safeRepositoryCall(
        block: suspend () -> RepositoryResult<T>,
    ): RepositoryResult<T> = try {
        block()
    } catch (error: Exception) {
        RepositoryResult.Failure(
            RepositoryError.Unknown(error.message ?: "Local data could not be read."),
        )
    }
}
