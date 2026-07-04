package com.carlosarancibia.playfit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlosarancibia.playfit.data.PlayfitRepository
import com.carlosarancibia.playfit.data.RepositoryError
import com.carlosarancibia.playfit.data.RepositoryResult
import com.carlosarancibia.playfit.data.auth.AuthManager
import com.carlosarancibia.playfit.data.auth.AuthResult
import com.carlosarancibia.playfit.data.local.PreferencesDataStore
import com.carlosarancibia.playfit.model.ProductDecisionFeedback
import com.carlosarancibia.playfit.model.ProductGameState
import com.carlosarancibia.playfit.model.ProductGameStateTransitions
import com.carlosarancibia.playfit.model.ProductOnboardingDraft
import com.carlosarancibia.playfit.model.ProductOnboardingRules
import com.carlosarancibia.playfit.model.ProductOnboardingValidation
import com.carlosarancibia.playfit.model.ProductPlayNextModel
import com.carlosarancibia.playfit.model.ProductPlayStatus
import com.carlosarancibia.playfit.model.ProductState
import com.carlosarancibia.playfit.model.ProductTasteModel
import com.carlosarancibia.playfit.model.RankedSeedGame
import com.carlosarancibia.playfit.model.SeedGame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.time.Instant

data class PlayfitUiState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val toast: String? = null,
    val showingStaleData: Boolean = false,
    val pendingSync: Boolean = false,
)

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isAnonymous: Boolean = false,
    val userId: String? = null,
)

sealed interface DossierUiState {
    data object Idle : DossierUiState
    data class Loading(val gameId: String) : DossierUiState
    data class Success(val entry: RankedSeedGame) : DossierUiState
    data class NotFound(val gameId: String) : DossierUiState
    data class Error(val gameId: String, val message: String) : DossierUiState
}

