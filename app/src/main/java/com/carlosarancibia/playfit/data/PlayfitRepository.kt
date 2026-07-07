package com.carlosarancibia.playfit.data

import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.data.auth.AuthResult
import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductOnboardingDraft
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductProfile
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import com.carlosarancibia.playfit.model.SimilarGame
import kotlinx.coroutines.flow.Flow

interface PlayfitRepository {
    suspend fun signInAnonymously(): AuthResult
    suspend fun signInWithGoogle(): AuthResult
    suspend fun resetPassword(email: String): AuthResult
    suspend fun signOut(): AuthResult
    suspend fun deleteAccount(): AuthResult
    fun isAuthenticated(): Boolean
    fun getDeviceId(): String
    fun observePendingSync(): Flow<Boolean>

    suspend fun getTodayRecommendations(): RepositoryResult<ProductPlayNextModel>
    suspend fun getPicks(): RepositoryResult<List<RankedSeedGame>>
    suspend fun getGameRecommendation(gameId: String): RepositoryResult<RankedSeedGame?>
    suspend fun getTasteProfile(): RepositoryResult<ProductProfile>
    suspend fun getTasteModel(): RepositoryResult<ProductTasteModel>
    suspend fun getState(): RepositoryResult<ProductState>
    suspend fun saveOnboarding(
        draft: ProductOnboardingDraft,
        completedAt: String?,
    ): RepositoryResult<Unit>
    suspend fun rebuildTasteProfile(
        draft: ProductOnboardingDraft,
        completedAt: String?,
    ): RepositoryResult<ProductProfile>
    suspend fun deleteGameState(gameId: String): RepositoryResult<Unit>
    suspend fun resetTaste(): RepositoryResult<Unit>
    suspend fun saveGameState(gameId: String, state: ProductGameState): RepositoryResult<Unit>
    suspend fun applyFeedback(gameId: String, feedback: ProductDecisionFeedback): RepositoryResult<Unit>
    suspend fun togglePick(gameId: String, picked: Boolean): RepositoryResult<Unit>
    suspend fun refreshRecommendations(): RepositoryResult<ProductPlayNextModel>
    suspend fun getSimilarGames(gameId: String): RepositoryResult<List<SimilarGame>>
    suspend fun searchGames(query: String, limit: Int = 20): RepositoryResult<List<SeedGame>>
}
