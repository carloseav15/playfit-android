package com.carlosarancibia.playfit.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.carlosarancibia.playfit.data.local.PlayfitDatabase
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.data.local.entity.CacheEntryEntity
import com.carlosarancibia.playfit.data.remote.PlayfitApiService
import com.carlosarancibia.playfit.data.remote.GameStateRequest
import com.carlosarancibia.playfit.data.remote.DeleteGameStateOperation
import com.carlosarancibia.playfit.data.remote.OnboardingDraftDto
import com.carlosarancibia.playfit.data.remote.ProfileBuildRequest
import com.carlosarancibia.playfit.data.remote.ProfileSaveRequest
import com.carlosarancibia.playfit.data.remote.ProfilePersistedState
import com.carlosarancibia.playfit.data.remote.ProfileStateResponse
import com.carlosarancibia.playfit.data.repository.PlayfitRepositoryImpl
import com.carlosarancibia.playfit.data.toRepositoryError
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: PlayfitDatabase,
    private val apiService: PlayfitApiService,
    private val preferencesDataStore: PreferencesDataStore,
) : CoroutineWorker(context, params) {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result {
        val resetOperation = database.pendingOperationDao()
            .getFirstByType(PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE)
        if (resetOperation != null) {
            return try {
                apiService.deleteProfile()
                database.pendingOperationDao().delete(resetOperation.operationId)
                val hasNewWork = database.gameStateDao().countPendingSync() > 0 ||
                    database.pendingOperationDao().getAll().isNotEmpty()
                if (hasNewWork) {
                    Result.retry()
                } else {
                    preferencesDataStore.setLastSyncAt(System.currentTimeMillis())
                    Result.success()
                }
            } catch (error: Exception) {
                if (error.toRepositoryError().retryable) Result.retry() else Result.failure()
            }
        }

        val pendingStates = database.gameStateDao().getPendingSync()
        val pendingOperations = database.pendingOperationDao().getAll()
        if (pendingStates.isEmpty() && pendingOperations.isEmpty()) return Result.success()

        var shouldRetry = false
        var permanentFailure = false

        for (state in pendingStates) {
            if (database.pendingOperationDao()
                    .getFirstByType(PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE) != null
            ) return Result.retry()
            try {
                apiService.upsertGameState(
                    gameId = state.gameId,
                    state = GameStateRequest(
                        status = state.status,
                        rating = state.rating,
                        inPlayfitPicks = state.inPlayfitPicks,
                        inBacklog = state.inBacklog,
                        inWishlist = state.inWishlist,
                        excluded = state.excluded,
                    ),
                )
                database.gameStateDao().markSynced(state.gameId)
            } catch (error: Exception) {
                if (error.toRepositoryError().retryable) shouldRetry = true else permanentFailure = true
            }
        }

        for (operation in pendingOperations) {
            if (operation.operationType != PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE &&
                database.pendingOperationDao()
                    .getFirstByType(PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE) != null
            ) return Result.retry()
            database.pendingOperationDao().markAttempt(operation.operationId)
            try {
                when (operation.operationType) {
                    PlayfitRepositoryImpl.OPERATION_SAVE_PROFILE -> {
                        val stored = json.decodeFromString<ProfileSaveRequest>(operation.payload)
                        val request = if (stored.profile == null) {
                            val profile = apiService.buildProfile(
                                ProfileBuildRequest(
                                    onboarding = OnboardingDraftDto(
                                        step = stored.onboarding.step,
                                        platforms = stored.onboarding.platforms,
                                        likedGameIds = stored.onboarding.likedGameIds,
                                        dislikedGameIds = stored.onboarding.dislikedGameIds,
                                    ),
                                    gameStates = stored.gameStates,
                                ),
                            ).profile
                            stored.copy(profile = profile)
                        } else {
                            stored
                        }
                        apiService.saveProfile(request)
                        database.cacheEntryDao().put(
                            CacheEntryEntity(
                                cacheKey = PlayfitRepositoryImpl.CACHE_PROFILE_STATE,
                                payload = json.encodeToString(
                                    ProfileStateResponse(
                                        ProfilePersistedState(
                                            gameStates = request.gameStates,
                                            profile = request.profile,
                                            onboarding = request.onboarding,
                                        ),
                                    ),
                                ),
                            ),
                        )
                    }
                    PlayfitRepositoryImpl.OPERATION_DELETE_GAME_STATE -> {
                        val request = json.decodeFromString<DeleteGameStateOperation>(operation.payload)
                        apiService.deleteGameState(request.gameId)
                    }
                    PlayfitRepositoryImpl.OPERATION_DELETE_PROFILE -> apiService.deleteProfile()
                    else -> error("Unsupported sync operation: ${operation.operationType}")
                }
                database.pendingOperationDao().delete(operation.operationId)
            } catch (error: Exception) {
                if (error.toRepositoryError().retryable) shouldRetry = true else permanentFailure = true
            }
        }

        val hasRemaining = database.gameStateDao().countPendingSync() > 0 ||
            database.pendingOperationDao().getAll().isNotEmpty()
        return when {
            !hasRemaining -> {
                preferencesDataStore.setLastSyncAt(System.currentTimeMillis())
                Result.success()
            }
            shouldRetry -> Result.retry()
            permanentFailure -> Result.failure()
            else -> Result.retry()
        }
    }
}