@HiltViewModel
class PlayfitViewModel @Inject constructor(
    private val repository: PlayfitRepository,
    val authManager: AuthManager,
    val preferencesDataStore: PreferencesDataStore,
) : ViewModel() {

    private val _state = MutableStateFlow(ProductState())
    val state: StateFlow<ProductState> = _state.asStateFlow()

    private val _ui = MutableStateFlow(PlayfitUiState())
    val ui: StateFlow<PlayfitUiState> = _ui.asStateFlow()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _playNext = MutableStateFlow<ProductPlayNextModel?>(null)
    val playNext: StateFlow<ProductPlayNextModel?> = _playNext.asStateFlow()

    private val _picks = MutableStateFlow<List<RankedSeedGame>>(emptyList())
    val picks: StateFlow<List<RankedSeedGame>> = _picks.asStateFlow()

    private val _tasteModel = MutableStateFlow<ProductTasteModel?>(null)
    val tasteModel: StateFlow<ProductTasteModel?> = _tasteModel.asStateFlow()

    private val _dossier = MutableStateFlow<DossierUiState>(DossierUiState.Idle)
    val dossier: StateFlow<DossierUiState> = _dossier.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _excludedIds = MutableStateFlow<Set<String>>(emptySet())
    val excludedIds: StateFlow<Set<String>> = _excludedIds.asStateFlow()

    private var saveJob: Job? = null
    private var platformSaveJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesDataStore.onboardingCompleted.collect { completed ->
                _onboardingCompleted.value = completed
            }
        }
        viewModelScope.launch {
            preferencesDataStore.themeMode.collect { mode ->
                _themeMode.value = mode
            }
        }
        viewModelScope.launch {
            authManager.session.collect { session ->
                _authState.value = AuthState(
                    isAuthenticated = session != null,
                    isAnonymous = session?.isAnonymous == true,
                    userId = session?.userId,
                )
            }
        }
        viewModelScope.launch {
            repository.observePendingSync().collect { pending ->
                _ui.value = _ui.value.copy(pendingSync = pending)
            }
        }
        viewModelScope.launch {
            initializeSessionAndData()
        }
    }

    private suspend fun initializeSessionAndData() {
        val restored = try {
            authManager.restoreSession()
        } catch (_: Exception) {
            null
        }
        if (restored == null) {
            when (val result = authManager.signInAnonymously()) {
                is AuthResult.Error -> _ui.value = _ui.value.copy(error = result.message)
                is AuthResult.Pending -> setToast(result.message)
                is AuthResult.Success -> Unit
            }
        }
        loadInitialData()
    }

    private suspend fun loadInitialData() {
        _ui.value = _ui.value.copy(loading = true)
        val failures = mutableListOf<String>()
        var stale = false

        when (val result = safeRepositoryCall { repository.getState() }) {
            is RepositoryResult.Success -> {
                _state.value = result.data
                val platformIds = result.data.user.onboarding.platforms.map { it.platformId }.toSet()
                if (platformIds.isNotEmpty()) {
                    preferencesDataStore.setSelectedPlatformIds(platformIds)
                }
                stale = stale || result.isStale
            }
            is RepositoryResult.Failure -> failures += result.error.message
        }
        when (val result = safeRepositoryCall { repository.getTodayRecommendations() }) {
            is RepositoryResult.Success -> {
                _playNext.value = result.data
                stale = stale || result.isStale
            }
            is RepositoryResult.Failure -> failures += result.error.message
        }
        when (val result = safeRepositoryCall { repository.getPicks() }) {
            is RepositoryResult.Success -> {
                _picks.value = result.data
                stale = stale || result.isStale
            }
            is RepositoryResult.Failure -> failures += result.error.message
        }
        when (val result = safeRepositoryCall { repository.getTasteModel() }) {
            is RepositoryResult.Success -> {
                _tasteModel.value = result.data
                stale = stale || result.isStale
            }
            is RepositoryResult.Failure -> failures += result.error.message
        }
        _ui.value = _ui.value.copy(
            loading = false,
            error = failures.firstOrNull(),
            showingStaleData = stale,
        )
    }

    fun linkGoogleAccount() {
        viewModelScope.launch {
            when (val result = authManager.linkGoogleIdentity()) {
                is AuthResult.Success -> setToast("Google account linked.")
                is AuthResult.Pending -> setToast(result.message)
                is AuthResult.Error -> setToast(result.message)
            }
        }
    }

    fun signOutAsync() {
        viewModelScope.launch {
            when (val result = repository.signOut()) {
                is AuthResult.Error -> setToast(result.message)
                is AuthResult.Pending -> setToast(result.message)
                is AuthResult.Success -> setToast("Signed out.")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            when (val result = repository.deleteAccount()) {
                is AuthResult.Error -> setToast(result.message)
                is AuthResult.Pending -> setToast(result.message)
                is AuthResult.Success -> setToast("Account deleted.")
            }
        }
    }

    suspend fun signInAnonymously(): AuthResult = authManager.signInAnonymously()

    suspend fun signInWithGoogle(): AuthResult {
        return authManager.signInWithGoogle()
    }

    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return authManager.signInWithEmail(email, password)
    }

    suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return authManager.signUpWithEmail(email, password)
    }

    suspend fun resetPassword(email: String): AuthResult {
        return repository.resetPassword(email)
    }

    suspend fun signInAsGuest(): AuthResult {
        return authManager.signInAnonymously()
    }

    suspend fun signOut(): AuthResult = authManager.signOut()

    fun completeOnboarding(draft: ProductOnboardingDraft? = null) {
        viewModelScope.launch {
            val completedDraft = draft ?: run {
                _ui.value = _ui.value.copy(error = "Complete calibration before continuing.")
                return@launch
            }
            when (val validation = ProductOnboardingRules.validate(completedDraft)) {
                is ProductOnboardingValidation.Invalid -> {
                    _ui.value = _ui.value.copy(error = validation.message)
                    return@launch
                }
                ProductOnboardingValidation.Valid -> Unit
            }
            val completedAt = Instant.now().toString()
            _state.value = _state.value.copy(
                user = _state.value.user.copy(
                    onboarding = completedDraft,
                    onboardingCompletedAt = completedAt,
                ),
            )
            preferencesDataStore.setSelectedPlatformIds(
                completedDraft.platforms.map { it.platformId }.toSet(),
            )
            when (val result = safeRepositoryCall {
                repository.saveOnboarding(completedDraft, completedAt)
            }) {
                is RepositoryResult.Failure -> {
                    _ui.value = _ui.value.copy(error = result.error.message)
                    return@launch
                }
                is RepositoryResult.Success -> {
                    _ui.value = _ui.value.copy(pendingSync = result.pendingSync)
                }
            }
            preferencesDataStore.setOnboardingCompleted(true)
            _onboardingCompleted.value = true
            loadInitialData()
            setToast("Profile ready. Your Play Next pick is ready.")
        }
    }

    fun updatePlatforms(ids: Set<String>) {
        if (ids.isEmpty()) {
            setToast("Keep at least one active platform.")
            return
        }
        val draft = _state.value.user.onboarding.copy(
            platforms = ids.sorted().map {
                com.carlosarancibia.playfit.model.ProductPlatformSelection(
                    platformId = it,
                    status = com.carlosarancibia.playfit.model.ProductAccessStatus.Available,
                )
            },
        )
        when (val validation = ProductOnboardingRules.validate(draft)) {
            is ProductOnboardingValidation.Invalid -> {
                setToast(validation.message)
                return
            }
            ProductOnboardingValidation.Valid -> Unit
        }
        _state.value = _state.value.copy(user = _state.value.user.copy(onboarding = draft))
        viewModelScope.launch {
            preferencesDataStore.setSelectedPlatformIds(ids)
        }
        platformSaveJob?.cancel()
        platformSaveJob = viewModelScope.launch {
            delay(500)
            when (val result = safeRepositoryCall {
                repository.saveOnboarding(draft, _state.value.user.onboardingCompletedAt)
            }) {
                is RepositoryResult.Failure -> _ui.value = _ui.value.copy(error = result.error.message)
                is RepositoryResult.Success -> {
                    _ui.value = _ui.value.copy(pendingSync = result.pendingSync)
                    setToast(if (result.pendingSync) "Platforms saved; waiting to sync" else "Platforms updated")
                    if (!result.pendingSync) refreshRecommendations()
                }
            }
        }
    }

    fun refreshRecommendations() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(refreshing = true)
            when (val result = safeRepositoryCall { repository.refreshRecommendations() }) {
                is RepositoryResult.Failure -> {
                    _ui.value = _ui.value.copy(refreshing = false, error = result.error.message)
                }
                is RepositoryResult.Success -> {
                    _playNext.value = result.data
                    _ui.value = _ui.value.copy(
                        refreshing = false,
                        showingStaleData = result.isStale,
                        error = null,
                    )
                    refreshSecondaryData()
                    setToast(if (result.isStale) "Showing saved recommendations" else "Recommendations updated")
                }
            }
        }
    }

    fun getGameRecommendation(gameId: String): RankedSeedGame? {
        val fromPlayNext = _playNext.value?.let { model ->
            model.primary?.takeIf { it.game.gameId == gameId }
                ?: model.alternatives.firstOrNull { it.game.gameId == gameId }
        }
        if (fromPlayNext != null) return fromPlayNext
        return _picks.value.firstOrNull { it.game.gameId == gameId }
    }

    fun loadGameRecommendation(gameId: String, forceRefresh: Boolean = false) {
        if (gameId.isBlank()) {
            _dossier.value = DossierUiState.NotFound(gameId)
            return
        }
        val local = getGameRecommendation(gameId)
        if (local != null && !forceRefresh) {
            _dossier.value = DossierUiState.Success(local)
            viewModelScope.launch {
                val result = safeRepositoryCall { repository.getGameRecommendation(gameId) }
                if (result is RepositoryResult.Success && result.data != null) {
                    _dossier.value = DossierUiState.Success(result.data)
                }
            }
            return
        }
        viewModelScope.launch {
            _dossier.value = DossierUiState.Loading(gameId)
            when (val result = safeRepositoryCall { repository.getGameRecommendation(gameId) }) {
                is RepositoryResult.Failure -> _dossier.value = DossierUiState.Error(
                    gameId,
                    result.error.message,
                )
                is RepositoryResult.Success -> _dossier.value = result.data
                    ?.let(DossierUiState::Success)
                    ?: DossierUiState.NotFound(gameId)
            }
        }
    }

    fun togglePick(gameId: String) {
        viewModelScope.launch {
            val current = _picks.value.any { it.game.gameId == gameId }
            val recommendation = getGameRecommendation(gameId)
            val existing = _state.value.user.gameStates[gameId]
            if (!current && (existing?.excluded == true || existing?.status.isTerminal())) {
                setToast("Finished or excluded games cannot be added to Picks")
                return@launch
            }
            if (!current && _picks.value.size >= 100) {
                setToast("Picks is limited to 100 games")
                return@launch
            }
            val write = try {
                repository.togglePick(gameId, !current)
            } catch (error: Exception) {
                setToast(error.message ?: "Could not update pick")
                return@launch
            }
            if (write is RepositoryResult.Failure) {
                setToast(write.error.message)
                return@launch
            }
            if (write is RepositoryResult.Success) {
                _ui.value = _ui.value.copy(pendingSync = _ui.value.pendingSync || write.pendingSync)
            }
            _picks.value = if (current) {
                _picks.value.filterNot { it.game.gameId == gameId }
            } else {
                recommendation?.let { listOf(it.copy(inPlayfitPicks = true)) + _picks.value }
                    ?: _picks.value
            }
            updateSavedPickIds(gameId = gameId, picked = !current)
            val next = ProductGameStateTransitions.setPick(existing, gameId, picked = !current)
            updateGameState(next)
            if (!current) removeRecommendation(gameId)
            if (current) setToast("Removed from picks") else setToast("Saved to picks")
        }
    }

    fun skipRecommendation(gameId: String) {
        _excludedIds.value = _excludedIds.value + gameId
        removeRecommendation(gameId)
        setToast("Skipped")
    }

    fun clearSkipped() {
        _excludedIds.value = emptySet()
        refreshRecommendations()
    }

    suspend fun searchGames(query: String): List<SeedGame> {
        return when (val result = safeRepositoryCall { repository.searchGames(query) }) {
            is RepositoryResult.Success -> result.data
            is RepositoryResult.Failure -> {
                _ui.value = _ui.value.copy(error = result.error.message)
                emptyList()
            }
        }
    }

    fun loadTasteModel() {
        viewModelScope.launch {
            try {
                when (val result = repository.getTasteModel()) {
                    is RepositoryResult.Success -> {
                        _tasteModel.value = result.data
                        _ui.value = _ui.value.copy(showingStaleData = result.isStale)
                    }
                    is RepositoryResult.Failure -> _ui.value = _ui.value.copy(error = result.error.message)
                }
            } catch (error: Exception) {
                _ui.value = _ui.value.copy(error = error.message)
            }
        }
    }

    fun removePick(gameId: String) {
        viewModelScope.launch {
            try {
                when (val result = repository.togglePick(gameId, false)) {
                    is RepositoryResult.Failure -> {
                        setToast(result.error.message)
                        return@launch
                    }
                    is RepositoryResult.Success -> {
                        _ui.value = _ui.value.copy(pendingSync = _ui.value.pendingSync || result.pendingSync)
                    }
                }
                _picks.value = _picks.value.filterNot { it.game.gameId == gameId }
                updateSavedPickIds(gameId = gameId, picked = false)
                val existing = _state.value.user.gameStates[gameId]
                updateGameState(ProductGameStateTransitions.setPick(existing, gameId, picked = false))
                loadTasteModel()
                setToast("Removed from picks.")
            } catch (_: Exception) {
                setToast("Could not remove pick.")
            }
        }
    }

    fun deleteSignal(gameId: String, source: String) {
        viewModelScope.launch {
            val current = _state.value
            var draft = current.user.onboarding
            var nextGameState = current.user.gameStates[gameId]

            when (source) {
                "onboarding_liked" -> draft = draft.copy(
                    likedGameIds = draft.likedGameIds.filterNot { it == gameId },
                )
                "onboarding_disliked" -> draft = draft.copy(
                    dislikedGameIds = draft.dislikedGameIds.filterNot { it == gameId },
                )
                else -> nextGameState = nextGameState?.let { state ->
                    state.copy(
                        rating = null,
                        excluded = false,
                        status = state.status.takeUnless { it.isTerminal() },
                    )
                }
            }

            if (source.startsWith("onboarding_") && nextGameState?.isInert() == true) {
                nextGameState = null
            }

            val write = if (nextGameState == null || nextGameState.isInert()) {
                safeRepositoryCall { repository.deleteGameState(gameId) }
            } else {
                safeRepositoryCall { repository.saveGameState(gameId, nextGameState) }
            }
            if (write is RepositoryResult.Failure) {
                setToast(write.error.message)
                return@launch
            }

            val gameStates = current.user.gameStates.toMutableMap().apply {
                if (nextGameState == null || nextGameState.isInert()) remove(gameId)
                else put(gameId, nextGameState)
            }
            _state.value = current.copy(
                user = current.user.copy(onboarding = draft, gameStates = gameStates),
            )
            _picks.value = _picks.value.filterNot { it.game.gameId == gameId && nextGameState?.inPlayfitPicks != true }

            when (val result = safeRepositoryCall {
                repository.rebuildTasteProfile(draft, current.user.onboardingCompletedAt)
            }) {
                is RepositoryResult.Failure -> _ui.value = _ui.value.copy(error = result.error.message)
                is RepositoryResult.Success -> {
                    if (!result.isStale) {
                        _state.value = _state.value.copy(
                            user = _state.value.user.copy(profile = result.data),
                        )
                    }
                    _ui.value = _ui.value.copy(pendingSync = _ui.value.pendingSync || result.pendingSync)
                }
            }
            loadTasteModel()
            setToast("Signal deleted.")
        }
    }

    fun resetTaste() {
        viewModelScope.launch {
            when (val result = safeRepositoryCall { repository.resetTaste() }) {
                is RepositoryResult.Failure -> {
                    _ui.value = _ui.value.copy(error = result.error.message)
                }
                is RepositoryResult.Success -> {
                    _state.value = ProductState()
                    _playNext.value = null
                    _picks.value = emptyList()
                    _tasteModel.value = null
                    _dossier.value = DossierUiState.Idle
                    _onboardingCompleted.value = false
                    _ui.value = _ui.value.copy(
                        pendingSync = result.pendingSync,
                        showingStaleData = false,
                    )
                    setToast(if (result.pendingSync) "Taste reset locally; waiting to sync" else "Taste reset")
                }
            }
        }
    }

    fun applyDecisionFeedback(gameId: String, feedback: ProductDecisionFeedback) {
        viewModelScope.launch {
            val write = try {
                repository.applyFeedback(gameId, feedback)
            } catch (error: Exception) {
                setToast(error.message ?: "Could not save feedback")
                return@launch
            }
            if (write is RepositoryResult.Failure) {
                setToast(write.error.message)
                return@launch
            }
            if (write is RepositoryResult.Success) {
                _ui.value = _ui.value.copy(pendingSync = _ui.value.pendingSync || write.pendingSync)
            }
            val existing = _state.value.user.gameStates[gameId]
            val next = ProductGameStateTransitions.applyFeedback(existing, gameId, feedback)
            updateGameState(next)
            if (!next.inPlayfitPicks) {
                _picks.value = _picks.value.filterNot { it.game.gameId == gameId }
                updateSavedPickIds(gameId = gameId, picked = false)
            }
            if (feedback == ProductDecisionFeedback.NotForMe) {
                _excludedIds.value = _excludedIds.value + gameId
                removeRecommendation(gameId)
            } else if (feedback.isPlayedFeedback()) {
                removeRecommendation(gameId)
            }
            rebuildProfileFromCurrentSignals()
            loadTasteModel()
            val message = when (feedback) {
                ProductDecisionFeedback.NotForMe -> "Noted. We'll find you something better."
                ProductDecisionFeedback.PlayedLoved -> "Already played and loved. Playfit will learn from it."
                ProductDecisionFeedback.PlayedLiked -> "Already played and liked."
                ProductDecisionFeedback.PlayedMixed -> "Marked as mixed. Playfit will tune around it."
                ProductDecisionFeedback.PlayedDropped -> "Marked as dropped. Playfit will steer away."
                ProductDecisionFeedback.Play -> "Set as playing. Your next pick will adapt around it."
                ProductDecisionFeedback.Later -> "Saved for later. Playfit will look past it for now."
                ProductDecisionFeedback.Loved -> "Marked as loved."
                ProductDecisionFeedback.Liked -> "Marked as liked."
                ProductDecisionFeedback.Mixed -> "Marked as mixed."
            }
            setToast(message)
        }
    }

    fun saveGameState(gameId: String, gameState: ProductGameState) {
        debouncedSave {
            repository.saveGameState(gameId, gameState)
        }
    }

    private fun debouncedSave(block: suspend () -> RepositoryResult<Unit>) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true)
            delay(1000)
            try {
                when (val result = block()) {
                    is RepositoryResult.Failure -> _ui.value = _ui.value.copy(
                        saving = false,
                        error = result.error.message,
                    )
                    is RepositoryResult.Success -> _ui.value = _ui.value.copy(
                        saving = false,
                        pendingSync = _ui.value.pendingSync || result.pendingSync,
                    )
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(saving = false, error = e.message)
            }
        }
    }

    fun updateState(updater: (ProductState) -> ProductState) {
        _state.value = updater(_state.value)
    }

    fun clearError() {
        _ui.value = _ui.value.copy(error = null)
    }

    suspend fun getDeviceId(): String = repository.getDeviceId()

    private fun setToast(message: String) {
        _ui.value = _ui.value.copy(toast = message)
    }

    fun clearToast() {
        _ui.value = _ui.value.copy(toast = null)
    }

    private fun updateGameState(next: ProductGameState) {
        _state.value = _state.value.copy(
            user = _state.value.user.copy(
                gameStates = _state.value.user.gameStates + (next.gameId to next),
            ),
        )
    }

    private fun updateSavedPickIds(gameId: String, picked: Boolean) {
        val current = _playNext.value ?: return
        val ids = current.savedPickIds.toMutableSet().apply {
            if (picked) add(gameId) else remove(gameId)
        }
        _playNext.value = current.copy(savedPickIds = ids.toList())
    }

    private fun removeRecommendation(gameId: String) {
        val current = _playNext.value ?: return
        val remaining = buildList {
            current.primary?.let(::add)
            addAll(current.alternatives)
        }.filterNot { it.game.gameId == gameId }

        val nextPrimary = if (current.primary?.game?.gameId == gameId) {
            remaining.firstOrNull()
        } else {
            current.primary
        }
        _playNext.value = current.copy(
            primary = nextPrimary,
            alternatives = remaining.filterNot { it.game.gameId == nextPrimary?.game?.gameId },
        )
    }

    private suspend fun refreshSecondaryData() {
        when (val result = safeRepositoryCall { repository.getPicks() }) {
            is RepositoryResult.Success -> _picks.value = result.data
            is RepositoryResult.Failure -> Unit
        }
        when (val result = safeRepositoryCall { repository.getTasteModel() }) {
            is RepositoryResult.Success -> _tasteModel.value = result.data
            is RepositoryResult.Failure -> Unit
        }
    }

    private suspend fun rebuildProfileFromCurrentSignals() {
        val current = _state.value
        when (val result = safeRepositoryCall {
            repository.rebuildTasteProfile(
                current.user.onboarding,
                current.user.onboardingCompletedAt,
            )
        }) {
            is RepositoryResult.Failure -> _ui.value = _ui.value.copy(error = result.error.message)
            is RepositoryResult.Success -> {
                if (!result.isStale) {
                    _state.value = _state.value.copy(
                        user = _state.value.user.copy(profile = result.data),
                    )
                }
                _ui.value = _ui.value.copy(pendingSync = _ui.value.pendingSync || result.pendingSync)
            }
        }
    }

    private fun ProductDecisionFeedback.isPlayedFeedback(): Boolean = when (this) {
        ProductDecisionFeedback.PlayedLoved,
        ProductDecisionFeedback.PlayedLiked,
        ProductDecisionFeedback.PlayedMixed,
        ProductDecisionFeedback.PlayedDropped -> true
        else -> false
    }

    private fun ProductPlayStatus?.isTerminal(): Boolean = when (this) {
        ProductPlayStatus.Beaten,
        ProductPlayStatus.Completed,
        ProductPlayStatus.Abandoned -> true
        else -> false
    }

    private fun ProductGameState.isInert(): Boolean =
        status == null && rating == null && !inPlayfitPicks && !inBacklog &&
            !inWishlist && !excluded

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(mode)
        }
    }

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
